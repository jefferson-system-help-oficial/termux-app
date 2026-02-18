package com.goldbox.shared.goldbox.shell.command.environment;

import android.content.Context;

import androidx.annotation.NonNull;

import com.goldbox.shared.errors.Error;
import com.goldbox.shared.file.FileUtils;
import com.goldbox.shared.logger.Logger;
import com.goldbox.shared.shell.command.ExecutionCommand;
import com.goldbox.shared.shell.command.environment.AndroidShellEnvironment;
import com.goldbox.shared.shell.command.environment.ShellEnvironmentUtils;
import com.goldbox.shared.shell.command.environment.ShellCommandShellEnvironment;
import com.goldbox.shared.goldbox.GoldBOXBootstrap;
import com.goldbox.shared.goldbox.GoldBOXConstants;
import com.goldbox.shared.goldbox.shell.GoldBOXShellUtils;

import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * Environment for GoldBOX.
 */
public class GoldBOXShellEnvironment extends AndroidShellEnvironment {

    private static final String LOG_TAG = "GoldBOXShellEnvironment";

    /** Environment variable for the goldbox {@link GoldBOXConstants#GOLDBOX_PREFIX_DIR_PATH}. */
    public static final String ENV_PREFIX = "PREFIX";

    public GoldBOXShellEnvironment() {
        super();
        shellCommandShellEnvironment = new GoldBOXShellCommandShellEnvironment();
    }


    /** Init {@link GoldBOXShellEnvironment} constants and caches. */
    public synchronized static void init(@NonNull Context currentPackageContext) {
        GoldBOXAppShellEnvironment.setGoldBOXAppEnvironment(currentPackageContext);
    }

    /** Init {@link GoldBOXShellEnvironment} constants and caches. */
    public synchronized static void writeEnvironmentToFile(@NonNull Context currentPackageContext) {
        HashMap<String, String> environmentMap = new GoldBOXShellEnvironment().getEnvironment(currentPackageContext, false);
        String environmentString = ShellEnvironmentUtils.convertEnvironmentToDotEnvFile(environmentMap);

        // Write environment string to temp file and then move to final location since otherwise
        // writing may happen while file is being sourced/read
        Error error = FileUtils.writeTextToFile("goldbox.env.tmp", GoldBOXConstants.GOLDBOX_ENV_TEMP_FILE_PATH,
            Charset.defaultCharset(), environmentString, false);
        if (error != null) {
            Logger.logErrorExtended(LOG_TAG, error.toString());
            return;
        }

        error = FileUtils.moveRegularFile("goldbox.env.tmp", GoldBOXConstants.GOLDBOX_ENV_TEMP_FILE_PATH, GoldBOXConstants.GOLDBOX_ENV_FILE_PATH, true);
        if (error != null) {
            Logger.logErrorExtended(LOG_TAG, error.toString());
        }
    }

    /** Get shell environment for GoldBOX. */
    @NonNull
    @Override
    public HashMap<String, String> getEnvironment(@NonNull Context currentPackageContext, boolean isFailSafe) {

        // GoldBOX environment builds upon the Android environment
        HashMap<String, String> environment = super.getEnvironment(currentPackageContext, isFailSafe);

        HashMap<String, String> goldboxAppEnvironment = GoldBOXAppShellEnvironment.getEnvironment(currentPackageContext);
        if (goldboxAppEnvironment != null)
            environment.putAll(goldboxAppEnvironment);

        HashMap<String, String> goldboxApiAppEnvironment = GoldBOXAPIShellEnvironment.getEnvironment(currentPackageContext);
        if (goldboxApiAppEnvironment != null)
            environment.putAll(goldboxApiAppEnvironment);

        environment.put(ENV_HOME, GoldBOXConstants.GOLDBOX_HOME_DIR_PATH);
        environment.put(ENV_PREFIX, GoldBOXConstants.GOLDBOX_PREFIX_DIR_PATH);

        // If failsafe is not enabled, then we keep default PATH and TMPDIR so that system binaries can be used
        if (!isFailSafe) {
            environment.put(ENV_TMPDIR, GoldBOXConstants.GOLDBOX_TMP_PREFIX_DIR_PATH);
            if (GoldBOXBootstrap.isAppPackageVariantAPTAndroid5()) {
                // GoldBOX in android 5/6 era shipped busybox binaries in applets directory
                environment.put(ENV_PATH, GoldBOXConstants.GOLDBOX_BIN_PREFIX_DIR_PATH + ":" + GoldBOXConstants.GOLDBOX_BIN_PREFIX_DIR_PATH + "/applets");
                environment.put(ENV_LD_LIBRARY_PATH, GoldBOXConstants.GOLDBOX_LIB_PREFIX_DIR_PATH);
            } else {
                // GoldBOX binaries on Android 7+ rely on DT_RUNPATH, so LD_LIBRARY_PATH should be unset by default
                environment.put(ENV_PATH, GoldBOXConstants.GOLDBOX_BIN_PREFIX_DIR_PATH);
                environment.remove(ENV_LD_LIBRARY_PATH);
            }
        }

        return environment;
    }


    @NonNull
    @Override
    public String getDefaultWorkingDirectoryPath() {
        return GoldBOXConstants.GOLDBOX_HOME_DIR_PATH;
    }

    @NonNull
    @Override
    public String getDefaultBinPath() {
        return GoldBOXConstants.GOLDBOX_BIN_PREFIX_DIR_PATH;
    }

    @NonNull
    @Override
    public String[] setupShellCommandArguments(@NonNull String executable, String[] arguments) {
        return GoldBOXShellUtils.setupShellCommandArguments(executable, arguments);
    }

}
