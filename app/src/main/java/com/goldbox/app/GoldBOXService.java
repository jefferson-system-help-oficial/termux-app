package com.goldbox.app;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.goldbox.R;
import com.goldbox.app.event.SystemEventReceiver;
import com.goldbox.app.terminal.GoldBOXTerminalSessionActivityClient;
import com.goldbox.app.terminal.GoldBOXTerminalSessionServiceClient;
import com.goldbox.shared.goldbox.plugins.GoldBOXPluginUtils;
import com.goldbox.shared.data.IntentUtils;
import com.goldbox.shared.net.uri.UriUtils;
import com.goldbox.shared.errors.Errno;
import com.goldbox.shared.shell.ShellUtils;
import com.goldbox.shared.shell.command.runner.app.AppShell;
import com.goldbox.shared.goldbox.settings.properties.GoldBOXAppSharedProperties;
import com.goldbox.shared.goldbox.shell.command.environment.GoldBOXShellEnvironment;
import com.goldbox.shared.goldbox.shell.GoldBOXShellUtils;
import com.goldbox.shared.goldbox.GoldBOXConstants;
import com.goldbox.shared.goldbox.GoldBOXConstants.GOLDBOX_APP.GOLDBOX_ACTIVITY;
import com.goldbox.shared.goldbox.GoldBOXConstants.GOLDBOX_APP.GOLDBOX_SERVICE;
import com.goldbox.shared.goldbox.settings.preferences.GoldBOXAppSharedPreferences;
import com.goldbox.shared.goldbox.shell.GoldBOXShellManager;
import com.goldbox.shared.goldbox.shell.command.runner.terminal.GoldBOXSession;
import com.goldbox.shared.goldbox.terminal.GoldBOXTerminalSessionClientBase;
import com.goldbox.shared.logger.Logger;
import com.goldbox.shared.notification.NotificationUtils;
import com.goldbox.shared.android.PermissionUtils;
import com.goldbox.shared.data.DataUtils;
import com.goldbox.shared.shell.command.ExecutionCommand;
import com.goldbox.shared.shell.command.ExecutionCommand.Runner;
import com.goldbox.shared.shell.command.ExecutionCommand.ShellCreateMode;
import com.goldbox.terminal.TerminalEmulator;
import com.goldbox.terminal.TerminalSession;
import com.goldbox.terminal.TerminalSessionClient;

import java.util.ArrayList;
import java.util.List;

/**
 * A service holding a list of {@link GoldBOXSession} in {@link GoldBOXShellManager#mGoldBOXSessions} and background {@link AppShell}
 * in {@link GoldBOXShellManager#mGoldBOXTasks}, showing a foreground notification while running so that it is not terminated.
 * The user interacts with the session through {@link GoldBOXActivity}, but this service may outlive
 * the activity when the user or the system disposes of the activity. In that case the user may
 * restart {@link GoldBOXActivity} later to yet again access the sessions.
 * <p/>
 * In order to keep both terminal sessions and spawned processes (who may outlive the terminal sessions) alive as long
 * as wanted by the user this service is a foreground service, {@link Service#startForeground(int, Notification)}.
 * <p/>
 * Optionally may hold a wake and a wifi lock, in which case that is shown in the notification - see
 * {@link #buildNotification()}.
 */
public final class GoldBOXService extends Service implements AppShell.AppShellClient, GoldBOXSession.GoldBOXSessionClient {

    /** This service is only bound from inside the same process and never uses IPC. */
    class LocalBinder extends Binder {
        public final GoldBOXService service = GoldBOXService.this;
    }

    private final IBinder mBinder = new LocalBinder();

    private final Handler mHandler = new Handler();


    /** The full implementation of the {@link TerminalSessionClient} interface to be used by {@link TerminalSession}
     * that holds activity references for activity related functions.
     * Note that the service may often outlive the activity, so need to clear this reference.
     */
    private GoldBOXTerminalSessionActivityClient mGoldBOXTerminalSessionActivityClient;

    /** The basic implementation of the {@link TerminalSessionClient} interface to be used by {@link TerminalSession}
     * that does not hold activity references and only a service reference.
     */
    private final GoldBOXTerminalSessionServiceClient mGoldBOXTerminalSessionServiceClient = new GoldBOXTerminalSessionServiceClient(this);

    /**
     * GoldBOX app shared properties manager, loaded from goldbox.properties
     */
    private GoldBOXAppSharedProperties mProperties;

    /**
     * GoldBOX app shell manager
     */
    private GoldBOXShellManager mShellManager;

    /** The wake lock and wifi lock are always acquired and released together. */
    private PowerManager.WakeLock mWakeLock;
    private WifiManager.WifiLock mWifiLock;

    /** If the user has executed the {@link GOLDBOX_SERVICE#ACTION_STOP_SERVICE} intent. */
    boolean mWantsToStop = false;

    private static final String LOG_TAG = "GoldBOXService";

    @Override
    public void onCreate() {
        Logger.logVerbose(LOG_TAG, "onCreate");

        // Get GoldBOX app SharedProperties without loading from disk since GoldBOXApplication handles
        // load and GoldBOXActivity handles reloads
        mProperties = GoldBOXAppSharedProperties.getProperties();

        mShellManager = GoldBOXShellManager.getShellManager();

        runStartForeground();

        SystemEventReceiver.registerPackageUpdateEvents(this);
    }

    @SuppressLint("Wakelock")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.logDebug(LOG_TAG, "onStartCommand");

        // Run again in case service is already started and onCreate() is not called
        runStartForeground();

        String action = null;
        if (intent != null) {
            Logger.logVerboseExtended(LOG_TAG, "Intent Received:\n" + IntentUtils.getIntentString(intent));
            action = intent.getAction();
        }

