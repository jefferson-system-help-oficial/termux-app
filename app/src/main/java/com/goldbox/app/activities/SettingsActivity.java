package com.goldbox.app.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.goldbox.R;
import com.goldbox.shared.activities.ReportActivity;
import com.goldbox.shared.file.FileUtils;
import com.goldbox.shared.models.ReportInfo;
import com.goldbox.app.models.UserAction;
import com.goldbox.shared.interact.ShareUtils;
import com.goldbox.shared.android.PackageUtils;
import com.goldbox.shared.goldbox.settings.preferences.GoldBOXAPIAppSharedPreferences;
import com.goldbox.shared.goldbox.settings.preferences.GoldBOXFloatAppSharedPreferences;
import com.goldbox.shared.goldbox.settings.preferences.GoldBOXTaskerAppSharedPreferences;
import com.goldbox.shared.goldbox.settings.preferences.GoldBOXWidgetAppSharedPreferences;
import com.goldbox.shared.android.AndroidUtils;
import com.goldbox.shared.goldbox.GoldBOXConstants;
import com.goldbox.shared.goldbox.GoldBOXUtils;
import com.goldbox.shared.activity.media.AppCompatActivityUtils;
import com.goldbox.shared.theme.NightMode;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatActivityUtils.setNightMode(this, NightMode.getAppNightMode().getName(), true);

        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new RootPreferencesFragment())
                .commit();
        }

        AppCompatActivityUtils.setToolbar(this, com.goldbox.shared.R.id.toolbar);
        AppCompatActivityUtils.setShowBackButtonInActionBar(this, true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public static class RootPreferencesFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            Context context = getContext();
            if (context == null) return;

            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            new Thread() {
                @Override
                public void run() {
                    configureGoldBOXAPIPreference(context);
                    configureGoldBOXFloatPreference(context);
                    configureGoldBOXTaskerPreference(context);
                    configureGoldBOXWidgetPreference(context);
                    configureAboutPreference(context);
                    configureDonatePreference(context);
                }
            }.start();
        }

        private void configureGoldBOXAPIPreference(@NonNull Context context) {
            Preference goldboxAPIPreference = findPreference("goldbox_api");
            if (goldboxAPIPreference != null) {
                GoldBOXAPIAppSharedPreferences preferences = GoldBOXAPIAppSharedPreferences.build(context, false);
                // If failed to get app preferences, then likely app is not installed, so do not show its preference
                goldboxAPIPreference.setVisible(preferences != null);
            }
        }

        private void configureGoldBOXFloatPreference(@NonNull Context context) {
            Preference goldboxFloatPreference = findPreference("goldbox_float");
            if (goldboxFloatPreference != null) {
                GoldBOXFloatAppSharedPreferences preferences = GoldBOXFloatAppSharedPreferences.build(context, false);
                // If failed to get app preferences, then likely app is not installed, so do not show its preference
                goldboxFloatPreference.setVisible(preferences != null);
            }
        }

        private void configureGoldBOXTaskerPreference(@NonNull Context context) {
            Preference goldboxTaskerPreference = findPreference("goldbox_tasker");
            if (goldboxTaskerPreference != null) {
                GoldBOXTaskerAppSharedPreferences preferences = GoldBOXTaskerAppSharedPreferences.build(context, false);
                // If failed to get app preferences, then likely app is not installed, so do not show its preference
                goldboxTaskerPreference.setVisible(preferences != null);
            }
        }

        private void configureGoldBOXWidgetPreference(@NonNull Context context) {
            Preference goldboxWidgetPreference = findPreference("goldbox_widget");
            if (goldboxWidgetPreference != null) {
                GoldBOXWidgetAppSharedPreferences preferences = GoldBOXWidgetAppSharedPreferences.build(context, false);
                // If failed to get app preferences, then likely app is not installed, so do not show its preference
                goldboxWidgetPreference.setVisible(preferences != null);
            }
        }

        private void configureAboutPreference(@NonNull Context context) {
            Preference aboutPreference = findPreference("about");
            if (aboutPreference != null) {
                aboutPreference.setOnPreferenceClickListener(preference -> {
                    new Thread() {
                        @Override
                        public void run() {
                            String title = "About";

                            StringBuilder aboutString = new StringBuilder();
                            aboutString.append(GoldBOXUtils.getAppInfoMarkdownString(context, GoldBOXUtils.AppInfoMode.GOLDBOX_AND_PLUGIN_PACKAGES));
                            aboutString.append("\n\n").append(AndroidUtils.getDeviceInfoMarkdownString(context, true));
                            aboutString.append("\n\n").append(GoldBOXUtils.getImportantLinksMarkdownString(context));

                            String userActionName = UserAction.ABOUT.getName();

                            ReportInfo reportInfo = new ReportInfo(userActionName,
                                GoldBOXConstants.GOLDBOX_APP.GOLDBOX_SETTINGS_ACTIVITY_NAME, title);
                            reportInfo.setReportString(aboutString.toString());
                            reportInfo.setReportSaveFileLabelAndPath(userActionName,
                                Environment.getExternalStorageDirectory() + "/" +
                                    FileUtils.sanitizeFileName(GoldBOXConstants.GOLDBOX_APP_NAME + "-" + userActionName + ".log", true, true));

                            ReportActivity.startReportActivity(context, reportInfo);
                        }
                    }.start();

                    return true;
                });
            }
        }

        private void configureDonatePreference(@NonNull Context context) {
            Preference donatePreference = findPreference("donate");
            if (donatePreference != null) {
                String signingCertificateSHA256Digest = PackageUtils.getSigningCertificateSHA256DigestForPackage(context);
                if (signingCertificateSHA256Digest != null) {
                    // If APK is a Google Playstore release, then do not show the donation link
                    // since GoldBOX isn't exempted from the playstore policy donation links restriction
                    // Check Fund solicitations: https://pay.google.com/intl/en_in/about/policy/
                    String apkRelease = GoldBOXUtils.getAPKRelease(signingCertificateSHA256Digest);
                    if (apkRelease == null || apkRelease.equals(GoldBOXConstants.APK_RELEASE_GOOGLE_PLAYSTORE_SIGNING_CERTIFICATE_SHA256_DIGEST)) {
                        donatePreference.setVisible(false);
                        return;
                    } else {
                        donatePreference.setVisible(true);
                    }
                }

                donatePreference.setOnPreferenceClickListener(preference -> {
                    ShareUtils.openUrl(context, GoldBOXConstants.GOLDBOX_DONATE_URL);
                    return true;
                });
            }
        }
    }

}
