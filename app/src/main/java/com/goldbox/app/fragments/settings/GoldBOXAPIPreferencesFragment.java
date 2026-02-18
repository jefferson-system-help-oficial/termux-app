package com.goldbox.app.fragments.settings;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.goldbox.R;
import com.goldbox.shared.goldbox.settings.preferences.GoldBOXAPIAppSharedPreferences;

@Keep
public class GoldBOXAPIPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getContext();
        if (context == null) return;

        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setPreferenceDataStore(GoldBOXAPIPreferencesDataStore.getInstance(context));

        setPreferencesFromResource(R.xml.goldbox_api_preferences, rootKey);
    }

}

class GoldBOXAPIPreferencesDataStore extends PreferenceDataStore {

    private final Context mContext;
    private final GoldBOXAPIAppSharedPreferences mPreferences;

    private static GoldBOXAPIPreferencesDataStore mInstance;

    private GoldBOXAPIPreferencesDataStore(Context context) {
        mContext = context;
        mPreferences = GoldBOXAPIAppSharedPreferences.build(context, true);
    }

    public static synchronized GoldBOXAPIPreferencesDataStore getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new GoldBOXAPIPreferencesDataStore(context);
        }
        return mInstance;
    }

}
