package com.goldbox.shared.goldbox.shell.command.environment;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.goldbox.shared.android.PackageUtils;
import com.goldbox.shared.android.SELinuxUtils;
import com.goldbox.shared.data.DataUtils;
import com.goldbox.shared.shell.command.environment.ShellEnvironmentUtils;
import com.goldbox.shared.goldbox.GoldBOXBootstrap;
import com.goldbox.shared.goldbox.GoldBOXConstants;
import com.goldbox.shared.goldbox.GoldBOXUtils;
import com.goldbox.shared.goldbox.shell.am.GoldBOXAmSocketServer;

import java.util.HashMap;

/**
 * Environment for {@link GoldBOXConstants#GOLDBOX_PACKAGE_NAME} app.
 */
public class GoldBOXAppShellEnvironment {

    /** GoldBOX app environment variables. */
    public static HashMap<String, String> goldboxAppEnvironment;

    /** Environment variable for the GoldBOX app version. */
    public static final String ENV_GOLDBOX_VERSION = GoldBOXConstants.GOLDBOX_ENV_PREFIX_ROOT + "_VERSION";

    /** Environment variable prefix for the GoldBOX app. */
    public static final String GOLDBOX_APP_ENV_PREFIX = GoldBOXConstants.GOLDBOX_ENV_PREFIX_ROOT + "_APP__";

    /** Environment variable for the GoldBOX app version name. */
    public static final String ENV_GOLDBOX_APP__VERSION_NAME = GOLDBOX_APP_ENV_PREFIX + "VERSION_NAME";
    /** Environment variable for the GoldBOX app version code. */
    public static final String ENV_GOLDBOX_APP__VERSION_CODE = GOLDBOX_APP_ENV_PREFIX + "VERSION_CODE";
    /** Environment variable for the GoldBOX app package name. */
    public static final String ENV_GOLDBOX_APP__PACKAGE_NAME = GOLDBOX_APP_ENV_PREFIX + "PACKAGE_NAME";
    /** Environment variable for the GoldBOX app process id. */
    public static final String ENV_GOLDBOX_APP__PID = GOLDBOX_APP_ENV_PREFIX + "PID";
    /** Environment variable for the GoldBOX app uid. */
    public static final String ENV_GOLDBOX_APP__UID = GOLDBOX_APP_ENV_PREFIX + "UID";
    /** Environment variable for the GoldBOX app targetSdkVersion. */
    public static final String ENV_GOLDBOX_APP__TARGET_SDK = GOLDBOX_APP_ENV_PREFIX + "TARGET_SDK";
    /** Environment variable for the GoldBOX app is debuggable apk build. */
    public static final String ENV_GOLDBOX_APP__IS_DEBUGGABLE_BUILD = GOLDBOX_APP_ENV_PREFIX + "IS_DEBUGGABLE_BUILD";
    /** Environment variable for the GoldBOX app {@link GoldBOXConstants} APK_RELEASE_*. */
    public static final String ENV_GOLDBOX_APP__APK_RELEASE = GOLDBOX_APP_ENV_PREFIX + "APK_RELEASE";
    /** Environment variable for the GoldBOX app install path. */
    public static final String ENV_GOLDBOX_APP__APK_PATH = GOLDBOX_APP_ENV_PREFIX + "APK_PATH";
    /** Environment variable for the GoldBOX app is installed on external/portable storage. */
    public static final String ENV_GOLDBOX_APP__IS_INSTALLED_ON_EXTERNAL_STORAGE = GOLDBOX_APP_ENV_PREFIX + "IS_INSTALLED_ON_EXTERNAL_STORAGE";

    /** Environment variable for the GoldBOX app process selinux context. */
    public static final String ENV_GOLDBOX_APP__SE_PROCESS_CONTEXT = GOLDBOX_APP_ENV_PREFIX + "SE_PROCESS_CONTEXT";
    /** Environment variable for the GoldBOX app data files selinux context. */
    public static final String ENV_GOLDBOX_APP__SE_FILE_CONTEXT = GOLDBOX_APP_ENV_PREFIX + "SE_FILE_CONTEXT";
    /** Environment variable for the GoldBOX app seInfo tag found in selinux policy used to set app process and app data files selinux context. */
    public static final String ENV_GOLDBOX_APP__SE_INFO = GOLDBOX_APP_ENV_PREFIX + "SE_INFO";
    /** Environment variable for the GoldBOX app user id. */
    public static final String ENV_GOLDBOX_APP__USER_ID = GOLDBOX_APP_ENV_PREFIX + "USER_ID";
    /** Environment variable for the GoldBOX app profile owner. */
    public static final String ENV_GOLDBOX_APP__PROFILE_OWNER = GOLDBOX_APP_ENV_PREFIX + "PROFILE_OWNER";

