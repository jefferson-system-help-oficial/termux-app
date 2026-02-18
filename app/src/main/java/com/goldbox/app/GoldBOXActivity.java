package com.goldbox.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.goldbox.R;
import com.goldbox.app.api.file.FileReceiverActivity;
import com.goldbox.app.terminal.GoldBOXActivityRootView;
import com.goldbox.app.terminal.GoldBOXTerminalSessionActivityClient;
import com.goldbox.app.terminal.io.GoldBOXTerminalExtraKeys;
import com.goldbox.shared.activities.ReportActivity;
import com.goldbox.shared.activity.ActivityUtils;
import com.goldbox.shared.activity.media.AppCompatActivityUtils;
import com.goldbox.shared.data.IntentUtils;
import com.goldbox.shared.android.PermissionUtils;
import com.goldbox.shared.data.DataUtils;
import com.goldbox.shared.goldbox.GoldBOXConstants;
import com.goldbox.shared.goldbox.GoldBOXConstants.GOLDBOX_APP.GOLDBOX_ACTIVITY;
import com.goldbox.app.activities.HelpActivity;
import com.goldbox.app.activities.SettingsActivity;
import com.goldbox.shared.goldbox.crash.GoldBOXCrashUtils;
import com.goldbox.shared.goldbox.settings.preferences.GoldBOXAppSharedPreferences;
import com.goldbox.app.activities.WelcomeActivity;
import com.goldbox.app.terminal.GoldBOXSessionsListViewController;
import com.goldbox.app.terminal.io.TerminalToolbarViewPager;
import com.goldbox.app.terminal.GoldBOXTerminalViewClient;
import com.goldbox.shared.goldbox.extrakeys.ExtraKeysView;
import com.goldbox.shared.goldbox.interact.TextInputDialogUtils;
import com.goldbox.shared.logger.Logger;
import com.goldbox.shared.goldbox.GoldBOXUtils;
import com.goldbox.shared.goldbox.settings.properties.GoldBOXAppSharedProperties;
import com.goldbox.shared.goldbox.theme.GoldBOXThemeUtils;
import com.goldbox.shared.theme.NightMode;
import com.goldbox.shared.view.ViewUtils;
import com.goldbox.terminal.TerminalSession;
import com.goldbox.terminal.TerminalSessionClient;
import com.goldbox.view.TerminalView;
import com.goldbox.view.TerminalViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import java.util.Arrays;

/**
 * A terminal emulator activity.
 * <p/>
 * See
 * <ul>
 * <li>http://www.mongrel-phones.com.au/default/how_to_make_a_local_service_and_bind_to_it_in_android</li>
 * <li>https://code.google.com/p/android/issues/detail?id=6426</li>
 * </ul>
 * about memory leaks.
 */
public final class GoldBOXActivity extends AppCompatActivity implements ServiceConnection {

    /**
     * The connection to the {@link GoldBOXService}. Requested in {@link #onCreate(Bundle)} with a call to
     * {@link #bindService(Intent, ServiceConnection, int)}, and obtained and stored in
     * {@link #onServiceConnected(ComponentName, IBinder)}.
     */
    GoldBOXService mGoldBOXService;

    /**
     * The {@link TerminalView} shown in  {@link GoldBOXActivity} that displays the terminal.
     */
    TerminalView mTerminalView;

    /**
     *  The {@link TerminalViewClient} interface implementation to allow for communication between
     *  {@link TerminalView} and {@link GoldBOXActivity}.
     */
    GoldBOXTerminalViewClient mGoldBOXTerminalViewClient;

    /**
     *  The {@link TerminalSessionClient} interface implementation to allow for communication between
     *  {@link TerminalSession} and {@link GoldBOXActivity}.
     */
    GoldBOXTerminalSessionActivityClient mGoldBOXTerminalSessionActivityClient;

    /**
     * GoldBOX app shared preferences manager.
     */
    private GoldBOXAppSharedPreferences mPreferences;

    /**
     * GoldBOX app SharedProperties loaded from goldbox.properties
     */
    private GoldBOXAppSharedProperties mProperties;

    /**
     * The root view of the {@link GoldBOXActivity}.
     */
    GoldBOXActivityRootView mGoldBOXActivityRootView;

    /**
     * The space at the bottom of {@link @mGoldBOXActivityRootView} of the {@link GoldBOXActivity}.
     */
    View mGoldBOXActivityBottomSpaceView;

    /**
     * The terminal extra keys view.
     */
    ExtraKeysView mExtraKeysView;

    /**
     * The client for the {@link #mExtraKeysView}.
     */
    GoldBOXTerminalExtraKeys mGoldBOXTerminalExtraKeys;

    /**
     * The goldbox sessions list controller.
     */
    GoldBOXSessionsListViewController mGoldBOXSessionListViewController;

