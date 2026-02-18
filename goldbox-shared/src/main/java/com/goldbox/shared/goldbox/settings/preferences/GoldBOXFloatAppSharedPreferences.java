package com.goldbox.shared.goldbox.settings.preferences;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.goldbox.shared.data.DataUtils;
import com.goldbox.shared.logger.Logger;
import com.goldbox.shared.android.PackageUtils;
import com.goldbox.shared.settings.preferences.AppSharedPreferences;
import com.goldbox.shared.settings.preferences.SharedPreferenceUtils;
import com.goldbox.shared.goldbox.GoldBOXUtils;
import com.goldbox.shared.goldbox.settings.preferences.GoldBOXPreferenceConstants.GOLDBOX_FLOAT_APP;
import com.goldbox.shared.goldbox.GoldBOXConstants;

public class GoldBOXFloatAppSharedPreferences extends AppSharedPreferences {

    private int MIN_FONTSIZE;
    private int MAX_FONTSIZE;
    private int DEFAULT_FONTSIZE;

    private static final String LOG_TAG = "GoldBOXFloatAppSharedPreferences";

    private GoldBOXFloatAppSharedPreferences(@NonNull Context context) {
        super(context,
            SharedPreferenceUtils.getPrivateSharedPreferences(context,
                GoldBOXConstants.GOLDBOX_FLOAT_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION),
            SharedPreferenceUtils.getPrivateAndMultiProcessSharedPreferences(context,
                GoldBOXConstants.GOLDBOX_FLOAT_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION));

        setFontVariables(context);
    }

    /**
     * Get {@link GoldBOXFloatAppSharedPreferences}.
     *
     * @param context The {@link Context} to use to get the {@link Context} of the
     *                {@link GoldBOXConstants#GOLDBOX_FLOAT_PACKAGE_NAME}.
     * @return Returns the {@link GoldBOXFloatAppSharedPreferences}. This will {@code null} if an exception is raised.
     */
    @Nullable
    public static GoldBOXFloatAppSharedPreferences build(@NonNull final Context context) {
        Context goldboxFloatPackageContext = PackageUtils.getContextForPackage(context, GoldBOXConstants.GOLDBOX_FLOAT_PACKAGE_NAME);
        if (goldboxFloatPackageContext == null)
            return null;
        else
            return new GoldBOXFloatAppSharedPreferences(goldboxFloatPackageContext);
    }

    /**
     * Get {@link GoldBOXFloatAppSharedPreferences}.
     *
     * @param context The {@link Context} to use to get the {@link Context} of the
     *                {@link GoldBOXConstants#GOLDBOX_FLOAT_PACKAGE_NAME}.
     * @param exitAppOnError If {@code true} and failed to get package context, then a dialog will
     *                       be shown which when dismissed will exit the app.
     * @return Returns the {@link GoldBOXFloatAppSharedPreferences}. This will {@code null} if an exception is raised.
     */
    public static GoldBOXFloatAppSharedPreferences build(@NonNull final Context context, final boolean exitAppOnError) {
        Context goldboxFloatPackageContext = GoldBOXUtils.getContextForPackageOrExitApp(context, GoldBOXConstants.GOLDBOX_FLOAT_PACKAGE_NAME, exitAppOnError);
        if (goldboxFloatPackageContext == null)
            return null;
        else
            return new GoldBOXFloatAppSharedPreferences(goldboxFloatPackageContext);
    }



    public int getWindowX() {
        return SharedPreferenceUtils.getInt(mSharedPreferences, GOLDBOX_FLOAT_APP.KEY_WINDOW_X, 200);

    }

    public void setWindowX(int value) {
        SharedPreferenceUtils.setInt(mSharedPreferences, GOLDBOX_FLOAT_APP.KEY_WINDOW_X, value, false);
    }

    public int getWindowY() {
        return SharedPreferenceUtils.getInt(mSharedPreferences, GOLDBOX_FLOAT_APP.KEY_WINDOW_Y, 200);

    }

