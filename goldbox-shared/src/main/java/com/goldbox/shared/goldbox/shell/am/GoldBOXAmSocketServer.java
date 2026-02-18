package com.goldbox.shared.goldbox.shell.am;

import android.content.Context;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.goldbox.shared.errors.Error;
import com.goldbox.shared.logger.Logger;
import com.goldbox.shared.net.socket.local.LocalClientSocket;
import com.goldbox.shared.net.socket.local.LocalServerSocket;
import com.goldbox.shared.net.socket.local.LocalSocketManager;
import com.goldbox.shared.net.socket.local.LocalSocketManagerClientBase;
import com.goldbox.shared.net.socket.local.LocalSocketRunConfig;
import com.goldbox.shared.shell.am.AmSocketServerRunConfig;
import com.goldbox.shared.shell.am.AmSocketServer;
import com.goldbox.shared.goldbox.GoldBOXConstants;
import com.goldbox.shared.goldbox.crash.GoldBOXCrashUtils;
import com.goldbox.shared.goldbox.plugins.GoldBOXPluginUtils;
import com.goldbox.shared.goldbox.settings.properties.GoldBOXAppSharedProperties;
import com.goldbox.shared.goldbox.settings.properties.GoldBOXPropertyConstants;
import com.goldbox.shared.goldbox.shell.command.environment.GoldBOXAppShellEnvironment;

/**
 * A wrapper for {@link AmSocketServer} for goldbox-app usage.
 *
 * The static {@link #goldboxAmSocketServer} variable stores the {@link LocalSocketManager} for the
 * {@link AmSocketServer}.
 *
 * The {@link GoldBOXAmSocketServerClient} extends the {@link AmSocketServer.AmSocketServerClient}
 * class to also show plugin error notifications for errors and disallowed client connections in
 * addition to logging the messages to logcat, which are only logged by {@link LocalSocketManagerClientBase}
 * if log level is debug or higher for privacy issues.
 *
 * It uses a filesystem socket server with the socket file at
 * {@link GoldBOXConstants.GOLDBOX_APP#GOLDBOX_AM_SOCKET_FILE_PATH}. It would normally only allow
 * processes belonging to the goldbox user and root user to connect to it. If commands are sent by the
 * root user, then the am commands executed will be run as the goldbox user and its permissions,
 * capabilities and selinux context instead of root.
 *
 * The `$PREFIX/bin/goldbox-am` client connects to the server via `$PREFIX/bin/goldbox-am-socket` to
 * run the am commands. It provides similar functionality to "$PREFIX/bin/am"
 * (and "/system/bin/am"), but should be faster since it does not require starting a dalvik vm for
 * every command as done by "am" via goldbox/GoldBOXAm.
 *
 * The server is started by goldbox-app Application class but is not started if
 * {@link GoldBOXPropertyConstants#KEY_RUN_GOLDBOX_AM_SOCKET_SERVER} is `false` which can be done by
 * adding the prop with value "false" to the "~/.goldbox/goldbox.properties" file. Changes
 * require goldbox-app to be force stopped and restarted.
 *
 * The current state of the server can be checked with the
 * {@link GoldBOXAppShellEnvironment#ENV_GOLDBOX_APP__AM_SOCKET_SERVER_ENABLED} env variable, which is exported
 * for all shell sessions and tasks.
 *
 * https://github.com.goldbox/goldbox-am-socket
 * https://github.com.goldbox/GoldBOXAm
 */
public class GoldBOXAmSocketServer {

    public static final String LOG_TAG = "GoldBOXAmSocketServer";

    public static final String TITLE = "GoldBOXAm";

    /** The static instance for the {@link GoldBOXAmSocketServer} {@link LocalSocketManager}. */
    private static LocalSocketManager goldboxAmSocketServer;

    /** Whether {@link GoldBOXAmSocketServer} is enabled and running or not. */
    @Keep
    protected static Boolean GOLDBOX_APP_AM_SOCKET_SERVER_ENABLED;

    /**
     * Setup the {@link AmSocketServer} {@link LocalServerSocket} and start listening for
     * new {@link LocalClientSocket} if enabled.
     *
     * @param context The {@link Context} for {@link LocalSocketManager}.
     */
    public static void setupGoldBOXAmSocketServer(@NonNull Context context) {
        // Start goldbox-am-socket server if enabled by user
        boolean enabled = false;
        if (GoldBOXAppSharedProperties.getProperties().shouldRunGoldBOXAmSocketServer()) {
            Logger.logDebug(LOG_TAG, "Starting " + TITLE + " socket server since its enabled");
            start(context);
            if (goldboxAmSocketServer != null && goldboxAmSocketServer.isRunning()) {
                enabled = true;
                Logger.logDebug(LOG_TAG, TITLE + " socket server successfully started");
            }
        } else {
            Logger.logDebug(LOG_TAG, "Not starting " + TITLE + " socket server since its not enabled");
        }

        // Once goldbox-app has started, the server state must not be changed since the variable is
        // exported in shell sessions and tasks and if state is changed, then env of older shells will
        // retain invalid value. User should force stop the app to update state after changing prop.
        GOLDBOX_APP_AM_SOCKET_SERVER_ENABLED = enabled;
        GoldBOXAppShellEnvironment.updateGoldBOXAppAMSocketServerEnabled(context);
    }