    /** Environment variable for the GoldBOX app {@link GoldBOXBootstrap#GOLDBOX_APP_PACKAGE_MANAGER}. */
    public static final String ENV_GOLDBOX_APP__PACKAGE_MANAGER = GOLDBOX_APP_ENV_PREFIX + "PACKAGE_MANAGER";
    /** Environment variable for the GoldBOX app {@link GoldBOXBootstrap#GOLDBOX_APP_PACKAGE_VARIANT}. */
    public static final String ENV_GOLDBOX_APP__PACKAGE_VARIANT = GOLDBOX_APP_ENV_PREFIX + "PACKAGE_VARIANT";
    /** Environment variable for the GoldBOX app files directory. */
    public static final String ENV_GOLDBOX_APP__FILES_DIR = GOLDBOX_APP_ENV_PREFIX + "FILES_DIR";


    /** Environment variable for the GoldBOX app {@link GoldBOXAmSocketServer#getGoldBOXAppAMSocketServerEnabled(Context)}. */
    public static final String ENV_GOLDBOX_APP__AM_SOCKET_SERVER_ENABLED = GOLDBOX_APP_ENV_PREFIX + "AM_SOCKET_SERVER_ENABLED";



    /** Get shell environment for GoldBOX app. */
    @Nullable
    public static HashMap<String, String> getEnvironment(@NonNull Context currentPackageContext) {
        setGoldBOXAppEnvironment(currentPackageContext);
        return goldboxAppEnvironment;
    }

