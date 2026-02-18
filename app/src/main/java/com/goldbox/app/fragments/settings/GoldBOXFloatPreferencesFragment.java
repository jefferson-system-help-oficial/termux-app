package com.goldbox.app.fragments.settings;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.goldbox.R;
import com.goldbox.shared.goldbox.settings.preferences.GoldBOXFloatAppSharedPreferences;

@Keep
public class GoldBOXFloatPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getContext();
        if (context == null) return;

        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setPreferenceDataStore(GoldBOXFloatPreferencesDataStore.getInstance(context));

        setPreferencesFromResource(R.xml.goldbox_float_preferences, rootKey);
    }

}

class GoldBOXFloatPreferencesDataStore extends PreferenceDataStore {

    private final Context mContext;
    private final GoldBOXFloatAppSharedPreferences mPreferences;

    private static GoldBOXFloatPreferencesDataStore mInstance;

    private GoldBOXFloatPreferencesDataStore(Context context) {
        mContext = context;
        mPreferences = GoldBOXFloatAppSharedPreferences.build(context, true);
    }

    public static synchronized GoldBOXFloatPreferencesDataStore getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new GoldBOXFloatPreferencesDataStore(context);
        }
        return mInstance;
    }

}
