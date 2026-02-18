package com.goldbox.app;

import android.app.Application;
import android.content.Context;

import com.goldbox.BuildConfig;
import com.goldbox.shared.errors.Error;
import com.goldbox.shared.logger.Logger;
import com.goldbox.shared.goldbox.GoldBOXBootstrap;
import com.goldbox.shared.goldbox.GoldBOXConstants;
import com.goldbox.shared.goldbox.crash.GoldBOXCrashUtils;
import com.goldbox.shared.goldbox.file.GoldBOXFileUtils;
import com.goldbox.shared.goldbox.settings.preferences.GoldBOXAppSharedPreferences;
import com.goldbox.shared.goldbox.settings.properties.GoldBOXAppSharedProperties;
import com.goldbox.shared.goldbox.shell.command.environment.GoldBOXShellEnvironment;
import com.goldbox.shared.goldbox.shell.am.GoldBOXAmSocketServer;
import com.goldbox.shared.goldbox.shell.GoldBOXShellManager;
import com.goldbox.shared.goldbox.theme.GoldBOXThemeUtils;

public class GoldBOXApplication extends Application {

    private static final String LOG_TAG = "GoldBOXApplication";

    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();

        // Set crash handler for the app
        GoldBOXCrashUtils.setDefaultCrashHandler(this);

        // Set log config for the app
        setLogConfig(context);

        Logger.logDebug("Starting Application");

        // Set GoldBOXBootstrap.GOLDBOX_APP_PACKAGE_MANAGER and GoldBOXBootstrap.GOLDBOX_APP_PACKAGE_VARIANT
        GoldBOXBootstrap.setGoldBOXPackageManagerAndVariant(BuildConfig.GOLDBOX_PACKAGE_VARIANT);

        // Init app wide SharedProperties loaded from goldbox.properties
        GoldBOXAppSharedProperties properties = GoldBOXAppSharedProperties.init(context);

        // Init app wide shell manager
        GoldBOXShellManager shellManager = GoldBOXShellManager.init(context);

        // Set NightMode.APP_NIGHT_MODE
        GoldBOXThemeUtils.setAppNightMode(properties.getNightMode());

        // Check and create goldbox files directory. If failed to access it like in case of secondary
        // user or external sd card installation, then don't run files directory related code
        Error error = GoldBOXFileUtils.isGoldBOXFilesDirectoryAccessible(this, true, true);
        boolean isGoldBOXFilesDirectoryAccessible = error == null;
        if (isGoldBOXFilesDirectoryAccessible) {
            Logger.logInfo(LOG_TAG, "GoldBOX files directory is accessible");

            error = GoldBOXFileUtils.isAppsGoldBOXAppDirectoryAccessible(true, true);
            if (error != null) {
                Logger.logErrorExtended(LOG_TAG, "Create apps/goldbox-app directory failed\n" + error);
                return;
            }

            // Setup goldbox-am-socket server
            GoldBOXAmSocketServer.setupGoldBOXAmSocketServer(context);
        } else {
            Logger.logErrorExtended(LOG_TAG, "GoldBOX files directory is not accessible\n" + error);
        }

        // Init GoldBOXShellEnvironment constants and caches after everything has been setup including goldbox-am-socket server
        GoldBOXShellEnvironment.init(this);

        if (isGoldBOXFilesDirectoryAccessible) {
            GoldBOXShellEnvironment.writeEnvironmentToFile(this);
        }
    }

    public static void setLogConfig(Context context) {
        Logger.setDefaultLogTag(GoldBOXConstants.GOLDBOX_APP_NAME);

        // Load the log level from shared preferences and set it to the {@link Logger.CURRENT_LOG_LEVEL}
        GoldBOXAppSharedPreferences preferences = GoldBOXAppSharedPreferences.build(context);
        if (preferences == null) return;
        preferences.setLogLevel(null, preferences.getLogLevel());
    }

}