    /**
     * The {@link GoldBOXActivity} broadcast receiver for various things like terminal style configuration changes.
     */
    private final BroadcastReceiver mGoldBOXActivityBroadcastReceiver = new GoldBOXActivityBroadcastReceiver();

    /**
     * The last toast shown, used cancel current toast before showing new in {@link #showToast(String, boolean)}.
     */
    Toast mLastToast;

    /**
     * If between onResume() and onStop(). Note that only one session is in the foreground of the terminal view at the
     * time, so if the session causing a change is not in the foreground it should probably be treated as background.
     */
    private boolean mIsVisible;

    /**
     * If onResume() was called after onCreate().
     */
    private boolean mIsOnResumeAfterOnCreate = false;

    /**
     * If activity was restarted like due to call to {@link #recreate()} after receiving
     * {@link GOLDBOX_ACTIVITY#ACTION_RELOAD_STYLE}, system dark night mode was changed or activity
     * was killed by android.
     */
    private boolean mIsActivityRecreated = false;

    /**
     * The {@link GoldBOXActivity} is in an invalid state and must not be run.
     */
    private boolean mIsInvalidState;

    private int mNavBarHeight;

    private float mTerminalToolbarDefaultHeight;


    private static final int CONTEXT_MENU_SELECT_URL_ID = 0;
    private static final int CONTEXT_MENU_SHARE_TRANSCRIPT_ID = 1;
    private static final int CONTEXT_MENU_SHARE_SELECTED_TEXT = 10;
    private static final int CONTEXT_MENU_AUTOFILL_USERNAME = 11;
    private static final int CONTEXT_MENU_AUTOFILL_PASSWORD = 2;
    private static final int CONTEXT_MENU_RESET_TERMINAL_ID = 3;
    private static final int CONTEXT_MENU_KILL_PROCESS_ID = 4;
    private static final int CONTEXT_MENU_STYLING_ID = 5;
    private static final int CONTEXT_MENU_TOGGLE_KEEP_SCREEN_ON = 6;
    private static final int CONTEXT_MENU_HELP_ID = 7;
    private static final int CONTEXT_MENU_SETTINGS_ID = 8;
    private static final int CONTEXT_MENU_REPORT_ID = 9;

    private static final String ARG_TERMINAL_TOOLBAR_TEXT_INPUT = "terminal_toolbar_text_input";
    private static final String ARG_ACTIVITY_RECREATED = "activity_recreated";

    private static final String LOG_TAG = "GoldBOXActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logger.logDebug(LOG_TAG, "onCreate");
        mIsOnResumeAfterOnCreate = true;

        if (savedInstanceState != null)
            mIsActivityRecreated = savedInstanceState.getBoolean(ARG_ACTIVITY_RECREATED, false);

        // Delete ReportInfo serialized object files from cache older than 14 days
        ReportActivity.deleteReportInfoFilesOlderThanXDays(this, 14, false);

        // Load GoldBOX app SharedProperties from disk
        mProperties = GoldBOXAppSharedProperties.getProperties();
        reloadProperties();