    /** Set GoldBOX app environment variables in {@link #goldboxAppEnvironment}. */
    public synchronized static void setGoldBOXAppEnvironment(@NonNull Context currentPackageContext) {
        boolean isGoldBOXApp = GoldBOXConstants.GOLDBOX_PACKAGE_NAME.equals(currentPackageContext.getPackageName());

        // If current package context is of goldbox app and its environment is already set, then no need to set again since it won't change
        // Other apps should always set environment again since goldbox app may be installed/updated/deleted in background
        if (goldboxAppEnvironment != null && isGoldBOXApp)
            return;

        goldboxAppEnvironment = null;

        String packageName = GoldBOXConstants.GOLDBOX_PACKAGE_NAME;
        PackageInfo packageInfo = PackageUtils.getPackageInfoForPackage(currentPackageContext, packageName);
        if (packageInfo == null) return;
        ApplicationInfo applicationInfo = PackageUtils.getApplicationInfoForPackage(currentPackageContext, packageName);
        if (applicationInfo == null || !applicationInfo.enabled) return;

        HashMap<String, String> environment = new HashMap<>();

        ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_GOLDBOX_VERSION, PackageUtils.getVersionNameForPackage(packageInfo));
        ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_GOLDBOX_APP__VERSION_NAME, PackageUtils.getVersionNameForPackage(packageInfo));
        ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_GOLDBOX_APP__VERSION_CODE, String.valueOf(PackageUtils.getVersionCodeForPackage(packageInfo)));

        ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_GOLDBOX_APP__PACKAGE_NAME, packageName);
        ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_GOLDBOX_APP__PID, GoldBOXUtils.getGoldBOXAppPID(currentPackageContext));
        ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_GOLDBOX_APP__UID, String.valueOf(PackageUtils.getUidForPackage(applicationInfo)));
        ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_GOLDBOX_APP__TARGET_SDK, String.valueOf(PackageUtils.getTargetSDKForPackage(applicationInfo)));
        ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_GOLDBOX_APP__IS_DEBUGGABLE_BUILD, PackageUtils.isAppForPackageADebuggableBuild(applicationInfo));
        ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_GOLDBOX_APP__APK_PATH, PackageUtils.getBaseAPKPathForPackage(applicationInfo));
        ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_GOLDBOX_APP__IS_INSTALLED_ON_EXTERNAL_STORAGE, PackageUtils.isAppInstalledOnExternalStorage(applicationInfo));

        putGoldBOXAPKSignature(currentPackageContext, environment);

        Context goldboxPackageContext = GoldBOXUtils.getGoldBOXPackageContext(currentPackageContext);
        if (goldboxPackageContext != null) {
            // An app that does not have the same sharedUserId as goldbox app will not be able to get
            // get goldbox context's classloader to get BuildConfig.GOLDBOX_PACKAGE_VARIANT via reflection.
            // Check GoldBOXBootstrap.setGoldBOXPackageManagerAndVariantFromGoldBOXApp()
            if (GoldBOXBootstrap.GOLDBOX_APP_PACKAGE_MANAGER != null)
                environment.put(ENV_GOLDBOX_APP__PACKAGE_MANAGER, GoldBOXBootstrap.GOLDBOX_APP_PACKAGE_MANAGER.getName());
            if (GoldBOXBootstrap.GOLDBOX_APP_PACKAGE_VARIANT != null)
                environment.put(ENV_GOLDBOX_APP__PACKAGE_VARIANT, GoldBOXBootstrap.GOLDBOX_APP_PACKAGE_VARIANT.getName());

            // Will not be set for plugins
            ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_GOLDBOX_APP__AM_SOCKET_SERVER_ENABLED,
                GoldBOXAmSocketServer.getGoldBOXAppAMSocketServerEnabled(currentPackageContext));

            String filesDirPath = currentPackageContext.getFilesDir().getAbsolutePath();
            ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_GOLDBOX_APP__FILES_DIR, filesDirPath);

            ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_GOLDBOX_APP__SE_PROCESS_CONTEXT, SELinuxUtils.getContext());
            ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_GOLDBOX_APP__SE_FILE_CONTEXT, SELinuxUtils.getFileContext(filesDirPath));

            String seInfoUser = PackageUtils.getApplicationInfoSeInfoUserForPackage(applicationInfo);
            ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_GOLDBOX_APP__SE_INFO, PackageUtils.getApplicationInfoSeInfoForPackage(applicationInfo) +
                (DataUtils.isNullOrEmpty(seInfoUser) ? "" : seInfoUser));

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_GOLDBOX_APP__USER_ID, String.valueOf(PackageUtils.getUserIdForPackage(currentPackageContext)));
            ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_GOLDBOX_APP__PROFILE_OWNER, PackageUtils.getProfileOwnerPackageNameForUser(currentPackageContext));
        }

        goldboxAppEnvironment = environment;
    }

    /** Put {@link #ENV_GOLDBOX_APP__APK_RELEASE} in {@code environment}. */
    public static void putGoldBOXAPKSignature(@NonNull Context currentPackageContext,
                                             @NonNull HashMap<String, String> environment) {
        String signingCertificateSHA256Digest = PackageUtils.getSigningCertificateSHA256DigestForPackage(currentPackageContext,
            GoldBOXConstants.GOLDBOX_PACKAGE_NAME);
        if (signingCertificateSHA256Digest != null) {
            ShellEnvironmentUtils.putToEnvIfSet(environment, ENV_GOLDBOX_APP__APK_RELEASE,
                GoldBOXUtils.getAPKRelease(signingCertificateSHA256Digest).replaceAll("[^a-zA-Z]", "_").toUpperCase());
        }
    }

    /** Update {@link #ENV_GOLDBOX_APP__AM_SOCKET_SERVER_ENABLED} value in {@code environment}. */
    public synchronized static void updateGoldBOXAppAMSocketServerEnabled(@NonNull Context currentPackageContext) {
        if (goldboxAppEnvironment == null) return;
        goldboxAppEnvironment.remove(ENV_GOLDBOX_APP__AM_SOCKET_SERVER_ENABLED);
        ShellEnvironmentUtils.putToEnvIfSet(goldboxAppEnvironment, ENV_GOLDBOX_APP__AM_SOCKET_SERVER_ENABLED,
            GoldBOXAmSocketServer.getGoldBOXAppAMSocketServerEnabled(currentPackageContext));
    }

}
