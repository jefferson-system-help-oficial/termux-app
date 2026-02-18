package com.goldbox.app.terminal.io;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

import com.goldbox.app.GoldBOXActivity;
import com.goldbox.app.terminal.GoldBOXTerminalSessionActivityClient;
import com.goldbox.app.terminal.GoldBOXTerminalViewClient;
import com.goldbox.shared.logger.Logger;
import com.goldbox.shared.goldbox.extrakeys.ExtraKeysConstants;
import com.goldbox.shared.goldbox.extrakeys.ExtraKeysInfo;
import com.goldbox.shared.goldbox.settings.properties.GoldBOXPropertyConstants;
import com.goldbox.shared.goldbox.settings.properties.GoldBOXSharedProperties;
import com.goldbox.shared.goldbox.terminal.io.TerminalExtraKeys;
import com.goldbox.view.TerminalView;

import org.json.JSONException;

public class GoldBOXTerminalExtraKeys extends TerminalExtraKeys {

    private ExtraKeysInfo mExtraKeysInfo;

    final GoldBOXActivity mActivity;
    final GoldBOXTerminalViewClient mGoldBOXTerminalViewClient;
    final GoldBOXTerminalSessionActivityClient mGoldBOXTerminalSessionActivityClient;

    private static final String LOG_TAG = "GoldBOXTerminalExtraKeys";

    public GoldBOXTerminalExtraKeys(GoldBOXActivity activity, @NonNull TerminalView terminalView,
                                   GoldBOXTerminalViewClient goldboxTerminalViewClient,
                                   GoldBOXTerminalSessionActivityClient goldboxTerminalSessionActivityClient) {
        super(terminalView);

        mActivity = activity;
        mGoldBOXTerminalViewClient = goldboxTerminalViewClient;
        mGoldBOXTerminalSessionActivityClient = goldboxTerminalSessionActivityClient;

        setExtraKeys();
    }


    /**
     * Set the terminal extra keys and style.
     */
    private void setExtraKeys() {
        mExtraKeysInfo = null;

        try {
            // The mMap stores the extra key and style string values while loading properties
            // Check {@link #getExtraKeysInternalPropertyValueFromValue(String)} and
            // {@link #getExtraKeysStyleInternalPropertyValueFromValue(String)}
            String extrakeys = (String) mActivity.getProperties().getInternalPropertyValue(GoldBOXPropertyConstants.KEY_EXTRA_KEYS, true);
            String extraKeysStyle = (String) mActivity.getProperties().getInternalPropertyValue(GoldBOXPropertyConstants.KEY_EXTRA_KEYS_STYLE, true);

            ExtraKeysConstants.ExtraKeyDisplayMap extraKeyDisplayMap = ExtraKeysInfo.getCharDisplayMapForStyle(extraKeysStyle);
            if (ExtraKeysConstants.EXTRA_KEY_DISPLAY_MAPS.DEFAULT_CHAR_DISPLAY.equals(extraKeyDisplayMap) && !GoldBOXPropertyConstants.DEFAULT_IVALUE_EXTRA_KEYS_STYLE.equals(extraKeysStyle)) {
                Logger.logError(GoldBOXSharedProperties.LOG_TAG, "The style \"" + extraKeysStyle + "\" for the key \"" + GoldBOXPropertyConstants.KEY_EXTRA_KEYS_STYLE + "\" is invalid. Using default style instead.");
                extraKeysStyle = GoldBOXPropertyConstants.DEFAULT_IVALUE_EXTRA_KEYS_STYLE;
            }

            mExtraKeysInfo = new ExtraKeysInfo(extrakeys, extraKeysStyle, ExtraKeysConstants.CONTROL_CHARS_ALIASES);
        } catch (JSONException e) {
            Logger.showToast(mActivity, "Could not load and set the \"" + GoldBOXPropertyConstants.KEY_EXTRA_KEYS + "\" property from the properties file: " + e.toString(), true);
            Logger.logStackTraceWithMessage(LOG_TAG, "Could not load and set the \"" + GoldBOXPropertyConstants.KEY_EXTRA_KEYS + "\" property from the properties file: ", e);

            try {
                mExtraKeysInfo = new ExtraKeysInfo(GoldBOXPropertyConstants.DEFAULT_IVALUE_EXTRA_KEYS, GoldBOXPropertyConstants.DEFAULT_IVALUE_EXTRA_KEYS_STYLE, ExtraKeysConstants.CONTROL_CHARS_ALIASES);
            } catch (JSONException e2) {
                Logger.showToast(mActivity, "Can't create default extra keys",true);
                Logger.logStackTraceWithMessage(LOG_TAG, "Could create default extra keys: ", e);
                mExtraKeysInfo = null;
            }
        }
    }

    public ExtraKeysInfo getExtraKeysInfo() {
        return mExtraKeysInfo;
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void onTerminalExtraKeyButtonClick(View view, String key, boolean ctrlDown, boolean altDown, boolean shiftDown, boolean fnDown) {
        if ("KEYBOARD".equals(key)) {
            if(mGoldBOXTerminalViewClient != null)
                mGoldBOXTerminalViewClient.onToggleSoftKeyboardRequest();
        } else if ("DRAWER".equals(key)) {
            DrawerLayout drawerLayout = mGoldBOXTerminalViewClient.getActivity().getDrawer();
            if (drawerLayout.isDrawerOpen(Gravity.LEFT))
                drawerLayout.closeDrawer(Gravity.LEFT);
            else
                drawerLayout.openDrawer(Gravity.LEFT);
        } else if ("PASTE".equals(key)) {
            if(mGoldBOXTerminalSessionActivityClient != null)
                mGoldBOXTerminalSessionActivityClient.onPasteTextFromClipboard(null);
        }  else if ("SCROLL".equals(key)) {
            TerminalView terminalView = mGoldBOXTerminalViewClient.getActivity().getTerminalView();
            if (terminalView != null && terminalView.mEmulator != null)
                terminalView.mEmulator.toggleAutoScrollDisabled();
        } else {
            super.onTerminalExtraKeyButtonClick(view, key, ctrlDown, altDown, shiftDown, fnDown);
        }
    }

}
