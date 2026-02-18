package com.goldbox.shared.goldbox.settings.preferences;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.goldbox.shared.logger.Logger;
import com.goldbox.shared.android.PackageUtils;
import com.goldbox.shared.settings.preferences.AppSharedPreferences;
import com.goldbox.shared.settings.preferences.SharedPreferenceUtils;
import com.goldbox.shared.goldbox.GoldBOXUtils;
import com.goldbox.shared.goldbox.settings.preferences.GoldBOXPreferenceConstants.GOLDBOX_BOOT_APP;
import com.goldbox.shared.goldbox.GoldBOXConstants;

public class GoldBOXBootAppSharedPreferences extends AppSharedPreferences {

    private static final String LOG_TAG = "GoldBOXBootAppSharedPreferences";

    private GoldBOXBootAppSharedPreferences(@NonNull Context context) {
        super(context,
            SharedPreferenceUtils.getPrivateSharedPreferences(context,
                GoldBOXConstants.GOLDBOX_BOOT_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION),
            SharedPreferenceUtils.getPrivateAndMultiProcessSharedPreferences(context,
                GoldBOXConstants.GOLDBOX_BOOT_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION));
    }

    /**
     * Get {@link GoldBOXBootAppSharedPreferences}.
     *
     * @param context The {@link Context} to use to get the {@link Context} of the
     *                {@link GoldBOXConstants#GOLDBOX_BOOT_PACKAGE_NAME}.
     * @return Returns the {@link GoldBOXBootAppSharedPreferences}. This will {@code null} if an exception is raised.
     */
    @Nullable
    public static GoldBOXBootAppSharedPreferences build(@NonNull final Context context) {
        Context goldboxBootPackageContext = PackageUtils.getContextForPackage(context, GoldBOXConstants.GOLDBOX_BOOT_PACKAGE_NAME);
        if (goldboxBootPackageContext == null)
            return null;
        else
            return new GoldBOXBootAppSharedPreferences(goldboxBootPackageContext);
    }

    /**
     * Get {@link GoldBOXBootAppSharedPreferences}.
     *
     * @param context The {@link Context} to use to get the {@link Context} of the
     *                {@link GoldBOXConstants#GOLDBOX_BOOT_PACKAGE_NAME}.
     * @param exitAppOnError If {@code true} and failed to get package context, then a dialog will
     *                       be shown which when dismissed will exit the app.
     * @return Returns the {@link GoldBOXBootAppSharedPreferences}. This will {@code null} if an exception is raised.
     */
    public static GoldBOXBootAppSharedPreferences build(@NonNull final Context context, final boolean exitAppOnError) {
        Context goldboxBootPackageContext = GoldBOXUtils.getContextForPackageOrExitApp(context, GoldBOXConstants.GOLDBOX_BOOT_PACKAGE_NAME, exitAppOnError);
        if (goldboxBootPackageContext == null)
            return null;
        else
            return new GoldBOXBootAppSharedPreferences(goldboxBootPackageContext);
    }



    public int getLogLevel(boolean readFromFile) {
        if (readFromFile)
            return SharedPreferenceUtils.getInt(mMultiProcessSharedPreferences, GOLDBOX_BOOT_APP.KEY_LOG_LEVEL, Logger.DEFAULT_LOG_LEVEL);
        else
            return SharedPreferenceUtils.getInt(mSharedPreferences, GOLDBOX_BOOT_APP.KEY_LOG_LEVEL, Logger.DEFAULT_LOG_LEVEL);
    }

    public void setLogLevel(Context context, int logLevel, boolean commitToFile) {
        logLevel = Logger.setLogLevel(context, logLevel);
        SharedPreferenceUtils.setInt(mSharedPreferences, GOLDBOX_BOOT_APP.KEY_LOG_LEVEL, logLevel, commitToFile);
    }

}
