package com.goldbox.shared.goldbox.settings.properties;

import android.content.Context;

import androidx.annotation.NonNull;

import com.goldbox.shared.goldbox.GoldBOXConstants;

public class GoldBOXAppSharedProperties extends GoldBOXSharedProperties {

    private static GoldBOXAppSharedProperties properties;


    private GoldBOXAppSharedProperties(@NonNull Context context) {
        super(context, GoldBOXConstants.GOLDBOX_APP_NAME,
            GoldBOXConstants.GOLDBOX_PROPERTIES_FILE_PATHS_LIST, GoldBOXPropertyConstants.GOLDBOX_APP_PROPERTIES_LIST,
            new GoldBOXSharedProperties.SharedPropertiesParserClient());
    }

    /**
     * Initialize the {@link #properties} and load properties from disk.
     *
     * @param context The {@link Context} for operations.
     * @return Returns the {@link GoldBOXAppSharedProperties}.
     */
    public static GoldBOXAppSharedProperties init(@NonNull Context context) {
        if (properties == null)
            properties = new GoldBOXAppSharedProperties(context);

        return properties;
    }

    /**
     * Get the {@link #properties}.
     *
     * @return Returns the {@link GoldBOXAppSharedProperties}.
     */
    public static GoldBOXAppSharedProperties getProperties() {
        return properties;
    }

}
