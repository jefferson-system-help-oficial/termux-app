package com.goldbox.shared.goldbox.shell.command.environment;

import android.content.Context;
import android.content.pm.PackageInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.goldbox.shared.android.PackageUtils;
import com.goldbox.shared.shell.command.environment.ShellEnvironmentUtils;
import com.goldbox.shared.goldbox.GoldBOXConstants;
import com.goldbox.shared.goldbox.GoldBOXUtils;

import java.util.HashMap;

/**
 * Environment for {@link GoldBOXConstants#GOLDBOX_API_PACKAGE_NAME} app.
 */
public class GoldBOXAPIShellEnvironment {

    /** Environment variable prefix for the GoldBOX:API app. */
    public static final String GOLDBOX_API_APP_ENV_PREFIX = GoldBOXConstants.GOLDBOX_ENV_PREFIX_ROOT + "_API_APP__";

    /** Environment variable for the GoldBOX:API app version. */
    public static final String ENV_GOLDBOX_API_APP__VERSION_NAME = GOLDBOX_API_APP_ENV_PREFIX + "VERSION_NAME";

    /** Get shell environment for GoldBOX:API app. */
    @Nullable
    public static HashMap<String, String> getEnvironment(@NonNull Context currentPackageContext) {
        if (GoldBOXUtils.isGoldBOXAPIAppInstalled(currentPackageContext) != null) return null;

        String packageName = GoldBOXConstants.GOLDBOX_API_PACKAGE_NAME;
        PackageInfo packageInfo = PackageUtils.getPackageInfoForPackage(currentPackageContext, packageName);
        if (packageInfo == null) return null;

        HashMap<String, String> environment = new HashMap<>();

        ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_GOLDBOX_API_APP__VERSION_NAME, PackageUtils.getVersionNameForPackage(packageInfo));

        return environment;
    }

}