        setActivityTheme();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_goldbox);

        // Load goldbox shared preferences
        // This will also fail if GoldBOXConstants.GOLDBOX_PACKAGE_NAME does not equal applicationId
        mPreferences = GoldBOXAppSharedPreferences.build(this, true);
        if (mPreferences == null) {
            // An AlertDialog should have shown to kill the app, so we don't continue running activity code
            mIsInvalidState = true;
            return;
        }

        if (mPreferences.shouldShowWelcomeScreens()) {
            ActivityUtils.startActivity(this, new Intent(this, WelcomeActivity.class));
        }

        setMargins();

        mGoldBOXActivityRootView = findViewById(R.id.activity_goldbox_root_view);
        mGoldBOXActivityRootView.setActivity(this);
        mGoldBOXActivityBottomSpaceView = findViewById(R.id.activity_goldbox_bottom_space_view);
        mGoldBOXActivityRootView.setOnApplyWindowInsetsListener(new GoldBOXActivityRootView.WindowInsetsListener());

        View content = findViewById(android.R.id.content);
        content.setOnApplyWindowInsetsListener((v, insets) -> {
            mNavBarHeight = insets.getSystemWindowInsetBottom();
            return insets;
        });

        if (mProperties.isUsingFullScreen()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setGoldBOXTerminalViewAndClients();

        setTerminalToolbarView(savedInstanceState);

        setSettingsButtonView();

        setNewSessionButtonView();

        setToggleKeyboardView();

        registerForContextMenu(mTerminalView);

        FileReceiverActivity.updateFileReceiverActivityComponentsState(this);

        try {
            // Start the {@link GoldBOXService} and make it run regardless of who is bound to it
            Intent serviceIntent = new Intent(this, GoldBOXService.class);
            startService(serviceIntent);

            // Attempt to bind to the service, this will call the {@link #onServiceConnected(ComponentName, IBinder)}
            // callback if it succeeds.
            if (!bindService(serviceIntent, this, 0))
                throw new RuntimeException("bindService() failed");
        } catch (Exception e) {
            Logger.logStackTraceWithMessage(LOG_TAG,"GoldBOXActivity failed to start GoldBOXService", e);
            Logger.showToast(this,
                getString(e.getMessage() != null && e.getMessage().contains("app is in background") ?
                    R.string.error_goldbox_service_start_failed_bg : R.string.error_goldbox_service_start_failed_general),
                true);
            mIsInvalidState = true;
            return;
        }

        // Send the {@link GoldBOXConstants#BROADCAST_GOLDBOX_OPENED} broadcast to notify apps that GoldBOX
        // app has been opened.
        GoldBOXUtils.sendGoldBOXOpenedBroadcast(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        Logger.logDebug(LOG_TAG, "onStart");

        if (mIsInvalidState) return;

        mIsVisible = true;

        if (mGoldBOXTerminalSessionActivityClient != null)
            mGoldBOXTerminalSessionActivityClient.onStart();

        if (mGoldBOXTerminalViewClient != null)
            mGoldBOXTerminalViewClient.onStart();

        if (mPreferences.isTerminalMarginAdjustmentEnabled())
            addGoldBOXActivityRootViewGlobalLayoutListener();

        registerGoldBOXActivityBroadcastReceiver();
    }

    @Override
    public void onResume() {
        super.onResume();

        Logger.logVerbose(LOG_TAG, "onResume");

        if (mIsInvalidState) return;

        if (mGoldBOXTerminalSessionActivityClient != null)
            mGoldBOXTerminalSessionActivityClient.onResume();

        if (mGoldBOXTerminalViewClient != null)
            mGoldBOXTerminalViewClient.onResume();

        // Check if a crash happened on last run of the app or if a plugin crashed and show a
        // notification with the crash details if it did
        GoldBOXCrashUtils.notifyAppCrashFromCrashLogFile(this, LOG_TAG);

        mIsOnResumeAfterOnCreate = false;
    }

    @Override
    protected void onStop() {
        super.onStop();

        Logger.logDebug(LOG_TAG, "onStop");

        if (mIsInvalidState) return;

        mIsVisible = false;

        if (mGoldBOXTerminalSessionActivityClient != null)
            mGoldBOXTerminalSessionActivityClient.onStop();

        if (mGoldBOXTerminalViewClient != null)
            mGoldBOXTerminalViewClient.onStop();

        removeGoldBOXActivityRootViewGlobalLayoutListener();

        unregisterGoldBOXActivityBroadcastReceiver();
        getDrawer().closeDrawers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Logger.logDebug(LOG_TAG, "onDestroy");

        if (mIsInvalidState) return;

        if (mGoldBOXService != null) {
            // Do not leave service and session clients with references to activity.
            mGoldBOXService.unsetGoldBOXTerminalSessionClient();
            mGoldBOXService = null;
        }

        try {
            unbindService(this);
        } catch (Exception e) {
            // ignore.
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        Logger.logVerbose(LOG_TAG, "onSaveInstanceState");

        super.onSaveInstanceState(savedInstanceState);
        saveTerminalToolbarTextInput(savedInstanceState);
        savedInstanceState.putBoolean(ARG_ACTIVITY_RECREATED, true);
    }





    /**
     * Part of the {@link ServiceConnection} interface. The service is bound with
     * {@link #bindService(Intent, ServiceConnection, int)} in {@link #onCreate(Bundle)} which will cause a call to this
     * callback method.
     */
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        Logger.logDebug(LOG_TAG, "onServiceConnected");

        mGoldBOXService = ((GoldBOXService.LocalBinder) service).service;

        setGoldBOXSessionsListView();

        final Intent intent = getIntent();
        setIntent(null);

        if (mGoldBOXService.isGoldBOXSessionsEmpty()) {
            if (mIsVisible) {
                GoldBOXInstaller.setupBootstrapIfNeeded(GoldBOXActivity.this, () -> {
                    if (mGoldBOXService == null) return; // Activity might have been destroyed.
                    try {
                        boolean launchFailsafe = false;
                        if (intent != null && intent.getExtras() != null) {
                            launchFailsafe = intent.getExtras().getBoolean(GOLDBOX_ACTIVITY.EXTRA_FAILSAFE_SESSION, false);
                        }
                        mGoldBOXTerminalSessionActivityClient.addNewSession(launchFailsafe, null);
                    } catch (WindowManager.BadTokenException e) {
                        // Activity finished - ignore.
                    }
                });
            } else {
                // The service connected while not in foreground - just bail out.
                finishActivityIfNotFinishing();
            }
        } else {
            // If goldbox was started from launcher "New session" shortcut and activity is recreated,
            // then the original intent will be re-delivered, resulting in a new session being re-added
            // each time.
            if (!mIsActivityRecreated && intent != null && Intent.ACTION_RUN.equals(intent.getAction())) {
                // Android 7.1 app shortcut from res/xml/shortcuts.xml.
                boolean isFailSafe = intent.getBooleanExtra(GOLDBOX_ACTIVITY.EXTRA_FAILSAFE_SESSION, false);
                mGoldBOXTerminalSessionActivityClient.addNewSession(isFailSafe, null);
            } else {
                mGoldBOXTerminalSessionActivityClient.setCurrentSession(mGoldBOXTerminalSessionActivityClient.getCurrentStoredSessionOrLast());
            }
        }

        // Update the {@link TerminalSession} and {@link TerminalEmulator} clients.
        mGoldBOXService.setGoldBOXTerminalSessionClient(mGoldBOXTerminalSessionActivityClient);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Logger.logDebug(LOG_TAG, "onServiceDisconnected");

        // Respect being stopped from the {@link GoldBOXService} notification action.
        finishActivityIfNotFinishing();
    }






    private void reloadProperties() {
        mProperties.loadGoldBOXPropertiesFromDisk();

        if (mGoldBOXTerminalViewClient != null)
            mGoldBOXTerminalViewClient.onReloadProperties();
    }



    private void setActivityTheme() {
        // Update NightMode.APP_NIGHT_MODE
        GoldBOXThemeUtils.setAppNightMode(mProperties.getNightMode());

        // Set activity night mode. If NightMode.SYSTEM is set, then android will automatically
        // trigger recreation of activity when uiMode/dark mode configuration is changed so that
        // day or night theme takes affect.
        AppCompatActivityUtils.setNightMode(this, NightMode.getAppNightMode().getName(), true);
    }

    private void setMargins() {
        RelativeLayout relativeLayout = findViewById(R.id.activity_goldbox_root_relative_layout);
        int marginHorizontal = mProperties.getTerminalMarginHorizontal();
        int marginVertical = mProperties.getTerminalMarginVertical();
        ViewUtils.setLayoutMarginsInDp(relativeLayout, marginHorizontal, marginVertical, marginHorizontal, marginVertical);
    }



    public void addGoldBOXActivityRootViewGlobalLayoutListener() {
        getGoldBOXActivityRootView().getViewTreeObserver().addOnGlobalLayoutListener(getGoldBOXActivityRootView());
    }

    public void removeGoldBOXActivityRootViewGlobalLayoutListener() {
        if (getGoldBOXActivityRootView() != null)
            getGoldBOXActivityRootView().getViewTreeObserver().removeOnGlobalLayoutListener(getGoldBOXActivityRootView());
    }



    private void setGoldBOXTerminalViewAndClients() {
        // Set goldbox terminal view and session clients
        mGoldBOXTerminalSessionActivityClient = new GoldBOXTerminalSessionActivityClient(this);
        mGoldBOXTerminalViewClient = new GoldBOXTerminalViewClient(this, mGoldBOXTerminalSessionActivityClient);

        // Set goldbox terminal view
        mTerminalView = findViewById(R.id.terminal_view);
        mTerminalView.setTerminalViewClient(mGoldBOXTerminalViewClient);

        if (mGoldBOXTerminalViewClient != null)
            mGoldBOXTerminalViewClient.onCreate();

        if (mGoldBOXTerminalSessionActivityClient != null)
            mGoldBOXTerminalSessionActivityClient.onCreate();
    }

    private void setGoldBOXSessionsListView() {
        ListView goldboxSessionsListView = findViewById(R.id.terminal_sessions_list);
        mGoldBOXSessionListViewController = new GoldBOXSessionsListViewController(this, mGoldBOXService.getGoldBOXSessions());
        goldboxSessionsListView.setAdapter(mGoldBOXSessionListViewController);
        goldboxSessionsListView.setOnItemClickListener(mGoldBOXSessionListViewController);
        goldboxSessionsListView.setOnItemLongClickListener(mGoldBOXSessionListViewController);
    }



    private void setTerminalToolbarView(Bundle savedInstanceState) {
        mGoldBOXTerminalExtraKeys = new GoldBOXTerminalExtraKeys(this, mTerminalView,
            mGoldBOXTerminalViewClient, mGoldBOXTerminalSessionActivityClient);

        final ViewPager terminalToolbarViewPager = getTerminalToolbarViewPager();
        if (mPreferences.shouldShowTerminalToolbar()) terminalToolbarViewPager.setVisibility(View.VISIBLE);

        ViewGroup.LayoutParams layoutParams = terminalToolbarViewPager.getLayoutParams();
        mTerminalToolbarDefaultHeight = layoutParams.height;

        setTerminalToolbarHeight();

        String savedTextInput = null;
        if (savedInstanceState != null)
            savedTextInput = savedInstanceState.getString(ARG_TERMINAL_TOOLBAR_TEXT_INPUT);

        terminalToolbarViewPager.setAdapter(new TerminalToolbarViewPager.PageAdapter(this, savedTextInput));
        terminalToolbarViewPager.addOnPageChangeListener(new TerminalToolbarViewPager.OnPageChangeListener(this, terminalToolbarViewPager));
    }

    private void setTerminalToolbarHeight() {
        final ViewPager terminalToolbarViewPager = getTerminalToolbarViewPager();
        if (terminalToolbarViewPager == null) return;

        ViewGroup.LayoutParams layoutParams = terminalToolbarViewPager.getLayoutParams();
        layoutParams.height = Math.round(mTerminalToolbarDefaultHeight *
            (mGoldBOXTerminalExtraKeys.getExtraKeysInfo() == null ? 0 : mGoldBOXTerminalExtraKeys.getExtraKeysInfo().getMatrix().length) *
            mProperties.getTerminalToolbarHeightScaleFactor());
        terminalToolbarViewPager.setLayoutParams(layoutParams);
    }

    public void toggleTerminalToolbar() {
        final ViewPager terminalToolbarViewPager = getTerminalToolbarViewPager();
        if (terminalToolbarViewPager == null) return;

        final boolean showNow = mPreferences.toogleShowTerminalToolbar();
        Logger.showToast(this, (showNow ? getString(R.string.msg_enabling_terminal_toolbar) : getString(R.string.msg_disabling_terminal_toolbar)), true);
        terminalToolbarViewPager.setVisibility(showNow ? View.VISIBLE : View.GONE);
        if (showNow && isTerminalToolbarTextInputViewSelected()) {
            // Focus the text input view if just revealed.
            findViewById(R.id.terminal_toolbar_text_input).requestFocus();
        }
    }

    private void saveTerminalToolbarTextInput(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;

        final EditText textInputView = findViewById(R.id.terminal_toolbar_text_input);
        if (textInputView != null) {
            String textInput = textInputView.getText().toString();
            if (!textInput.isEmpty()) savedInstanceState.putString(ARG_TERMINAL_TOOLBAR_TEXT_INPUT, textInput);
        }
    }



    private void setSettingsButtonView() {
        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> {
            ActivityUtils.startActivity(this, new Intent(this, SettingsActivity.class));
        });
    }

    private void setNewSessionButtonView() {
        View newSessionButton = findViewById(R.id.new_session_button);
        newSessionButton.setOnClickListener(v -> mGoldBOXTerminalSessionActivityClient.addNewSession(false, null));
        newSessionButton.setOnLongClickListener(v -> {
            TextInputDialogUtils.textInput(GoldBOXActivity.this, R.string.title_create_named_session, null,
                R.string.action_create_named_session_confirm, text -> mGoldBOXTerminalSessionActivityClient.addNewSession(false, text),
                R.string.action_new_session_failsafe, text -> mGoldBOXTerminalSessionActivityClient.addNewSession(true, text),
                -1, null, null);
            return true;
        });
    }

    private void setToggleKeyboardView() {
        findViewById(R.id.toggle_keyboard_button).setOnClickListener(v -> {
            mGoldBOXTerminalViewClient.onToggleSoftKeyboardRequest();
            getDrawer().closeDrawers();
        });

        findViewById(R.id.toggle_keyboard_button).setOnLongClickListener(v -> {
            toggleTerminalToolbar();
            return true;
        });
    }





    @SuppressLint("RtlHardcoded")
    @Override
    public void onBackPressed() {
        if (getDrawer().isDrawerOpen(Gravity.LEFT)) {
            getDrawer().closeDrawers();
        } else {
            finishActivityIfNotFinishing();
        }
    }

    public void finishActivityIfNotFinishing() {
        // prevent duplicate calls to finish() if called from multiple places
        if (!GoldBOXActivity.this.isFinishing()) {
            finish();
        }
    }

    /** Show a toast and dismiss the last one if still visible. */
    public void showToast(String text, boolean longDuration) {
        if (text == null || text.isEmpty()) return;
        if (mLastToast != null) mLastToast.cancel();
        mLastToast = Toast.makeText(GoldBOXActivity.this, text, longDuration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        mLastToast.setGravity(Gravity.TOP, 0, 0);
        mLastToast.show();
    }



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        TerminalSession currentSession = getCurrentSession();
        if (currentSession == null) return;

        boolean autoFillEnabled = mTerminalView.isAutoFillEnabled();

        menu.add(Menu.NONE, CONTEXT_MENU_SELECT_URL_ID, Menu.NONE, R.string.action_select_url);
        menu.add(Menu.NONE, CONTEXT_MENU_SHARE_TRANSCRIPT_ID, Menu.NONE, R.string.action_share_transcript);
        if (!DataUtils.isNullOrEmpty(mTerminalView.getStoredSelectedText()))
            menu.add(Menu.NONE, CONTEXT_MENU_SHARE_SELECTED_TEXT, Menu.NONE, R.string.action_share_selected_text);
        if (autoFillEnabled)
            menu.add(Menu.NONE, CONTEXT_MENU_AUTOFILL_USERNAME, Menu.NONE, R.string.action_autofill_username);
        if (autoFillEnabled)
            menu.add(Menu.NONE, CONTEXT_MENU_AUTOFILL_PASSWORD, Menu.NONE, R.string.action_autofill_password);
        menu.add(Menu.NONE, CONTEXT_MENU_RESET_TERMINAL_ID, Menu.NONE, R.string.action_reset_terminal);
        menu.add(Menu.NONE, CONTEXT_MENU_KILL_PROCESS_ID, Menu.NONE, getResources().getString(R.string.action_kill_process, getCurrentSession().getPid())).setEnabled(currentSession.isRunning());
        menu.add(Menu.NONE, CONTEXT_MENU_STYLING_ID, Menu.NONE, R.string.action_style_terminal);
        menu.add(Menu.NONE, CONTEXT_MENU_TOGGLE_KEEP_SCREEN_ON, Menu.NONE, R.string.action_toggle_keep_screen_on).setCheckable(true).setChecked(mPreferences.shouldKeepScreenOn());
        menu.add(Menu.NONE, CONTEXT_MENU_HELP_ID, Menu.NONE, R.string.action_open_help);
        menu.add(Menu.NONE, CONTEXT_MENU_SETTINGS_ID, Menu.NONE, R.string.action_open_settings);
        menu.add(Menu.NONE, CONTEXT_MENU_REPORT_ID, Menu.NONE, R.string.action_report_issue);
    }

    /** Hook system menu to show context menu instead. */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mTerminalView.showContextMenu();
        return false;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        TerminalSession session = getCurrentSession();

        switch (item.getItemId()) {
            case CONTEXT_MENU_SELECT_URL_ID:
                mGoldBOXTerminalViewClient.showUrlSelection();
                return true;
            case CONTEXT_MENU_SHARE_TRANSCRIPT_ID:
                mGoldBOXTerminalViewClient.shareSessionTranscript();
                return true;
            case CONTEXT_MENU_SHARE_SELECTED_TEXT:
                mGoldBOXTerminalViewClient.shareSelectedText();
                return true;
            case CONTEXT_MENU_AUTOFILL_USERNAME:
                mTerminalView.requestAutoFillUsername();
                return true;
            case CONTEXT_MENU_AUTOFILL_PASSWORD:
                mTerminalView.requestAutoFillPassword();
                return true;
            case CONTEXT_MENU_RESET_TERMINAL_ID:
                onResetTerminalSession(session);
                return true;
            case CONTEXT_MENU_KILL_PROCESS_ID:
                showKillSessionDialog(session);
                return true;
            case CONTEXT_MENU_STYLING_ID:
                showStylingDialog();
                return true;
            case CONTEXT_MENU_TOGGLE_KEEP_SCREEN_ON:
                toggleKeepScreenOn();
                return true;
            case CONTEXT_MENU_HELP_ID:
                ActivityUtils.startActivity(this, new Intent(this, HelpActivity.class));
                return true;
            case CONTEXT_MENU_SETTINGS_ID:
                ActivityUtils.startActivity(this, new Intent(this, SettingsActivity.class));
                return true;
            case CONTEXT_MENU_REPORT_ID:
                mGoldBOXTerminalViewClient.reportIssueFromTranscript();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
        // onContextMenuClosed() is triggered twice if back button is pressed to dismiss instead of tap for some reason
        mTerminalView.onContextMenuClosed(menu);
    }

    private void showKillSessionDialog(TerminalSession session) {
        if (session == null) return;

        final AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.setMessage(R.string.title_confirm_kill_process);
        b.setPositiveButton(android.R.string.yes, (dialog, id) -> {
            dialog.dismiss();
            session.finishIfRunning();
        });
        b.setNegativeButton(android.R.string.no, null);
        b.show();
    }

    private void onResetTerminalSession(TerminalSession session) {
        if (session != null) {
            session.reset();
            showToast(getResources().getString(R.string.msg_terminal_reset), true);

            if (mGoldBOXTerminalSessionActivityClient != null)
                mGoldBOXTerminalSessionActivityClient.onResetTerminalSession();
        }
    }

    private void showStylingDialog() {
        Intent stylingIntent = new Intent();
        stylingIntent.setClassName(GoldBOXConstants.GOLDBOX_STYLING_PACKAGE_NAME, GoldBOXConstants.GOLDBOX_STYLING_APP.GOLDBOX_STYLING_ACTIVITY_NAME);
        try {
            startActivity(stylingIntent);
        } catch (ActivityNotFoundException | IllegalArgumentException e) {
            // The startActivity() call is not documented to throw IllegalArgumentException.
            // However, crash reporting shows that it sometimes does, so catch it here.
            new AlertDialog.Builder(this).setMessage(getString(R.string.error_styling_not_installed))
                .setPositiveButton(R.string.action_styling_install,
                    (dialog, which) -> ActivityUtils.startActivity(this, new Intent(Intent.ACTION_VIEW, Uri.parse(GoldBOXConstants.GOLDBOX_STYLING_FDROID_PACKAGE_URL))))
                .setNegativeButton(android.R.string.cancel, null).show();
        }
    }
    private void toggleKeepScreenOn() {
        if (mTerminalView.getKeepScreenOn()) {
            mTerminalView.setKeepScreenOn(false);
            mPreferences.setKeepScreenOn(false);
        } else {
            mTerminalView.setKeepScreenOn(true);
            mPreferences.setKeepScreenOn(true);
        }
    }



    /**
     * For processes to access primary external storage (/sdcard, /storage/emulated/0, ~/storage/shared),
     * goldbox needs to be granted legacy WRITE_EXTERNAL_STORAGE or MANAGE_EXTERNAL_STORAGE permissions
     * if targeting targetSdkVersion 30 (android 11) and running on sdk 30 (android 11) and higher.
     */
    public void requestStoragePermission(boolean isPermissionCallback) {
        new Thread() {
            @Override
            public void run() {
                // Do not ask for permission again
                int requestCode = isPermissionCallback ? -1 : PermissionUtils.REQUEST_GRANT_STORAGE_PERMISSION;

                // If permission is granted, then also setup storage symlinks.
                if(PermissionUtils.checkAndRequestLegacyOrManageExternalStoragePermission(
                    GoldBOXActivity.this, requestCode, !isPermissionCallback)) {
                    if (isPermissionCallback)
                        Logger.logInfoAndShowToast(GoldBOXActivity.this, LOG_TAG,
                            getString(com.goldbox.shared.R.string.msg_storage_permission_granted_on_request));

                    GoldBOXInstaller.setupStorageSymlinks(GoldBOXActivity.this);
                } else {
                    if (isPermissionCallback)
                        Logger.logInfoAndShowToast(GoldBOXActivity.this, LOG_TAG,
                            getString(com.goldbox.shared.R.string.msg_storage_permission_not_granted_on_request));
                }
            }
        }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.logVerbose(LOG_TAG, "onActivityResult: requestCode: " + requestCode + ", resultCode: "  + resultCode + ", data: "  + IntentUtils.getIntentString(data));
        if (requestCode == PermissionUtils.REQUEST_GRANT_STORAGE_PERMISSION) {
            requestStoragePermission(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Logger.logVerbose(LOG_TAG, "onRequestPermissionsResult: requestCode: " + requestCode + ", permissions: "  + Arrays.toString(permissions) + ", grantResults: "  + Arrays.toString(grantResults));
        if (requestCode == PermissionUtils.REQUEST_GRANT_STORAGE_PERMISSION) {
            requestStoragePermission(true);
        }
    }



    public int getNavBarHeight() {
        return mNavBarHeight;
    }

    public GoldBOXActivityRootView getGoldBOXActivityRootView() {
        return mGoldBOXActivityRootView;
    }

    public View getGoldBOXActivityBottomSpaceView() {
        return mGoldBOXActivityBottomSpaceView;
    }

    public ExtraKeysView getExtraKeysView() {
        return mExtraKeysView;
    }

    public GoldBOXTerminalExtraKeys getGoldBOXTerminalExtraKeys() {
        return mGoldBOXTerminalExtraKeys;
    }

    public void setExtraKeysView(ExtraKeysView extraKeysView) {
        mExtraKeysView = extraKeysView;
    }

    public DrawerLayout getDrawer() {
        return (DrawerLayout) findViewById(R.id.drawer_layout);
    }


    public ViewPager getTerminalToolbarViewPager() {
        return (ViewPager) findViewById(R.id.terminal_toolbar_view_pager);
    }

    public float getTerminalToolbarDefaultHeight() {
        return mTerminalToolbarDefaultHeight;
    }

    public boolean isTerminalViewSelected() {
        return getTerminalToolbarViewPager().getCurrentItem() == 0;
    }

    public boolean isTerminalToolbarTextInputViewSelected() {
        return getTerminalToolbarViewPager().getCurrentItem() == 1;
    }


    public void goldboxSessionListNotifyUpdated() {
        mGoldBOXSessionListViewController.notifyDataSetChanged();
    }

    public boolean isVisible() {
        return mIsVisible;
    }

    public boolean isOnResumeAfterOnCreate() {
        return mIsOnResumeAfterOnCreate;
    }

    public boolean isActivityRecreated() {
        return mIsActivityRecreated;
    }



    public GoldBOXService getGoldBOXService() {
        return mGoldBOXService;
    }

    public TerminalView getTerminalView() {
        return mTerminalView;
    }

    public GoldBOXTerminalViewClient getGoldBOXTerminalViewClient() {
        return mGoldBOXTerminalViewClient;
    }

    public GoldBOXTerminalSessionActivityClient getGoldBOXTerminalSessionClient() {
        return mGoldBOXTerminalSessionActivityClient;
    }

    @Nullable
    public TerminalSession getCurrentSession() {
        if (mTerminalView != null)
            return mTerminalView.getCurrentSession();
        else
            return null;
    }

    public GoldBOXAppSharedPreferences getPreferences() {
        return mPreferences;
    }

    public GoldBOXAppSharedProperties getProperties() {
        return mProperties;
    }




    public static void updateGoldBOXActivityStyling(Context context, boolean recreateActivity) {
        // Make sure that terminal styling is always applied.
        Intent stylingIntent = new Intent(GOLDBOX_ACTIVITY.ACTION_RELOAD_STYLE);
        stylingIntent.putExtra(GOLDBOX_ACTIVITY.EXTRA_RECREATE_ACTIVITY, recreateActivity);
        context.sendBroadcast(stylingIntent);
    }

    private void registerGoldBOXActivityBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GOLDBOX_ACTIVITY.ACTION_NOTIFY_APP_CRASH);
        intentFilter.addAction(GOLDBOX_ACTIVITY.ACTION_RELOAD_STYLE);
        intentFilter.addAction(GOLDBOX_ACTIVITY.ACTION_REQUEST_PERMISSIONS);

        registerReceiver(mGoldBOXActivityBroadcastReceiver, intentFilter);
    }

    private void unregisterGoldBOXActivityBroadcastReceiver() {
        unregisterReceiver(mGoldBOXActivityBroadcastReceiver);
    }

    private void fixGoldBOXActivityBroadcastReceiverIntent(Intent intent) {
        if (intent == null) return;

        String extraReloadStyle = intent.getStringExtra(GOLDBOX_ACTIVITY.EXTRA_RELOAD_STYLE);
        if ("storage".equals(extraReloadStyle)) {
            intent.removeExtra(GOLDBOX_ACTIVITY.EXTRA_RELOAD_STYLE);
            intent.setAction(GOLDBOX_ACTIVITY.ACTION_REQUEST_PERMISSIONS);
        }
    }

    class GoldBOXActivityBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;

            if (mIsVisible) {
                fixGoldBOXActivityBroadcastReceiverIntent(intent);

                switch (intent.getAction()) {
                    case GOLDBOX_ACTIVITY.ACTION_NOTIFY_APP_CRASH:
                        Logger.logDebug(LOG_TAG, "Received intent to notify app crash");
                        GoldBOXCrashUtils.notifyAppCrashFromCrashLogFile(context, LOG_TAG);
                        return;
                    case GOLDBOX_ACTIVITY.ACTION_RELOAD_STYLE:
                        Logger.logDebug(LOG_TAG, "Received intent to reload styling");
                        reloadActivityStyling(intent.getBooleanExtra(GOLDBOX_ACTIVITY.EXTRA_RECREATE_ACTIVITY, true));
                        return;
                    case GOLDBOX_ACTIVITY.ACTION_REQUEST_PERMISSIONS:
                        Logger.logDebug(LOG_TAG, "Received intent to request storage permissions");
                        requestStoragePermission(false);
                        return;
                    default:
                }
            }
        }
    }

    private void reloadActivityStyling(boolean recreateActivity) {
        if (mProperties != null) {
            reloadProperties();

            if (mExtraKeysView != null) {
                mExtraKeysView.setButtonTextAllCaps(mProperties.shouldExtraKeysTextBeAllCaps());
                mExtraKeysView.reload(mGoldBOXTerminalExtraKeys.getExtraKeysInfo(), mTerminalToolbarDefaultHeight);
            }

            // Update NightMode.APP_NIGHT_MODE
            GoldBOXThemeUtils.setAppNightMode(mProperties.getNightMode());
        }

        setMargins();
        setTerminalToolbarHeight();

        FileReceiverActivity.updateFileReceiverActivityComponentsState(this);

        if (mGoldBOXTerminalSessionActivityClient != null)
            mGoldBOXTerminalSessionActivityClient.onReloadActivityStyling();

        if (mGoldBOXTerminalViewClient != null)
            mGoldBOXTerminalViewClient.onReloadActivityStyling();

        // To change the activity and drawer theme, activity needs to be recreated.
        // It will destroy the activity, including all stored variables and views, and onCreate()
        // will be called again. Extra keys input text, terminal sessions and transcripts will be preserved.
        if (recreateActivity) {
            Logger.logDebug(LOG_TAG, "Recreating activity");
            GoldBOXActivity.this.recreate();
        }
    }



    public static void startGoldBOXActivity(@NonNull final Context context) {
        ActivityUtils.startActivity(context, newInstance(context));
    }

    public static Intent newInstance(@NonNull final Context context) {
        Intent intent = new Intent(context, GoldBOXActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

}
