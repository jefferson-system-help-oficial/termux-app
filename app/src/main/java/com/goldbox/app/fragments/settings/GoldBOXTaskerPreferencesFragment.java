package com.goldbox.app.fragments.settings;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.goldbox.R;
import com.goldbox.shared.goldbox.settings.preferences.GoldBOXTaskerAppSharedPreferences;

@Keep
public class GoldBOXTaskerPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getContext();
        if (context == null) return;

        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setPreferenceDataStore(GoldBOXTaskerPreferencesDataStore.getInstance(context));

        setPreferencesFromResource(R.xml.goldbox_tasker_preferences, rootKey);
    }

}

class GoldBOXTaskerPreferencesDataStore extends PreferenceDataStore {

    private final Context mContext;
    private final GoldBOXTaskerAppSharedPreferences mPreferences;

    private static GoldBOXTaskerPreferencesDataStore mInstance;

    private GoldBOXTaskerPreferencesDataStore(Context context) {
        mContext = context;
        mPreferences = GoldBOXTaskerAppSharedPreferences.build(context, true);
    }

    public static synchronized GoldBOXTaskerPreferencesDataStore getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new GoldBOXTaskerPreferencesDataStore(context);
        }
        return mInstance;
    }

}
