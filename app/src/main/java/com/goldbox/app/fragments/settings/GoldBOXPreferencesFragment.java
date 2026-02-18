package com.goldbox.app.fragments.settings;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.goldbox.R;
import com.goldbox.shared.goldbox.settings.preferences.GoldBOXAppSharedPreferences;

@Keep
public class GoldBOXPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getContext();
        if (context == null) return;

        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setPreferenceDataStore(GoldBOXPreferencesDataStore.getInstance(context));

        setPreferencesFromResource(R.xml.goldbox_preferences, rootKey);
    }

}

class GoldBOXPreferencesDataStore extends PreferenceDataStore {

    private final Context mContext;
    private final GoldBOXAppSharedPreferences mPreferences;

    private static GoldBOXPreferencesDataStore mInstance;

    private GoldBOXPreferencesDataStore(Context context) {
        mContext = context;
        mPreferences = GoldBOXAppSharedPreferences.build(context, true);
    }

    public static synchronized GoldBOXPreferencesDataStore getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new GoldBOXPreferencesDataStore(context);
        }
        return mInstance;
    }

}