        if (action != null) {
            switch (action) {
                case GOLDBOX_SERVICE.ACTION_STOP_SERVICE:
                    Logger.logDebug(LOG_TAG, "ACTION_STOP_SERVICE intent received");
                    actionStopService();
                    break;
                case GOLDBOX_SERVICE.ACTION_WAKE_LOCK:
                    Logger.logDebug(LOG_TAG, "ACTION_WAKE_LOCK intent received");
                    actionAcquireWakeLock();
                    break;
                case GOLDBOX_SERVICE.ACTION_WAKE_UNLOCK:
                    Logger.logDebug(LOG_TAG, "ACTION_WAKE_UNLOCK intent received");
                    actionReleaseWakeLock(true);
                    break;
                case GOLDBOX_SERVICE.ACTION_SERVICE_EXECUTE:
                    Logger.logDebug(LOG_TAG, "ACTION_SERVICE_EXECUTE intent received");
                    actionServiceExecute(intent);
                    break;
                default:
                    Logger.logError(LOG_TAG, "Invalid action: \"" + action + "\"");
                    break;
            }
        }

        // If this service really do get killed, there is no point restarting it automatically - let the user do on next
        // start of {@link Term):
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Logger.logVerbose(LOG_TAG, "onDestroy");

        GoldBOXShellUtils.clearGoldBOXTMPDIR(true);

        actionReleaseWakeLock(false);
        if (!mWantsToStop)
            killAllGoldBOXExecutionCommands();

        GoldBOXShellManager.onAppExit(this);

        SystemEventReceiver.unregisterPackageUpdateEvents(this);

        runStopForeground();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logger.logVerbose(LOG_TAG, "onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Logger.logVerbose(LOG_TAG, "onUnbind");

        // Since we cannot rely on {@link GoldBOXActivity.onDestroy()} to always complete,
        // we unset clients here as well if it failed, so that we do not leave service and session
        // clients with references to the activity.
        if (mGoldBOXTerminalSessionActivityClient != null)
            unsetGoldBOXTerminalSessionClient();
        return false;
    }

    /** Make service run in foreground mode. */
    private void runStartForeground() {
        setupNotificationChannel();
        startForeground(GoldBOXConstants.GOLDBOX_APP_NOTIFICATION_ID, buildNotification());
    }

    /** Make service leave foreground mode. */
    private void runStopForeground() {
        stopForeground(true);
    }

    /** Request to stop service. */
    private void requestStopService() {
        Logger.logDebug(LOG_TAG, "Requesting to stop service");
        runStopForeground();
        stopSelf();
    }

    /** Process action to stop service. */
    private void actionStopService() {
        mWantsToStop = true;
        killAllGoldBOXExecutionCommands();
        requestStopService();
    }

    /** Kill all GoldBOXSessions and GoldBOXTasks by sending SIGKILL to their processes.
     *
     * For GoldBOXSessions, all sessions will be killed, whether user manually exited GoldBOX or if
     * onDestroy() was directly called because of unintended shutdown. The processing of results
     * will only be done if user manually exited goldbox or if the session was started by a plugin
     * which **expects** the result back via a pending intent.
     *
     * For GoldBOXTasks, only tasks that were started by a plugin which **expects** the result
     * back via a pending intent will be killed, whether user manually exited GoldBOX or if
     * onDestroy() was directly called because of unintended shutdown. The processing of results
     * will always be done for the tasks that are killed. The remaining processes will keep on
     * running until the goldbox app process is killed by android, like by OOM, so we let them run
     * as long as they can.
     *
     * Some plugin execution commands may not have been processed and added to mGoldBOXSessions and
     * mGoldBOXTasks lists before the service is killed, so we maintain a separate
     * mPendingPluginExecutionCommands list for those, so that we can notify the pending intent
     * creators that execution was cancelled.
     *
     * Note that if user didn't manually exit GoldBOX and if onDestroy() was directly called because
     * of unintended shutdown, like android deciding to kill the service, then there will be no
     * guarantee that onDestroy() will be allowed to finish and goldbox app process may be killed before
     * it has finished. This means that in those cases some results may not be sent back to their
     * creators for plugin commands but we still try to process whatever results can be processed
     * despite the unreliable behaviour of onDestroy().
     *
     * Note that if don't kill the processes started by plugins which **expect** the result back
     * and notify their creators that they have been killed, then they may get stuck waiting for
     * the results forever like in case of commands started by GoldBOX:Tasker or RUN_COMMAND intent,
     * since once GoldBOXService has been killed, no result will be sent back. They may still get
     * stuck if goldbox app process gets killed, so for this case reasonable timeout values should
     * be used, like in Tasker for the GoldBOX:Tasker actions.
     *
     * We make copies of each list since items are removed inside the loop.
     */
    private synchronized void killAllGoldBOXExecutionCommands() {
        boolean processResult;

        Logger.logDebug(LOG_TAG, "Killing GoldBOXSessions=" + mShellManager.mGoldBOXSessions.size() +
            ", GoldBOXTasks=" + mShellManager.mGoldBOXTasks.size() +
            ", PendingPluginExecutionCommands=" + mShellManager.mPendingPluginExecutionCommands.size());

        List<GoldBOXSession> goldboxSessions = new ArrayList<>(mShellManager.mGoldBOXSessions);
        List<AppShell> goldboxTasks = new ArrayList<>(mShellManager.mGoldBOXTasks);
        List<ExecutionCommand> pendingPluginExecutionCommands = new ArrayList<>(mShellManager.mPendingPluginExecutionCommands);

        for (int i = 0; i < goldboxSessions.size(); i++) {
            ExecutionCommand executionCommand = goldboxSessions.get(i).getExecutionCommand();
            processResult = mWantsToStop || executionCommand.isPluginExecutionCommandWithPendingResult();
            goldboxSessions.get(i).killIfExecuting(this, processResult);
            if (!processResult)
                mShellManager.mGoldBOXSessions.remove(goldboxSessions.get(i));
        }


        for (int i = 0; i < goldboxTasks.size(); i++) {
            ExecutionCommand executionCommand = goldboxTasks.get(i).getExecutionCommand();
            if (executionCommand.isPluginExecutionCommandWithPendingResult())
                goldboxTasks.get(i).killIfExecuting(this, true);
            else
                mShellManager.mGoldBOXTasks.remove(goldboxTasks.get(i));
        }

        for (int i = 0; i < pendingPluginExecutionCommands.size(); i++) {
            ExecutionCommand executionCommand = pendingPluginExecutionCommands.get(i);
            if (!executionCommand.shouldNotProcessResults() && executionCommand.isPluginExecutionCommandWithPendingResult()) {
                if (executionCommand.setStateFailed(Errno.ERRNO_CANCELLED.getCode(), this.getString(com.goldbox.shared.R.string.error_execution_cancelled))) {
                    GoldBOXPluginUtils.processPluginExecutionCommandResult(this, LOG_TAG, executionCommand);
                }
            }
        }
    }