    /**
     * Create the {@link AmSocketServer} {@link LocalServerSocket} and start listening for new {@link LocalClientSocket}.
     */
    public static synchronized void start(@NonNull Context context) {
        stop();

        AmSocketServerRunConfig amSocketServerRunConfig = new AmSocketServerRunConfig(TITLE,
            GoldBOXConstants.GOLDBOX_APP.GOLDBOX_AM_SOCKET_FILE_PATH, new GoldBOXAmSocketServerClient());

        goldboxAmSocketServer = AmSocketServer.start(context, amSocketServerRunConfig);
    }

    /**
     * Stop the {@link AmSocketServer} {@link LocalServerSocket} and stop listening for new {@link LocalClientSocket}.
     */
    public static synchronized void stop() {
        if (goldboxAmSocketServer != null) {
            Error error = goldboxAmSocketServer.stop();
            if (error != null) {
                goldboxAmSocketServer.onError(error);
            }
            goldboxAmSocketServer = null;
        }
    }

    /**
     * Update the state of the {@link AmSocketServer} {@link LocalServerSocket} depending on current
     * value of {@link GoldBOXPropertyConstants#KEY_RUN_GOLDBOX_AM_SOCKET_SERVER}.
     */
    public static synchronized void updateState(@NonNull Context context) {
        GoldBOXAppSharedProperties properties = GoldBOXAppSharedProperties.getProperties();
        if (properties.shouldRunGoldBOXAmSocketServer()) {
            if (goldboxAmSocketServer == null) {
                Logger.logDebug(LOG_TAG, "updateState: Starting " + TITLE + " socket server");
                start(context);
            }
        } else {
            if (goldboxAmSocketServer != null) {
                Logger.logDebug(LOG_TAG, "updateState: Disabling " + TITLE + " socket server");
                stop();
            }
        }
    }

    /**
     * Get {@link #goldboxAmSocketServer}.
     */
    public static synchronized LocalSocketManager getGoldBOXAmSocketServer() {
        return goldboxAmSocketServer;
    }

    /**
     * Show an error notification on the {@link GoldBOXConstants#GOLDBOX_PLUGIN_COMMAND_ERRORS_NOTIFICATION_CHANNEL_ID}
     * {@link GoldBOXConstants#GOLDBOX_PLUGIN_COMMAND_ERRORS_NOTIFICATION_CHANNEL_NAME} with a call
     * to {@link GoldBOXPluginUtils#sendPluginCommandErrorNotification(Context, String, CharSequence, String, String)}.
     *
     * @param context The {@link Context} to send the notification with.
     * @param error The {@link Error} generated.
     * @param localSocketRunConfig The {@link LocalSocketRunConfig} for {@link LocalSocketManager}.
     * @param clientSocket The optional {@link LocalClientSocket} for which the error was generated.
     */
    public static synchronized void showErrorNotification(@NonNull Context context, @NonNull Error error,
                                                          @NonNull LocalSocketRunConfig localSocketRunConfig,
                                                          @Nullable LocalClientSocket clientSocket) {
        GoldBOXPluginUtils.sendPluginCommandErrorNotification(context, LOG_TAG,
            localSocketRunConfig.getTitle() + " Socket Server Error", error.getMinimalErrorString(),
            LocalSocketManager.getErrorMarkdownString(error, localSocketRunConfig, clientSocket));
    }



    public static Boolean getGoldBOXAppAMSocketServerEnabled(@NonNull Context currentPackageContext) {
        boolean isGoldBOXApp = GoldBOXConstants.GOLDBOX_PACKAGE_NAME.equals(currentPackageContext.getPackageName());
        if (isGoldBOXApp) {
            return GOLDBOX_APP_AM_SOCKET_SERVER_ENABLED;
        } else {
            // Currently, unsupported since plugin app processes don't know that value is set in goldbox
            // app process GoldBOXAmSocketServer class. A binder API or a way to check if server is actually
            // running needs to be used. Long checks would also not be possible on main application thread
            return null;
        }

    }





    /** Enhanced implementation for {@link AmSocketServer.AmSocketServerClient} for {@link GoldBOXAmSocketServer}. */
    public static class GoldBOXAmSocketServerClient extends AmSocketServer.AmSocketServerClient {

        public static final String LOG_TAG = "GoldBOXAmSocketServerClient";

        @Nullable
        @Override
        public Thread.UncaughtExceptionHandler getLocalSocketManagerClientThreadUEH(
            @NonNull LocalSocketManager localSocketManager) {
            // Use goldbox crash handler for socket listener thread just like used for main app process thread.
            return GoldBOXCrashUtils.getCrashHandler(localSocketManager.getContext());
        }

        @Override
        public void onError(@NonNull LocalSocketManager localSocketManager,
                            @Nullable LocalClientSocket clientSocket, @NonNull Error error) {
            // Don't show notification if server is not running since errors may be triggered
            // when server is stopped and server and client sockets are closed.
            if (localSocketManager.isRunning()) {
                GoldBOXAmSocketServer.showErrorNotification(localSocketManager.getContext(), error,
                    localSocketManager.getLocalSocketRunConfig(), clientSocket);
            }

            // But log the exception
            super.onError(localSocketManager, clientSocket, error);
        }

        @Override
        public void onDisallowedClientConnected(@NonNull LocalSocketManager localSocketManager,
                                                @NonNull LocalClientSocket clientSocket, @NonNull Error error) {
            // Always show notification and log error regardless of if server is running or not
            GoldBOXAmSocketServer.showErrorNotification(localSocketManager.getContext(), error,
                localSocketManager.getLocalSocketRunConfig(), clientSocket);
            super.onDisallowedClientConnected(localSocketManager, clientSocket, error);
        }



        @Override
        protected String getLogTag() {
            return LOG_TAG;
        }

    }

}
