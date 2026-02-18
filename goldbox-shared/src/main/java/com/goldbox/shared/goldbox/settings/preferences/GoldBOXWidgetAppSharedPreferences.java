package com.goldbox.shared.goldbox.settings.preferences;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.goldbox.shared.logger.Logger;
import com.goldbox.shared.android.PackageUtils;
import com.goldbox.shared.settings.preferences.AppSharedPreferences;
import com.goldbox.shared.settings.preferences.SharedPreferenceUtils;
import com.goldbox.shared.goldbox.GoldBOXUtils;
import com.goldbox.shared.goldbox.settings.preferences.GoldBOXPreferenceConstants.GOLDBOX_WIDGET_APP;
import com.goldbox.shared.goldbox.GoldBOXConstants;

import java.util.UUID;

public class GoldBOXWidgetAppSharedPreferences extends AppSharedPreferences {

    private static final String LOG_TAG = "GoldBOXWidgetAppSharedPreferences";

    private GoldBOXWidgetAppSharedPreferences(@NonNull Context context) {
        super(context,
            SharedPreferenceUtils.getPrivateSharedPreferences(context,
                GoldBOXConstants.GOLDBOX_WIDGET_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION),
            SharedPreferenceUtils.getPrivateAndMultiProcessSharedPreferences(context,
                GoldBOXConstants.GOLDBOX_WIDGET_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION));
    }

    /**
     * Get {@link GoldBOXWidgetAppSharedPreferences}.
     *
     * @param context The {@link Context} to use to get the {@link Context} of the
     *                {@link GoldBOXConstants#GOLDBOX_WIDGET_PACKAGE_NAME}.
     * @return Returns the {@link GoldBOXWidgetAppSharedPreferences}. This will {@code null} if an exception is raised.
     */
    @Nullable
    public static GoldBOXWidgetAppSharedPreferences build(@NonNull final Context context) {
        Context goldboxWidgetPackageContext = PackageUtils.getContextForPackage(context, GoldBOXConstants.GOLDBOX_WIDGET_PACKAGE_NAME);
        if (goldboxWidgetPackageContext == null)
            return null;
        else
            return new GoldBOXWidgetAppSharedPreferences(goldboxWidgetPackageContext);
    }

    /**
     * Get the {@link GoldBOXWidgetAppSharedPreferences}.
     *
     * @param context The {@link Context} to use to get the {@link Context} of the
     *                {@link GoldBOXConstants#GOLDBOX_WIDGET_PACKAGE_NAME}.
     * @param exitAppOnError If {@code true} and failed to get package context, then a dialog will
     *                       be shown which when dismissed will exit the app.
     * @return Returns the {@link GoldBOXWidgetAppSharedPreferences}. This will {@code null} if an exception is raised.
     */
    public static GoldBOXWidgetAppSharedPreferences build(@NonNull final Context context, final boolean exitAppOnError) {
        Context goldboxWidgetPackageContext = GoldBOXUtils.getContextForPackageOrExitApp(context, GoldBOXConstants.GOLDBOX_WIDGET_PACKAGE_NAME, exitAppOnError);
        if (goldboxWidgetPackageContext == null)
            return null;
        else
            return new GoldBOXWidgetAppSharedPreferences(goldboxWidgetPackageContext);
    }



    public static String getGeneratedToken(@NonNull Context context) {
        GoldBOXWidgetAppSharedPreferences preferences = GoldBOXWidgetAppSharedPreferences.build(context, true);
        if (preferences == null) return null;
        return preferences.getGeneratedToken();
    }

    public String getGeneratedToken() {
        String token =  SharedPreferenceUtils.getString(mSharedPreferences, GOLDBOX_WIDGET_APP.KEY_TOKEN, null, true);
        if (token == null) {
            token = UUID.randomUUID().toString();
            SharedPreferenceUtils.setString(mSharedPreferences, GOLDBOX_WIDGET_APP.KEY_TOKEN, token, true);
        }
        return token;
    }



    public int getLogLevel(boolean readFromFile) {
        if (readFromFile)
            return SharedPreferenceUtils.getInt(mMultiProcessSharedPreferences, GOLDBOX_WIDGET_APP.KEY_LOG_LEVEL, Logger.DEFAULT_LOG_LEVEL);
        else
            return SharedPreferenceUtils.getInt(mSharedPreferences, GOLDBOX_WIDGET_APP.KEY_LOG_LEVEL, Logger.DEFAULT_LOG_LEVEL);
    }

    public void setLogLevel(Context context, int logLevel, boolean commitToFile) {
        logLevel = Logger.setLogLevel(context, logLevel);
        SharedPreferenceUtils.setInt(mSharedPreferences, GOLDBOX_WIDGET_APP.KEY_LOG_LEVEL, logLevel, commitToFile);
    }

}