    public void setWindowY(int value) {
        SharedPreferenceUtils.setInt(mSharedPreferences, GOLDBOX_FLOAT_APP.KEY_WINDOW_Y, value, false);
    }



    public int getWindowWidth() {
        return SharedPreferenceUtils.getInt(mSharedPreferences, GOLDBOX_FLOAT_APP.KEY_WINDOW_WIDTH, 500);

    }

    public void setWindowWidth(int value) {
        SharedPreferenceUtils.setInt(mSharedPreferences, GOLDBOX_FLOAT_APP.KEY_WINDOW_WIDTH, value, false);
    }

    public int getWindowHeight() {
        return SharedPreferenceUtils.getInt(mSharedPreferences, GOLDBOX_FLOAT_APP.KEY_WINDOW_HEIGHT, 500);

    }

    public void setWindowHeight(int value) {
        SharedPreferenceUtils.setInt(mSharedPreferences, GOLDBOX_FLOAT_APP.KEY_WINDOW_HEIGHT, value, false);
    }



    public void setFontVariables(Context context) {
        int[] sizes = GoldBOXAppSharedPreferences.getDefaultFontSizes(context);

        DEFAULT_FONTSIZE = sizes[0];
        MIN_FONTSIZE = sizes[1];
        MAX_FONTSIZE = sizes[2];
    }

    public int getFontSize() {
        int fontSize = SharedPreferenceUtils.getIntStoredAsString(mSharedPreferences, GOLDBOX_FLOAT_APP.KEY_FONTSIZE, DEFAULT_FONTSIZE);
        return DataUtils.clamp(fontSize, MIN_FONTSIZE, MAX_FONTSIZE);
    }

    public void setFontSize(int value) {
        SharedPreferenceUtils.setIntStoredAsString(mSharedPreferences, GOLDBOX_FLOAT_APP.KEY_FONTSIZE, value, false);
    }

    public void changeFontSize(boolean increase) {
        int fontSize = getFontSize();

        fontSize += (increase ? 1 : -1) * 2;
        fontSize = Math.max(MIN_FONTSIZE, Math.min(fontSize, MAX_FONTSIZE));

        setFontSize(fontSize);
    }


    public int getLogLevel(boolean readFromFile) {
        if (readFromFile)
            return SharedPreferenceUtils.getInt(mMultiProcessSharedPreferences, GOLDBOX_FLOAT_APP.KEY_LOG_LEVEL, Logger.DEFAULT_LOG_LEVEL);
        else
            return SharedPreferenceUtils.getInt(mSharedPreferences, GOLDBOX_FLOAT_APP.KEY_LOG_LEVEL, Logger.DEFAULT_LOG_LEVEL);
    }

    public void setLogLevel(Context context, int logLevel, boolean commitToFile) {
        logLevel = Logger.setLogLevel(context, logLevel);
        SharedPreferenceUtils.setInt(mSharedPreferences, GOLDBOX_FLOAT_APP.KEY_LOG_LEVEL, logLevel, commitToFile);
    }


    public boolean isTerminalViewKeyLoggingEnabled(boolean readFromFile) {
        if (readFromFile)
            return SharedPreferenceUtils.getBoolean(mMultiProcessSharedPreferences, GOLDBOX_FLOAT_APP.KEY_TERMINAL_VIEW_KEY_LOGGING_ENABLED, GOLDBOX_FLOAT_APP.DEFAULT_VALUE_TERMINAL_VIEW_KEY_LOGGING_ENABLED);
        else
            return SharedPreferenceUtils.getBoolean(mSharedPreferences, GOLDBOX_FLOAT_APP.KEY_TERMINAL_VIEW_KEY_LOGGING_ENABLED, GOLDBOX_FLOAT_APP.DEFAULT_VALUE_TERMINAL_VIEW_KEY_LOGGING_ENABLED);
    }

    public void setTerminalViewKeyLoggingEnabled(boolean value, boolean commitToFile) {
        SharedPreferenceUtils.setBoolean(mSharedPreferences, GOLDBOX_FLOAT_APP.KEY_TERMINAL_VIEW_KEY_LOGGING_ENABLED, value, commitToFile);
    }

}
