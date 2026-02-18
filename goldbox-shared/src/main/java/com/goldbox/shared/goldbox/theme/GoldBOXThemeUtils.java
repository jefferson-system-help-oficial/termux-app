package com.goldbox.shared.goldbox.theme;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.goldbox.shared.goldbox.settings.properties.GoldBOXPropertyConstants;
import com.goldbox.shared.goldbox.settings.properties.GoldBOXSharedProperties;
import com.goldbox.shared.theme.NightMode;

public class GoldBOXThemeUtils {

    /** Get the {@link GoldBOXPropertyConstants#KEY_NIGHT_MODE} value from the properties file on disk
     * and set it to app wide night mode value. */
    public static void setAppNightMode(@NonNull Context context) {
        NightMode.setAppNightMode(GoldBOXSharedProperties.getNightMode(context));
    }

    /** Set name as app wide night mode value. */
    public static void setAppNightMode(@Nullable String name) {
        NightMode.setAppNightMode(name);
    }

}