    /** Process action to acquire Power and Wi-Fi WakeLocks. */
    @SuppressLint({"WakelockTimeout", "BatteryLife"})
    private void actionAcquireWakeLock() {
        if (mWakeLock != null) {
            Logger.logDebug(LOG_TAG, "Ignoring acquiring WakeLocks since they are already held");
            return;
        }

        Logger.logDebug(LOG_TAG, "Acquiring WakeLocks");

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, GoldBOXConstants.GOLDBOX_APP_NAME.toLowerCase() + ":service-wakelock");
        mWakeLock.acquire();

        // http://tools.android.com/tech-docs/lint-in-studio-2-3#TOC-WifiManager-Leak
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, GoldBOXConstants.GOLDBOX_APP_NAME.toLowerCase());
        mWifiLock.acquire();

        if (!PermissionUtils.checkIfBatteryOptimizationsDisabled(this)) {
            PermissionUtils.requestDisableBatteryOptimizations(this);
        }

        updateNotification();

        Logger.logDebug(LOG_TAG, "WakeLocks acquired successfully");

    }

    /** Process action to release Power and Wi-Fi WakeLocks. */
    private void actionReleaseWakeLock(boolean updateNotification) {
        if (mWakeLock == null && mWifiLock == null) {
            Logger.logDebug(LOG_TAG, "Ignoring releasing WakeLocks since none are already held");
            return;
        }

        Logger.logDebug(LOG_TAG, "Releasing WakeLocks");

        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }

        if (mWifiLock != null) {
            mWifiLock.release();
            mWifiLock = null;
        }

        if (updateNotification)
            updateNotification();

        Logger.logDebug(LOG_TAG, "WakeLocks released successfully");
    }

    /** Process {@link GOLDBOX_SERVICE#ACTION_SERVICE_EXECUTE} intent to execute a shell command in
     * a foreground GoldBOXSession or in a background GoldBOXTask. */
    private void actionServiceExecute(Intent intent) {
        if (intent == null) {
            Logger.logError(LOG_TAG, "Ignoring null intent to actionServiceExecute");
            return;
        }

        ExecutionCommand executionCommand = new ExecutionCommand(GoldBOXShellManager.getNextShellId());

        executionCommand.executableUri = intent.getData();
        executionCommand.isPluginExecutionCommand = true;

        // If EXTRA_RUNNER is passed, use that, otherwise check EXTRA_BACKGROUND and default to Runner.TERMINAL_SESSION
        executionCommand.runner = IntentUtils.getStringExtraIfSet(intent, GOLDBOX_SERVICE.EXTRA_RUNNER,
            (intent.getBooleanExtra(GOLDBOX_SERVICE.EXTRA_BACKGROUND, false) ? Runner.APP_SHELL.getName() : Runner.TERMINAL_SESSION.getName()));
        if (Runner.runnerOf(executionCommand.runner) == null) {
            String errmsg = this.getString(R.string.error_goldbox_service_invalid_execution_command_runner, executionCommand.runner);
            executionCommand.setStateFailed(Errno.ERRNO_FAILED.getCode(), errmsg);
            GoldBOXPluginUtils.processPluginExecutionCommandError(this, LOG_TAG, executionCommand, false);
            return;
        }

        if (executionCommand.executableUri != null) {
            Logger.logVerbose(LOG_TAG, "uri: \"" + executionCommand.executableUri + "\", path: \"" + executionCommand.executableUri.getPath() + "\", fragment: \"" + executionCommand.executableUri.getFragment() + "\"");

            // Get full path including fragment (anything after last "#")
            executionCommand.executable = UriUtils.getUriFilePathWithFragment(executionCommand.executableUri);
            executionCommand.arguments = IntentUtils.getStringArrayExtraIfSet(intent, GOLDBOX_SERVICE.EXTRA_ARGUMENTS, null);
            if (Runner.APP_SHELL.equalsRunner(executionCommand.runner))
                executionCommand.stdin = IntentUtils.getStringExtraIfSet(intent, GOLDBOX_SERVICE.EXTRA_STDIN, null);
            executionCommand.backgroundCustomLogLevel = IntentUtils.getIntegerExtraIfSet(intent, GOLDBOX_SERVICE.EXTRA_BACKGROUND_CUSTOM_LOG_LEVEL, null);
        }

        executionCommand.workingDirectory = IntentUtils.getStringExtraIfSet(intent, GOLDBOX_SERVICE.EXTRA_WORKDIR, null);
        executionCommand.isFailsafe = intent.getBooleanExtra(GOLDBOX_ACTIVITY.EXTRA_FAILSAFE_SESSION, false);
        executionCommand.sessionAction = intent.getStringExtra(GOLDBOX_SERVICE.EXTRA_SESSION_ACTION);
        executionCommand.shellName = IntentUtils.getStringExtraIfSet(intent, GOLDBOX_SERVICE.EXTRA_SHELL_NAME, null);
        executionCommand.shellCreateMode = IntentUtils.getStringExtraIfSet(intent, GOLDBOX_SERVICE.EXTRA_SHELL_CREATE_MODE, null);
        executionCommand.commandLabel = IntentUtils.getStringExtraIfSet(intent, GOLDBOX_SERVICE.EXTRA_COMMAND_LABEL, "Execution Intent Command");
        executionCommand.commandDescription = IntentUtils.getStringExtraIfSet(intent, GOLDBOX_SERVICE.EXTRA_COMMAND_DESCRIPTION, null);
        executionCommand.commandHelp = IntentUtils.getStringExtraIfSet(intent, GOLDBOX_SERVICE.EXTRA_COMMAND_HELP, null);
        executionCommand.pluginAPIHelp = IntentUtils.getStringExtraIfSet(intent, GOLDBOX_SERVICE.EXTRA_PLUGIN_API_HELP, null);
        executionCommand.resultConfig.resultPendingIntent = intent.getParcelableExtra(GOLDBOX_SERVICE.EXTRA_PENDING_INTENT);
        executionCommand.resultConfig.resultDirectoryPath = IntentUtils.getStringExtraIfSet(intent, GOLDBOX_SERVICE.EXTRA_RESULT_DIRECTORY, null);
        if (executionCommand.resultConfig.resultDirectoryPath != null) {
            executionCommand.resultConfig.resultSingleFile = intent.getBooleanExtra(GOLDBOX_SERVICE.EXTRA_RESULT_SINGLE_FILE, false);
            executionCommand.resultConfig.resultFileBasename = IntentUtils.getStringExtraIfSet(intent, GOLDBOX_SERVICE.EXTRA_RESULT_FILE_BASENAME, null);
            executionCommand.resultConfig.resultFileOutputFormat = IntentUtils.getStringExtraIfSet(intent, GOLDBOX_SERVICE.EXTRA_RESULT_FILE_OUTPUT_FORMAT, null);
            executionCommand.resultConfig.resultFileErrorFormat = IntentUtils.getStringExtraIfSet(intent, GOLDBOX_SERVICE.EXTRA_RESULT_FILE_ERROR_FORMAT, null);
            executionCommand.resultConfig.resultFilesSuffix = IntentUtils.getStringExtraIfSet(intent, GOLDBOX_SERVICE.EXTRA_RESULT_FILES_SUFFIX, null);
        }

        if (executionCommand.shellCreateMode == null)
            executionCommand.shellCreateMode = ShellCreateMode.ALWAYS.getMode();

        // Add the execution command to pending plugin execution commands list
        mShellManager.mPendingPluginExecutionCommands.add(executionCommand);

        if (Runner.APP_SHELL.equalsRunner(executionCommand.runner))
            executeGoldBOXTaskCommand(executionCommand);
        else if (Runner.TERMINAL_SESSION.equalsRunner(executionCommand.runner))
            executeGoldBOXSessionCommand(executionCommand);
        else {
            String errmsg = getString(R.string.error_goldbox_service_unsupported_execution_command_runner, executionCommand.runner);
            executionCommand.setStateFailed(Errno.ERRNO_FAILED.getCode(), errmsg);
            GoldBOXPluginUtils.processPluginExecutionCommandError(this, LOG_TAG, executionCommand, false);
        }
    }





    /** Execute a shell command in background GoldBOXTask. */
    private void executeGoldBOXTaskCommand(ExecutionCommand executionCommand) {
        if (executionCommand == null) return;

        Logger.logDebug(LOG_TAG, "Executing background \"" + executionCommand.getCommandIdAndLabelLogString() + "\" GoldBOXTask command");

        // Transform executable path to shell/session name, e.g. "/bin/do-something.sh" => "do-something.sh".
        if (executionCommand.shellName == null && executionCommand.executable != null)
            executionCommand.shellName = ShellUtils.getExecutableBasename(executionCommand.executable);

        AppShell newGoldBOXTask = null;
        ShellCreateMode shellCreateMode = processShellCreateMode(executionCommand);
        if (shellCreateMode == null) return;
        if (ShellCreateMode.NO_SHELL_WITH_NAME.equals(shellCreateMode)) {
            newGoldBOXTask = getGoldBOXTaskForShellName(executionCommand.shellName);
            if (newGoldBOXTask != null)
                Logger.logVerbose(LOG_TAG, "Existing GoldBOXTask with \"" + executionCommand.shellName + "\" shell name found for shell create mode \"" + shellCreateMode.getMode() + "\"");
            else
                Logger.logVerbose(LOG_TAG, "No existing GoldBOXTask with \"" + executionCommand.shellName + "\" shell name found for shell create mode \"" + shellCreateMode.getMode() + "\"");
        }

        if (newGoldBOXTask == null)
            newGoldBOXTask = createGoldBOXTask(executionCommand);
    }

    /** Create a GoldBOXTask. */
    @Nullable
    public AppShell createGoldBOXTask(String executablePath, String[] arguments, String stdin, String workingDirectory) {
        return createGoldBOXTask(new ExecutionCommand(GoldBOXShellManager.getNextShellId(), executablePath,
            arguments, stdin, workingDirectory, Runner.APP_SHELL.getName(), false));
    }

    /** Create a GoldBOXTask. */
    @Nullable
    public synchronized AppShell createGoldBOXTask(ExecutionCommand executionCommand) {
        if (executionCommand == null) return null;

        Logger.logDebug(LOG_TAG, "Creating \"" + executionCommand.getCommandIdAndLabelLogString() + "\" GoldBOXTask");

        if (!Runner.APP_SHELL.equalsRunner(executionCommand.runner)) {
            Logger.logDebug(LOG_TAG, "Ignoring wrong runner \"" + executionCommand.runner + "\" command passed to createGoldBOXTask()");
            return null;
        }

        executionCommand.setShellCommandShellEnvironment = true;

        if (Logger.getLogLevel() >= Logger.LOG_LEVEL_VERBOSE)
            Logger.logVerboseExtended(LOG_TAG, executionCommand.toString());

        AppShell newGoldBOXTask = AppShell.execute(this, executionCommand, this,
            new GoldBOXShellEnvironment(), null,false);
        if (newGoldBOXTask == null) {
            Logger.logError(LOG_TAG, "Failed to execute new GoldBOXTask command for:\n" + executionCommand.getCommandIdAndLabelLogString());
            // If the execution command was started for a plugin, then process the error
            if (executionCommand.isPluginExecutionCommand)
                GoldBOXPluginUtils.processPluginExecutionCommandError(this, LOG_TAG, executionCommand, false);
            else {
                Logger.logError(LOG_TAG, "Set log level to debug or higher to see error in logs");
                Logger.logErrorPrivateExtended(LOG_TAG, executionCommand.toString());
            }
            return null;
        }

        mShellManager.mGoldBOXTasks.add(newGoldBOXTask);

        // Remove the execution command from the pending plugin execution commands list since it has
        // now been processed
        if (executionCommand.isPluginExecutionCommand)
            mShellManager.mPendingPluginExecutionCommands.remove(executionCommand);

        updateNotification();

        return newGoldBOXTask;
    }

    /** Callback received when a GoldBOXTask finishes. */
    @Override
    public void onAppShellExited(final AppShell goldboxTask) {
        mHandler.post(() -> {
            if (goldboxTask != null) {
                ExecutionCommand executionCommand = goldboxTask.getExecutionCommand();

                Logger.logVerbose(LOG_TAG, "The onGoldBOXTaskExited() callback called for \"" + executionCommand.getCommandIdAndLabelLogString() + "\" GoldBOXTask command");

                // If the execution command was started for a plugin, then process the results
                if (executionCommand != null && executionCommand.isPluginExecutionCommand)
                    GoldBOXPluginUtils.processPluginExecutionCommandResult(this, LOG_TAG, executionCommand);

                mShellManager.mGoldBOXTasks.remove(goldboxTask);
            }

            updateNotification();
        });
    }





    /** Execute a shell command in a foreground {@link GoldBOXSession}. */
    private void executeGoldBOXSessionCommand(ExecutionCommand executionCommand) {
        if (executionCommand == null) return;

        Logger.logDebug(LOG_TAG, "Executing foreground \"" + executionCommand.getCommandIdAndLabelLogString() + "\" GoldBOXSession command");

        // Transform executable path to shell/session name, e.g. "/bin/do-something.sh" => "do-something.sh".
        if (executionCommand.shellName == null && executionCommand.executable != null)
            executionCommand.shellName = ShellUtils.getExecutableBasename(executionCommand.executable);

        GoldBOXSession newGoldBOXSession = null;
        ShellCreateMode shellCreateMode = processShellCreateMode(executionCommand);
        if (shellCreateMode == null) return;
        if (ShellCreateMode.NO_SHELL_WITH_NAME.equals(shellCreateMode)) {
            newGoldBOXSession = getGoldBOXSessionForShellName(executionCommand.shellName);
            if (newGoldBOXSession != null)
                Logger.logVerbose(LOG_TAG, "Existing GoldBOXSession with \"" + executionCommand.shellName + "\" shell name found for shell create mode \"" + shellCreateMode.getMode() + "\"");
            else
                Logger.logVerbose(LOG_TAG, "No existing GoldBOXSession with \"" + executionCommand.shellName + "\" shell name found for shell create mode \"" + shellCreateMode.getMode() + "\"");
        }

        if (newGoldBOXSession == null)
            newGoldBOXSession = createGoldBOXSession(executionCommand);
        if (newGoldBOXSession == null) return;

        handleSessionAction(DataUtils.getIntFromString(executionCommand.sessionAction,
            GOLDBOX_SERVICE.VALUE_EXTRA_SESSION_ACTION_SWITCH_TO_NEW_SESSION_AND_OPEN_ACTIVITY),
            newGoldBOXSession.getTerminalSession());
    }

    /**
     * Create a {@link GoldBOXSession}.
     * Currently called by {@link GoldBOXTerminalSessionActivityClient#addNewSession(boolean, String)} to add a new {@link GoldBOXSession}.
     */
    @Nullable
    public GoldBOXSession createGoldBOXSession(String executablePath, String[] arguments, String stdin,
                                             String workingDirectory, boolean isFailSafe, String sessionName) {
        ExecutionCommand executionCommand = new ExecutionCommand(GoldBOXShellManager.getNextShellId(),
            executablePath, arguments, stdin, workingDirectory, Runner.TERMINAL_SESSION.getName(), isFailSafe);
        executionCommand.shellName = sessionName;
        return createGoldBOXSession(executionCommand);
    }

    /** Create a {@link GoldBOXSession}. */
    @Nullable
    public synchronized GoldBOXSession createGoldBOXSession(ExecutionCommand executionCommand) {
        if (executionCommand == null) return null;

        Logger.logDebug(LOG_TAG, "Creating \"" + executionCommand.getCommandIdAndLabelLogString() + "\" GoldBOXSession");

        if (!Runner.TERMINAL_SESSION.equalsRunner(executionCommand.runner)) {
            Logger.logDebug(LOG_TAG, "Ignoring wrong runner \"" + executionCommand.runner + "\" command passed to createGoldBOXSession()");
            return null;
        }

        executionCommand.setShellCommandShellEnvironment = true;
        executionCommand.terminalTranscriptRows = mProperties.getTerminalTranscriptRows();

        if (Logger.getLogLevel() >= Logger.LOG_LEVEL_VERBOSE)
            Logger.logVerboseExtended(LOG_TAG, executionCommand.toString());

        // If the execution command was started for a plugin, only then will the stdout be set
        // Otherwise if command was manually started by the user like by adding a new terminal session,
        // then no need to set stdout
        GoldBOXSession newGoldBOXSession = GoldBOXSession.execute(this, executionCommand, getGoldBOXTerminalSessionClient(),
            this, new GoldBOXShellEnvironment(), null, executionCommand.isPluginExecutionCommand);
        if (newGoldBOXSession == null) {
            Logger.logError(LOG_TAG, "Failed to execute new GoldBOXSession command for:\n" + executionCommand.getCommandIdAndLabelLogString());
            // If the execution command was started for a plugin, then process the error
            if (executionCommand.isPluginExecutionCommand)
                GoldBOXPluginUtils.processPluginExecutionCommandError(this, LOG_TAG, executionCommand, false);
            else {
                Logger.logError(LOG_TAG, "Set log level to debug or higher to see error in logs");
                Logger.logErrorPrivateExtended(LOG_TAG, executionCommand.toString());
            }
            return null;
        }

        mShellManager.mGoldBOXSessions.add(newGoldBOXSession);

        // Remove the execution command from the pending plugin execution commands list since it has
        // now been processed
        if (executionCommand.isPluginExecutionCommand)
            mShellManager.mPendingPluginExecutionCommands.remove(executionCommand);

        // Notify {@link GoldBOXSessionsListViewController} that sessions list has been updated if
        // activity in is foreground
        if (mGoldBOXTerminalSessionActivityClient != null)
            mGoldBOXTerminalSessionActivityClient.goldboxSessionListNotifyUpdated();

        updateNotification();

        // No need to recreate the activity since it likely just started and theme should already have applied
        GoldBOXActivity.updateGoldBOXActivityStyling(this, false);

        return newGoldBOXSession;
    }

    /** Remove a GoldBOXSession. */
    public synchronized int removeGoldBOXSession(TerminalSession sessionToRemove) {
        int index = getIndexOfSession(sessionToRemove);

        if (index >= 0)
            mShellManager.mGoldBOXSessions.get(index).finish();

        return index;
    }

    /** Callback received when a {@link GoldBOXSession} finishes. */
    @Override
    public void onGoldBOXSessionExited(final GoldBOXSession goldboxSession) {
        if (goldboxSession != null) {
            ExecutionCommand executionCommand = goldboxSession.getExecutionCommand();

            Logger.logVerbose(LOG_TAG, "The onGoldBOXSessionExited() callback called for \"" + executionCommand.getCommandIdAndLabelLogString() + "\" GoldBOXSession command");

            // If the execution command was started for a plugin, then process the results
            if (executionCommand != null && executionCommand.isPluginExecutionCommand)
                GoldBOXPluginUtils.processPluginExecutionCommandResult(this, LOG_TAG, executionCommand);

            mShellManager.mGoldBOXSessions.remove(goldboxSession);

            // Notify {@link GoldBOXSessionsListViewController} that sessions list has been updated if
            // activity in is foreground
            if (mGoldBOXTerminalSessionActivityClient != null)
                mGoldBOXTerminalSessionActivityClient.goldboxSessionListNotifyUpdated();
        }

        updateNotification();
    }





    private ShellCreateMode processShellCreateMode(@NonNull ExecutionCommand executionCommand) {
        if (ShellCreateMode.ALWAYS.equalsMode(executionCommand.shellCreateMode))
            return ShellCreateMode.ALWAYS; // Default
        else if (ShellCreateMode.NO_SHELL_WITH_NAME.equalsMode(executionCommand.shellCreateMode))
            if (DataUtils.isNullOrEmpty(executionCommand.shellName)) {
                GoldBOXPluginUtils.setAndProcessPluginExecutionCommandError(this, LOG_TAG, executionCommand, false,
                    getString(R.string.error_goldbox_service_execution_command_shell_name_unset, executionCommand.shellCreateMode));
                return null;
            } else {
               return ShellCreateMode.NO_SHELL_WITH_NAME;
            }
        else {
            GoldBOXPluginUtils.setAndProcessPluginExecutionCommandError(this, LOG_TAG, executionCommand, false,
                getString(R.string.error_goldbox_service_unsupported_execution_command_shell_create_mode, executionCommand.shellCreateMode));
            return null;
        }
    }

    /** Process session action for new session. */
    private void handleSessionAction(int sessionAction, TerminalSession newTerminalSession) {
        Logger.logDebug(LOG_TAG, "Processing sessionAction \"" + sessionAction + "\" for session \"" + newTerminalSession.mSessionName + "\"");

        switch (sessionAction) {
            case GOLDBOX_SERVICE.VALUE_EXTRA_SESSION_ACTION_SWITCH_TO_NEW_SESSION_AND_OPEN_ACTIVITY:
                setCurrentStoredTerminalSession(newTerminalSession);
                if (mGoldBOXTerminalSessionActivityClient != null)
                    mGoldBOXTerminalSessionActivityClient.setCurrentSession(newTerminalSession);
                startGoldBOXActivity();
                break;
            case GOLDBOX_SERVICE.VALUE_EXTRA_SESSION_ACTION_KEEP_CURRENT_SESSION_AND_OPEN_ACTIVITY:
                if (getGoldBOXSessionsSize() == 1)
                    setCurrentStoredTerminalSession(newTerminalSession);
                startGoldBOXActivity();
                break;
            case GOLDBOX_SERVICE.VALUE_EXTRA_SESSION_ACTION_SWITCH_TO_NEW_SESSION_AND_DONT_OPEN_ACTIVITY:
                setCurrentStoredTerminalSession(newTerminalSession);
                if (mGoldBOXTerminalSessionActivityClient != null)
                    mGoldBOXTerminalSessionActivityClient.setCurrentSession(newTerminalSession);
                break;
            case GOLDBOX_SERVICE.VALUE_EXTRA_SESSION_ACTION_KEEP_CURRENT_SESSION_AND_DONT_OPEN_ACTIVITY:
                if (getGoldBOXSessionsSize() == 1)
                    setCurrentStoredTerminalSession(newTerminalSession);
                break;
            default:
                Logger.logError(LOG_TAG, "Invalid sessionAction: \"" + sessionAction + "\". Force using default sessionAction.");
                handleSessionAction(GOLDBOX_SERVICE.VALUE_EXTRA_SESSION_ACTION_SWITCH_TO_NEW_SESSION_AND_OPEN_ACTIVITY, newTerminalSession);
                break;
        }
    }

    /** Launch the {@link }GoldBOXActivity} to bring it to foreground. */
    private void startGoldBOXActivity() {
        // For android >= 10, apps require Display over other apps permission to start foreground activities
        // from background (services). If it is not granted, then GoldBOXSessions that are started will
        // show in GoldBOX notification but will not run until user manually clicks the notification.
        if (PermissionUtils.validateDisplayOverOtherAppsPermissionForPostAndroid10(this, true)) {
            GoldBOXActivity.startGoldBOXActivity(this);
        } else {
            GoldBOXAppSharedPreferences preferences = GoldBOXAppSharedPreferences.build(this);
            if (preferences == null) return;
            if (preferences.arePluginErrorNotificationsEnabled(false))
                Logger.showToast(this, this.getString(R.string.error_display_over_other_apps_permission_not_granted_to_start_terminal), true);
        }
    }





    /** If {@link GoldBOXActivity} has not bound to the {@link GoldBOXService} yet or is destroyed, then
     * interface functions requiring the activity should not be available to the terminal sessions,
     * so we just return the {@link #mGoldBOXTerminalSessionServiceClient}. Once {@link GoldBOXActivity} bind
     * callback is received, it should call {@link #setGoldBOXTerminalSessionClient} to set the
     * {@link GoldBOXService#mGoldBOXTerminalSessionActivityClient} so that further terminal sessions are directly
     * passed the {@link GoldBOXTerminalSessionActivityClient} object which fully implements the
     * {@link TerminalSessionClient} interface.
     *
     * @return Returns the {@link GoldBOXTerminalSessionActivityClient} if {@link GoldBOXActivity} has bound with
     * {@link GoldBOXService}, otherwise {@link GoldBOXTerminalSessionServiceClient}.
     */
    public synchronized GoldBOXTerminalSessionClientBase getGoldBOXTerminalSessionClient() {
        if (mGoldBOXTerminalSessionActivityClient != null)
            return mGoldBOXTerminalSessionActivityClient;
        else
            return mGoldBOXTerminalSessionServiceClient;
    }

    /** This should be called when {@link GoldBOXActivity#onServiceConnected} is called to set the
     * {@link GoldBOXService#mGoldBOXTerminalSessionActivityClient} variable and update the {@link TerminalSession}
     * and {@link TerminalEmulator} clients in case they were passed {@link GoldBOXTerminalSessionServiceClient}
     * earlier.
     *
     * @param goldboxTerminalSessionActivityClient The {@link GoldBOXTerminalSessionActivityClient} object that fully
     * implements the {@link TerminalSessionClient} interface.
     */
    public synchronized void setGoldBOXTerminalSessionClient(GoldBOXTerminalSessionActivityClient goldboxTerminalSessionActivityClient) {
        mGoldBOXTerminalSessionActivityClient = goldboxTerminalSessionActivityClient;

        for (int i = 0; i < mShellManager.mGoldBOXSessions.size(); i++)
            mShellManager.mGoldBOXSessions.get(i).getTerminalSession().updateTerminalSessionClient(mGoldBOXTerminalSessionActivityClient);
    }

    /** This should be called when {@link GoldBOXActivity} has been destroyed and in {@link #onUnbind(Intent)}
     * so that the {@link GoldBOXService} and {@link TerminalSession} and {@link TerminalEmulator}
     * clients do not hold an activity references.
     */
    public synchronized void unsetGoldBOXTerminalSessionClient() {
        for (int i = 0; i < mShellManager.mGoldBOXSessions.size(); i++)
            mShellManager.mGoldBOXSessions.get(i).getTerminalSession().updateTerminalSessionClient(mGoldBOXTerminalSessionServiceClient);

        mGoldBOXTerminalSessionActivityClient = null;
    }





    private Notification buildNotification() {
        Resources res = getResources();

        // Set pending intent to be launched when notification is clicked
        Intent notificationIntent = GoldBOXActivity.newInstance(this);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);


        // Set notification text
        int sessionCount = getGoldBOXSessionsSize();
        int taskCount = mShellManager.mGoldBOXTasks.size();
        String notificationText = sessionCount + " session" + (sessionCount == 1 ? "" : "s");
        if (taskCount > 0) {
            notificationText += ", " + taskCount + " task" + (taskCount == 1 ? "" : "s");
        }

        final boolean wakeLockHeld = mWakeLock != null;
        if (wakeLockHeld) notificationText += " (wake lock held)";


        // Set notification priority
        // If holding a wake or wifi lock consider the notification of high priority since it's using power,
        // otherwise use a low priority
        int priority = (wakeLockHeld) ? Notification.PRIORITY_HIGH : Notification.PRIORITY_LOW;


        // Build the notification
        Notification.Builder builder =  NotificationUtils.geNotificationBuilder(this,
            GoldBOXConstants.GOLDBOX_APP_NOTIFICATION_CHANNEL_ID, priority,
            GoldBOXConstants.GOLDBOX_APP_NAME, notificationText, null,
            contentIntent, null, NotificationUtils.NOTIFICATION_MODE_SILENT);
        if (builder == null)  return null;

        // No need to show a timestamp:
        builder.setShowWhen(false);

        // Set notification icon
        builder.setSmallIcon(R.drawable.ic_service_notification);

        // Set background color for small notification icon
        builder.setColor(0xFF607D8B);

        // GoldBOXSessions are always ongoing
        builder.setOngoing(true);


        // Set Exit button action
        Intent exitIntent = new Intent(this, GoldBOXService.class).setAction(GOLDBOX_SERVICE.ACTION_STOP_SERVICE);
        builder.addAction(android.R.drawable.ic_delete, res.getString(R.string.notification_action_exit), PendingIntent.getService(this, 0, exitIntent, 0));


        // Set Wakelock button actions
        String newWakeAction = wakeLockHeld ? GOLDBOX_SERVICE.ACTION_WAKE_UNLOCK : GOLDBOX_SERVICE.ACTION_WAKE_LOCK;
        Intent toggleWakeLockIntent = new Intent(this, GoldBOXService.class).setAction(newWakeAction);
        String actionTitle = res.getString(wakeLockHeld ? R.string.notification_action_wake_unlock : R.string.notification_action_wake_lock);
        int actionIcon = wakeLockHeld ? android.R.drawable.ic_lock_idle_lock : android.R.drawable.ic_lock_lock;
        builder.addAction(actionIcon, actionTitle, PendingIntent.getService(this, 0, toggleWakeLockIntent, 0));


        return builder.build();
    }

    private void setupNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationUtils.setupNotificationChannel(this, GoldBOXConstants.GOLDBOX_APP_NOTIFICATION_CHANNEL_ID,
            GoldBOXConstants.GOLDBOX_APP_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
    }

    /** Update the shown foreground service notification after making any changes that affect it. */
    private synchronized void updateNotification() {
        if (mWakeLock == null && mShellManager.mGoldBOXSessions.isEmpty() && mShellManager.mGoldBOXTasks.isEmpty()) {
            // Exit if we are updating after the user disabled all locks with no sessions or tasks running.
            requestStopService();
        } else {
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(GoldBOXConstants.GOLDBOX_APP_NOTIFICATION_ID, buildNotification());
        }
    }





    private void setCurrentStoredTerminalSession(TerminalSession terminalSession) {
        if (terminalSession == null) return;
        // Make the newly created session the current one to be displayed
        GoldBOXAppSharedPreferences preferences = GoldBOXAppSharedPreferences.build(this);
        if (preferences == null) return;
        preferences.setCurrentSession(terminalSession.mHandle);
    }

    public synchronized boolean isGoldBOXSessionsEmpty() {
        return mShellManager.mGoldBOXSessions.isEmpty();
    }

    public synchronized int getGoldBOXSessionsSize() {
        return mShellManager.mGoldBOXSessions.size();
    }

    public synchronized List<GoldBOXSession> getGoldBOXSessions() {
        return mShellManager.mGoldBOXSessions;
    }

    @Nullable
    public synchronized GoldBOXSession getGoldBOXSession(int index) {
        if (index >= 0 && index < mShellManager.mGoldBOXSessions.size())
            return mShellManager.mGoldBOXSessions.get(index);
        else
            return null;
    }

    @Nullable
    public synchronized GoldBOXSession getGoldBOXSessionForTerminalSession(TerminalSession terminalSession) {
        if (terminalSession == null) return null;

        for (int i = 0; i < mShellManager.mGoldBOXSessions.size(); i++) {
            if (mShellManager.mGoldBOXSessions.get(i).getTerminalSession().equals(terminalSession))
                return mShellManager.mGoldBOXSessions.get(i);
        }

        return null;
    }

    public synchronized GoldBOXSession getLastGoldBOXSession() {
        return mShellManager.mGoldBOXSessions.isEmpty() ? null : mShellManager.mGoldBOXSessions.get(mShellManager.mGoldBOXSessions.size() - 1);
    }

    public synchronized int getIndexOfSession(TerminalSession terminalSession) {
        if (terminalSession == null) return -1;

        for (int i = 0; i < mShellManager.mGoldBOXSessions.size(); i++) {
            if (mShellManager.mGoldBOXSessions.get(i).getTerminalSession().equals(terminalSession))
                return i;
        }
        return -1;
    }

    public synchronized TerminalSession getTerminalSessionForHandle(String sessionHandle) {
        TerminalSession terminalSession;
        for (int i = 0, len = mShellManager.mGoldBOXSessions.size(); i < len; i++) {
            terminalSession = mShellManager.mGoldBOXSessions.get(i).getTerminalSession();
            if (terminalSession.mHandle.equals(sessionHandle))
                return terminalSession;
        }
        return null;
    }

    public synchronized AppShell getGoldBOXTaskForShellName(String name) {
        if (DataUtils.isNullOrEmpty(name)) return null;
        AppShell appShell;
        for (int i = 0, len = mShellManager.mGoldBOXTasks.size(); i < len; i++) {
            appShell = mShellManager.mGoldBOXTasks.get(i);
            String shellName = appShell.getExecutionCommand().shellName;
            if (shellName != null && shellName.equals(name))
                return appShell;
        }
        return null;
    }

    public synchronized GoldBOXSession getGoldBOXSessionForShellName(String name) {
        if (DataUtils.isNullOrEmpty(name)) return null;
        GoldBOXSession goldboxSession;
        for (int i = 0, len = mShellManager.mGoldBOXSessions.size(); i < len; i++) {
            goldboxSession = mShellManager.mGoldBOXSessions.get(i);
            String shellName = goldboxSession.getExecutionCommand().shellName;
            if (shellName != null && shellName.equals(name))
                return goldboxSession;
        }
        return null;
    }



    public boolean wantsToStop() {
        return mWantsToStop;
    }

}
