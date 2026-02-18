package com.goldbox.app.fragments.settings;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.goldbox.R;
import com.goldbox.shared.goldbox.settings.preferences.GoldBOXWidgetAppSharedPreferences;

@Keep
public class GoldBOXWidgetPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getContext();
        if (context == null) return;

        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setPreferenceDataStore(GoldBOXWidgetPreferencesDataStore.getInstance(context));

        setPreferencesFromResource(R.xml.goldbox_widget_preferences, rootKey);
    }

}

class GoldBOXWidgetPreferencesDataStore extends PreferenceDataStore {

    private final Context mContext;
    private final GoldBOXWidgetAppSharedPreferences mPreferences;

    private static GoldBOXWidgetPreferencesDataStore mInstance;

    private GoldBOXWidgetPreferencesDataStore(Context context) {
        mContext = context;
        mPreferences = GoldBOXWidgetAppSharedPreferences.build(context, true);
    }

    public static synchronized GoldBOXWidgetPreferencesDataStore getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new GoldBOXWidgetPreferencesDataStore(context);
        }
        return mInstance;
    }

}
