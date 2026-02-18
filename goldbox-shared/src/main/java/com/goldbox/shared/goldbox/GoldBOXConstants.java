package com.goldbox.shared.goldbox;

import android.annotation.SuppressLint;
import android.content.Intent;

import com.goldbox.shared.shell.command.ExecutionCommand;
import com.goldbox.shared.shell.command.ExecutionCommand.Runner;

import java.io.File;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;

/*
 * Version: v0.53.0
 * SPDX-License-Identifier: MIT
 *
 * Changelog
 *
 * - 0.1.0 (2021-03-08)
 *      - Initial Release.
 *
 * - 0.2.0 (2021-03-11)
 *      - Added `_DIR` and `_FILE` substrings to paths.
 *      - Added `INTERNAL_PRIVATE_APP_DATA_DIR*`, `GOLDBOX_CACHE_DIR*`, `GOLDBOX_DATABASES_DIR*`,
 *          `GOLDBOX_SHARED_PREFERENCES_DIR*`, `GOLDBOX_BIN_PREFIX_DIR*`, `GOLDBOX_ETC_DIR*`,
 *          `GOLDBOX_INCLUDE_DIR*`, `GOLDBOX_LIB_DIR*`, `GOLDBOX_LIBEXEC_DIR*`, `GOLDBOX_SHARE_DIR*`,
 *          `GOLDBOX_TMP_DIR*`, `GOLDBOX_VAR_DIR*`, `GOLDBOX_STAGING_PREFIX_DIR*`,
 *          `GOLDBOX_STORAGE_HOME_DIR*`, `GOLDBOX_DEFAULT_PREFERENCES_FILE_BASENAME*`,
 *          `GOLDBOX_DEFAULT_PREFERENCES_FILE`.
 *      - Renamed `DATA_HOME_PATH` to `GOLDBOX_DATA_HOME_DIR_PATH`.
 *      - Renamed `CONFIG_HOME_PATH` to `GOLDBOX_CONFIG_HOME_DIR_PATH`.
 *      - Updated javadocs and spacing.
 *
 * - 0.3.0 (2021-03-12)
 *      - Remove `GOLDBOX_CACHE_DIR_PATH*`, `GOLDBOX_DATABASES_DIR_PATH*`,
 *          `GOLDBOX_SHARED_PREFERENCES_DIR_PATH*` since they may not be consistent on all devices.
 *      - Renamed `GOLDBOX_DEFAULT_PREFERENCES_FILE_BASENAME` to
 *          `GOLDBOX_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION`. This should be used for
 *           accessing shared preferences between GoldBOX app and its plugins if ever needed by first
 *           getting shared package context with {@link Context.createPackageContext(String,int}).
 *
 * - 0.4.0 (2021-03-16)
 *      - Added `BROADCAST_GOLDBOX_OPENED`,
 *          `GOLDBOX_API_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION`
 *          `GOLDBOX_BOOT_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION`,
 *          `GOLDBOX_FLOAT_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION`,
 *          `GOLDBOX_STYLING_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION`,
 *          `GOLDBOX_TASKER_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION`,
 *          `GOLDBOX_WIDGET_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION`.
 *
 * - 0.5.0 (2021-03-16)
 *      - Renamed "GoldBOX Plugin app" labels to "GoldBOX:Tasker app".
 *
 * - 0.6.0 (2021-03-16)
 *      - Added `GOLDBOX_FILE_SHARE_URI_AUTHORITY`.
 *
 * - 0.7.0 (2021-03-17)
 *      - Fixed javadocs.
 *
 * - 0.8.0 (2021-03-18)
 *      - Fixed Intent extra types javadocs.
 *      - Added following to `GOLDBOX_SERVICE`:
 *          `EXTRA_PENDING_INTENT`, `EXTRA_RESULT_BUNDLE`,
 *          `EXTRA_STDOUT`, `EXTRA_STDERR`, `EXTRA_EXIT_CODE`,
 *          `EXTRA_ERR`, `EXTRA_ERRMSG`.
 *
 * - 0.9.0 (2021-03-18)
 *      - Fixed javadocs.
 *
 * - 0.10.0 (2021-03-19)
 *      - Added following to `GOLDBOX_SERVICE`:
 *          `EXTRA_SESSION_ACTION`,
 *          `VALUE_EXTRA_SESSION_ACTION_SWITCH_TO_NEW_SESSION_AND_OPEN_ACTIVITY`,
 *          `VALUE_EXTRA_SESSION_ACTION_KEEP_CURRENT_SESSION_AND_OPEN_ACTIVITY`,
 *          `VALUE_EXTRA_SESSION_ACTION_SWITCH_TO_NEW_SESSION_AND_DONT_OPEN_ACTIVITY`
 *          `VALUE_EXTRA_SESSION_ACTION_KEEP_CURRENT_SESSION_AND_DONT_OPEN_ACTIVITY`.
 *      - Added following to `RUN_COMMAND_SERVICE`:
 *          `EXTRA_SESSION_ACTION`.
 *
 * - 0.11.0 (2021-03-24)
 *      - Added following to `GOLDBOX_SERVICE`:
 *          `EXTRA_COMMAND_LABEL`, `EXTRA_COMMAND_DESCRIPTION`, `EXTRA_COMMAND_HELP`, `EXTRA_PLUGIN_API_HELP`.
 *      - Added following to `RUN_COMMAND_SERVICE`:
 *          `EXTRA_COMMAND_LABEL`, `EXTRA_COMMAND_DESCRIPTION`, `EXTRA_COMMAND_HELP`.
 *      - Updated `RESULT_BUNDLE` related extras with `PLUGIN_RESULT_BUNDLE` prefixes.
 *
 * - 0.12.0 (2021-03-25)
 *      - Added following to `GOLDBOX_SERVICE`:
 *          `EXTRA_PLUGIN_RESULT_BUNDLE_STDOUT_ORIGINAL_LENGTH`,
 *          `EXTRA_PLUGIN_RESULT_BUNDLE_STDERR_ORIGINAL_LENGTH`.
 *
 * - 0.13.0 (2021-03-25)
 *      - Added following to `RUN_COMMAND_SERVICE`:
 *          `EXTRA_PENDING_INTENT`.
 *
 * - 0.14.0 (2021-03-25)
 *      - Added `FDROID_PACKAGES_BASE_URL`,
 *          `GOLDBOX_GITHUB_ORGANIZATION_NAME`, `GOLDBOX_GITHUB_ORGANIZATION_URL`,
 *          `GOLDBOX_GITHUB_REPO_NAME`, `GOLDBOX_GITHUB_REPO_URL`, `GOLDBOX_FDROID_PACKAGE_URL`,
 *          `GOLDBOX_API_GITHUB_REPO_NAME`,`GOLDBOX_API_GITHUB_REPO_URL`, `GOLDBOX_API_FDROID_PACKAGE_URL`,
 *          `GOLDBOX_BOOT_GITHUB_REPO_NAME`, `GOLDBOX_BOOT_GITHUB_REPO_URL`, `GOLDBOX_BOOT_FDROID_PACKAGE_URL`,
 *          `GOLDBOX_FLOAT_GITHUB_REPO_NAME`, `GOLDBOX_FLOAT_GITHUB_REPO_URL`, `GOLDBOX_FLOAT_FDROID_PACKAGE_URL`,
 *          `GOLDBOX_STYLING_GITHUB_REPO_NAME`, `GOLDBOX_STYLING_GITHUB_REPO_URL`, `GOLDBOX_STYLING_FDROID_PACKAGE_URL`,
 *          `GOLDBOX_TASKER_GITHUB_REPO_NAME`, `GOLDBOX_TASKER_GITHUB_REPO_URL`, `GOLDBOX_TASKER_FDROID_PACKAGE_URL`,
 *          `GOLDBOX_WIDGET_GITHUB_REPO_NAME`, `GOLDBOX_WIDGET_GITHUB_REPO_URL` `GOLDBOX_WIDGET_FDROID_PACKAGE_URL`.
 *
 * - 0.15.0 (2021-04-06)
 *      - Fixed some variables that had `PREFIX_` substring missing in their name.
 *      - Added `GOLDBOX_CRASH_LOG_FILE_PATH`, `GOLDBOX_CRASH_LOG_BACKUP_FILE_PATH`,
 *          `GOLDBOX_GITHUB_ISSUES_REPO_URL`, `GOLDBOX_API_GITHUB_ISSUES_REPO_URL`,
 *          `GOLDBOX_BOOT_GITHUB_ISSUES_REPO_URL`, `GOLDBOX_FLOAT_GITHUB_ISSUES_REPO_URL`,
 *          `GOLDBOX_STYLING_GITHUB_ISSUES_REPO_URL`, `GOLDBOX_TASKER_GITHUB_ISSUES_REPO_URL`,
 *          `GOLDBOX_WIDGET_GITHUB_ISSUES_REPO_URL`,
 *          `GOLDBOX_GITHUB_WIKI_REPO_URL`, `GOLDBOX_PACKAGES_GITHUB_WIKI_REPO_URL`,
 *          `GOLDBOX_PACKAGES_GITHUB_REPO_NAME`, `GOLDBOX_PACKAGES_GITHUB_REPO_URL`, `GOLDBOX_PACKAGES_GITHUB_ISSUES_REPO_URL`,
 *          `GOLDBOX_GAME_PACKAGES_GITHUB_REPO_NAME`, `GOLDBOX_GAME_PACKAGES_GITHUB_REPO_URL`, `GOLDBOX_GAME_PACKAGES_GITHUB_ISSUES_REPO_URL`,
 *          `GOLDBOX_SCIENCE_PACKAGES_GITHUB_REPO_NAME`, `GOLDBOX_SCIENCE_PACKAGES_GITHUB_REPO_URL`, `GOLDBOX_SCIENCE_PACKAGES_GITHUB_ISSUES_REPO_URL`,
 *          `GOLDBOX_ROOT_PACKAGES_GITHUB_REPO_NAME`, `GOLDBOX_ROOT_PACKAGES_GITHUB_REPO_URL`, `GOLDBOX_ROOT_PACKAGES_GITHUB_ISSUES_REPO_URL`,
 *          `GOLDBOX_UNSTABLE_PACKAGES_GITHUB_REPO_NAME`, `GOLDBOX_UNSTABLE_PACKAGES_GITHUB_REPO_URL`, `GOLDBOX_UNSTABLE_PACKAGES_GITHUB_ISSUES_REPO_URL`,
 *          `GOLDBOX_X11_PACKAGES_GITHUB_REPO_NAME`, `GOLDBOX_X11_PACKAGES_GITHUB_REPO_URL`, `GOLDBOX_X11_PACKAGES_GITHUB_ISSUES_REPO_URL`.
 *      - Added following to `RUN_COMMAND_SERVICE`:
 *          `RUN_COMMAND_API_HELP_URL`.
 *
 * - 0.16.0 (2021-04-06)
 *      - Added `GOLDBOX_SUPPORT_EMAIL`, `GOLDBOX_SUPPORT_EMAIL_URL`, `GOLDBOX_SUPPORT_EMAIL_MAILTO_URL`,
 *          `GOLDBOX_REDDIT_SUBREDDIT`, `GOLDBOX_REDDIT_SUBREDDIT_URL`.
 *      - The `GOLDBOX_SUPPORT_EMAIL_URL` value must be fixed later when email has been set up.
 *
 * - 0.17.0 (2021-04-07)
 *      - Added `GOLDBOX_APP_NOTIFICATION_CHANNEL_ID`, `GOLDBOX_APP_NOTIFICATION_CHANNEL_NAME`, `GOLDBOX_APP_NOTIFICATION_ID`,
 *          `GOLDBOX_RUN_COMMAND_NOTIFICATION_CHANNEL_ID`, `GOLDBOX_RUN_COMMAND_NOTIFICATION_CHANNEL_NAME`, `GOLDBOX_RUN_COMMAND_NOTIFICATION_ID`,
 *          `GOLDBOX_PLUGIN_COMMAND_ERRORS_NOTIFICATION_CHANNEL_ID`, `GOLDBOX_PLUGIN_COMMAND_ERRORS_NOTIFICATION_CHANNEL_NAME`,
 *          `GOLDBOX_CRASH_REPORTS_NOTIFICATION_CHANNEL_ID`, `GOLDBOX_CRASH_REPORTS_NOTIFICATION_CHANNEL_NAME`.
 *      - Updated javadocs.
 *
 * - 0.18.0 (2021-04-11)
 *      - Updated `GOLDBOX_SUPPORT_EMAIL_URL` to a valid email.
 *      - Removed `GOLDBOX_SUPPORT_EMAIL`.
 *
 * - 0.19.0 (2021-04-12)
 *      - Added `GOLDBOX_ACTIVITY.ACTION_REQUEST_PERMISSIONS`.
 *      - Added `GOLDBOX_SERVICE.EXTRA_STDIN`.
 *      - Added `RUN_COMMAND_SERVICE.EXTRA_STDIN`.
 *      - Deprecated `GOLDBOX_ACTIVITY.EXTRA_RELOAD_STYLE`.
 *
 * - 0.20.0 (2021-05-13)
 *      - Added `GOLDBOX_WIKI`, `GOLDBOX_WIKI_URL`, `GOLDBOX_PLUGIN_APP_NAMES_LIST`, `GOLDBOX_PLUGIN_APP_PACKAGE_NAMES_LIST`.
 *      - Added `GOLDBOX_SETTINGS_ACTIVITY_NAME`.
 *
 * - 0.21.0 (2021-05-13)
 *      - Added `APK_RELEASE_FDROID`, `APK_RELEASE_FDROID_SIGNING_CERTIFICATE_SHA256_DIGEST`,
 *          `APK_RELEASE_GITHUB_DEBUG_BUILD`, `APK_RELEASE_GITHUB_DEBUG_BUILD_SIGNING_CERTIFICATE_SHA256_DIGEST`,
 *          `APK_RELEASE_GOOGLE_PLAYSTORE`, `APK_RELEASE_GOOGLE_PLAYSTORE_SIGNING_CERTIFICATE_SHA256_DIGEST`.
 *
 * - 0.22.0 (2021-05-13)
 *      - Added `GOLDBOX_DONATE_URL`.
 *
 * - 0.23.0 (2021-06-12)
 *      - Rename `INTERNAL_PRIVATE_APP_DATA_DIR_PATH` to `GOLDBOX_INTERNAL_PRIVATE_APP_DATA_DIR_PATH`.
 *
 * - 0.24.0 (2021-06-27)
 *      - Add `COMMA_NORMAL`, `COMMA_ALTERNATIVE`.
 *      - Added following to `GOLDBOX_APP.GOLDBOX_SERVICE`:
 *          `EXTRA_RESULT_DIRECTORY`, `EXTRA_RESULT_SINGLE_FILE`, `EXTRA_RESULT_FILE_BASENAME`,
 *          `EXTRA_RESULT_FILE_OUTPUT_FORMAT`, `EXTRA_RESULT_FILE_ERROR_FORMAT`, `EXTRA_RESULT_FILES_SUFFIX`.
 *      - Added following to `GOLDBOX_APP.RUN_COMMAND_SERVICE`:
 *          `EXTRA_RESULT_DIRECTORY`, `EXTRA_RESULT_SINGLE_FILE`, `EXTRA_RESULT_FILE_BASENAME`,
 *          `EXTRA_RESULT_FILE_OUTPUT_FORMAT`, `EXTRA_RESULT_FILE_ERROR_FORMAT`, `EXTRA_RESULT_FILES_SUFFIX`,
 *          `EXTRA_REPLACE_COMMA_ALTERNATIVE_CHARS_IN_ARGUMENTS`, `EXTRA_COMMA_ALTERNATIVE_CHARS_IN_ARGUMENTS`.
 *      - Added following to `RESULT_SENDER`:
 *           `FORMAT_SUCCESS_STDOUT`, `FORMAT_SUCCESS_STDOUT__EXIT_CODE`, `FORMAT_SUCCESS_STDOUT__STDERR__EXIT_CODE`
 *           `FORMAT_FAILED_ERR__ERRMSG__STDOUT__STDERR__EXIT_CODE`,
 *           `RESULT_FILE_ERR_PREFIX`, `RESULT_FILE_ERRMSG_PREFIX` `RESULT_FILE_STDOUT_PREFIX`,
 *           `RESULT_FILE_STDERR_PREFIX`, `RESULT_FILE_EXIT_CODE_PREFIX`.
 *
 * - 0.25.0 (2021-08-19)
 *      - Added following to `GOLDBOX_APP.GOLDBOX_SERVICE`:
 *          `EXTRA_BACKGROUND_CUSTOM_LOG_LEVEL`.
 *      - Added following to `GOLDBOX_APP.RUN_COMMAND_SERVICE`:
 *          `EXTRA_BACKGROUND_CUSTOM_LOG_LEVEL`.
 *
 * - 0.26.0 (2021-08-25)
 *      - Changed `GOLDBOX_ACTIVITY.ACTION_FAILSAFE_SESSION` to `GOLDBOX_ACTIVITY.EXTRA_FAILSAFE_SESSION`.
 *
 * - 0.27.0 (2021-09-02)
 *      - Added `GOLDBOX_FLOAT_APP_NOTIFICATION_CHANNEL_ID`, `GOLDBOX_FLOAT_APP_NOTIFICATION_CHANNEL_NAME`,
 *          `GOLDBOX_FLOAT_APP.GOLDBOX_FLOAT_SERVICE_NAME`.
 *      - Added following to `GOLDBOX_FLOAT_APP.GOLDBOX_FLOAT_SERVICE`:
 *          `ACTION_STOP_SERVICE`, `ACTION_SHOW`, `ACTION_HIDE`.
 *
 * - 0.28.0 (2021-09-02)
 *      - Added `GOLDBOX_FLOAT_PROPERTIES_PRIMARY_FILE*` and `GOLDBOX_FLOAT_PROPERTIES_SECONDARY_FILE*`.
 *
 * - 0.29.0 (2021-09-04)
 *      - Added `GOLDBOX_SHORTCUT_TASKS_SCRIPTS_DIR_BASENAME`, `GOLDBOX_SHORTCUT_SCRIPT_ICONS_DIR_BASENAME`,
 *          `GOLDBOX_SHORTCUT_SCRIPT_ICONS_DIR_PATH`, `GOLDBOX_SHORTCUT_SCRIPT_ICONS_DIR`.
 *      - Added following to `GOLDBOX_WIDGET.GOLDBOX_WIDGET_PROVIDER`:
 *          `ACTION_WIDGET_ITEM_CLICKED`, `ACTION_REFRESH_WIDGET`, `EXTRA_FILE_CLICKED`.
 *      - Changed naming convention of `GOLDBOX_FLOAT_APP.GOLDBOX_FLOAT_SERVICE.ACTION_*`.
 *      - Fixed wrong path set for `GOLDBOX_SHORTCUT_SCRIPTS_DIR_PATH`.
 *
 * - 0.30.0 (2021-09-08)
 *      - Changed `APK_RELEASE_GITHUB_DEBUG_BUILD`to `APK_RELEASE_GITHUB` and
 *          `APK_RELEASE_GITHUB_DEBUG_BUILD_SIGNING_CERTIFICATE_SHA256_DIGEST` to
 *          `APK_RELEASE_GITHUB_SIGNING_CERTIFICATE_SHA256_DIGEST`.
 *
 * - 0.31.0 (2021-09-09)
 *      - Added following to `GOLDBOX_APP.GOLDBOX_SERVICE`:
 *          `MIN_VALUE_EXTRA_SESSION_ACTION` and `MAX_VALUE_EXTRA_SESSION_ACTION`.
 *
 * - 0.32.0 (2021-09-23)
 *      - Added `GOLDBOX_API.GOLDBOX_API_ACTIVITY_NAME`, `GOLDBOX_TASKER.GOLDBOX_TASKER_ACTIVITY_NAME`
 *          and `GOLDBOX_WIDGET.GOLDBOX_WIDGET_ACTIVITY_NAME`.
 *
 * - 0.33.0 (2021-10-08)
 *      - Added `GOLDBOX_PROPERTIES_FILE_PATHS_LIST` and `GOLDBOX_FLOAT_PROPERTIES_FILE_PATHS_LIST`.
 *
 * - 0.34.0 (2021-10-26)
 *      - Move `RESULT_SENDER` to `com.goldbox.shared.shell.command.ShellCommandConstants`.
 *
 * - 0.35.0 (2022-01-28)
 *      - Add `GOLDBOX_APP.GOLDBOX_ACTIVITY.EXTRA_RECREATE_ACTIVITY`.
 *
 * - 0.36.0 (2022-03-10)
 *      - Added `GOLDBOX_APP.GOLDBOX_SERVICE.EXTRA_RUNNER` and `GOLDBOX_APP.RUN_COMMAND_SERVICE.EXTRA_RUNNER`
 *
 * - 0.37.0 (2022-03-15)
 *  - Added `GOLDBOX_API_APT_*`.
 *
 * - 0.38.0 (2022-03-16)
 *      - Added `GOLDBOX_APP.GOLDBOX_ACTIVITY.ACTION_NOTIFY_APP_CRASH`.
 *
 * - 0.39.0 (2022-03-18)
 *      - Added `GOLDBOX_APP.GOLDBOX_SERVICE.EXTRA_SESSION_NAME`, `GOLDBOX_APP.RUN_COMMAND_SERVICE.EXTRA_SESSION_NAME`,
 *          `GOLDBOX_APP.GOLDBOX_SERVICE.EXTRA_SESSION_CREATE_MODE` and `GOLDBOX_APP.RUN_COMMAND_SERVICE.EXTRA_SESSION_CREATE_MODE`.
 *
 * - 0.40.0 (2022-04-17)
 *      - Added `GOLDBOX_APPS_DIR_PATH` and `GOLDBOX_APP.APPS_DIR_PATH`.
 *
 * - 0.41.0 (2022-04-17)
 *      - Added `GOLDBOX_APP.GOLDBOX_AM_SOCKET_FILE_PATH`.
 *
 * - 0.42.0 (2022-04-29)
 *      - Added `APK_RELEASE_GOLDBOX_DEVS` and `APK_RELEASE_GOLDBOX_DEVS_SIGNING_CERTIFICATE_SHA256_DIGEST`.
 *
 * - 0.43.0 (2022-05-29)
 *      - Changed `GOLDBOX_SUPPORT_EMAIL_URL` to support@goldbox.dev.
 *
 * - 0.44.0 (2022-05-29)
 *      - Changed `GOLDBOX_APP.APPS_DIR_PATH` basename from `goldbox-app` to `com.goldbox`.
 *
 * - 0.45.0 (2022-06-01)
 *      - Added `GOLDBOX_APP.BUILD_CONFIG_CLASS_NAME`.
 *
 * - 0.46.0 (2022-06-03)
 *      - Rename `GOLDBOX_APP.GOLDBOX_SERVICE.EXTRA_SESSION_NAME` to `*.EXTRA_SHELL_NAME`,
 *          `GOLDBOX_APP.RUN_COMMAND_SERVICE.EXTRA_SESSION_NAME` to `*.EXTRA_SHELL_NAME`,
 *          `GOLDBOX_APP.GOLDBOX_SERVICE.EXTRA_SESSION_CREATE_MODE` to `*.EXTRA_SHELL_CREATE_MODE` and
 *          `GOLDBOX_APP.RUN_COMMAND_SERVICE.EXTRA_SESSION_CREATE_MODE` to `*.EXTRA_SHELL_CREATE_MODE`.
 *
 * - 0.47.0 (2022-06-04)
 *      - Added `GOLDBOX_SITE` and `GOLDBOX_SITE_URL`.
 *      - Changed `GOLDBOX_DONATE_URL`.
 *
 * - 0.48.0 (2022-06-04)
 *      - Removed `GOLDBOX_GAME_PACKAGES_GITHUB_*`, `GOLDBOX_SCIENCE_PACKAGES_GITHUB_*`,
 *          `GOLDBOX_ROOT_PACKAGES_GITHUB_*`, `GOLDBOX_UNSTABLE_PACKAGES_GITHUB_*`
 *
 * - 0.49.0 (2022-06-11)
 *      - Added `GOLDBOX_ENV_PREFIX_ROOT`.
 *
 * - 0.50.0 (2022-06-11)
 *      - Added `GOLDBOX_CONFIG_PREFIX_DIR_PATH`, `GOLDBOX_ENV_FILE_PATH` and `GOLDBOX_ENV_TEMP_FILE_PATH`.
 *
 * - 0.51.0 (2022-06-13)
 *      - Added `GOLDBOX_APP.FILE_SHARE_RECEIVER_ACTIVITY_CLASS_NAME` and `GOLDBOX_APP.FILE_VIEW_RECEIVER_ACTIVITY_CLASS_NAME`.
 *
 * - 0.52.0 (2022-06-18)
 *      - Added `GOLDBOX_PREFIX_DIR_IGNORED_SUB_FILES_PATHS_TO_CONSIDER_AS_EMPTY`.
 *
 * - 0.53.0 (2025-01-12)
 *      - Renamed `GOLDBOX_API`, `GOLDBOX_STYLING`, `GOLDBOX_TASKER`, `GOLDBOX_WIDGET` classes with `_APP` suffix added.
 *      - Added `GOLDBOX_*_MAIN_ACTIVITY_NAME` and `GOLDBOX_*_LAUNCHER_ACTIVITY_NAME` constants to each app class.
 */

/**
 * A class that defines shared constants of the GoldBOX app and its plugins.
 * This class will be hosted by goldbox-shared lib and should be imported by other goldbox plugin
 * apps as is instead of copying constants to random classes. The 3rd party apps can also import
 * it for interacting with goldbox apps. If changes are made to this file, increment the version number
 * and add an entry in the Changelog section above.
 *
 * GoldBOX app default package name is "com.goldbox" and is used in {@link #GOLDBOX_PREFIX_DIR_PATH}.
 * The binaries compiled for goldbox have {@link #GOLDBOX_PREFIX_DIR_PATH} hardcoded in them but it
 * can be changed during compilation.
 *
 * The {@link #GOLDBOX_PACKAGE_NAME} must be the same as the applicationId of goldbox-app build.gradle
 * since its also used by {@link #GOLDBOX_FILES_DIR_PATH}.
 * If {@link #GOLDBOX_PACKAGE_NAME} is changed, then binaries, specially used in bootstrap need to be
 * compiled appropriately. Check https://github.com.goldbox/goldbox-packages/wiki/Building-packages
 * for more info.
 *
 * Ideally the only places where changes should be required if changing package name are the following:
 * - The {@link #GOLDBOX_PACKAGE_NAME} in {@link GoldBOXConstants}.
 * - The "applicationId" in "build.gradle" of goldbox-app. This is package name that android and app
 *      stores will use and is also the final package name stored in "AndroidManifest.xml".
 * - The "manifestPlaceholders" values for {@link #GOLDBOX_PACKAGE_NAME} and *_APP_NAME in
 *      "build.gradle" of goldbox-app.
 * - The "ENTITY" values for {@link #GOLDBOX_PACKAGE_NAME} and *_APP_NAME in "strings.xml" of
 *      goldbox-app and of goldbox-shared.
 * - The "shortcut.xml" and "*_preferences.xml" files of goldbox-app since dynamic variables don't
 *      work in it.
 * - Optionally the "package" in "AndroidManifest.xml" if modifying project structure of goldbox-app.
 *      This is package name for java classes project structure and is prefixed if activity and service
 *      names use dot (.) notation. This is currently not advisable since this will break lot of
 *      stuff, including goldbox-* packages.
 * - Optionally the *_PATH variables in {@link GoldBOXConstants} containing the string "goldbox".
 *
 * Check https://developer.android.com/studio/build/application-id for info on "package" in
 * "AndroidManifest.xml" and "applicationId" in "build.gradle".
 *
 * The {@link #GOLDBOX_PACKAGE_NAME} must be used in source code of GoldBOX app and its plugins instead
 * of hardcoded "com.goldbox" paths.
 */
public final class GoldBOXConstants {


    /*
     * GoldBOX organization variables.
     */

    /** GoldBOX GitHub organization name */
    public static final String GOLDBOX_GITHUB_ORGANIZATION_NAME = "goldbox"; // Default: "goldbox"
    /** GoldBOX GitHub organization url */
    public static final String GOLDBOX_GITHUB_ORGANIZATION_URL = "https://github.com" + "/" + GOLDBOX_GITHUB_ORGANIZATION_NAME; // Default: "https://github.com.goldbox"

    /** F-Droid packages base url */
    public static final String FDROID_PACKAGES_BASE_URL = "https://f-droid.org/en/packages"; // Default: "https://f-droid.org/en/packages"





    /*
     * GoldBOX and its plugin app and package names and urls.
     */

    /** GoldBOX app name */
    public static final String GOLDBOX_APP_NAME = "GoldBOX"; // Default: "GoldBOX"
    /** GoldBOX package name */
    public static final String GOLDBOX_PACKAGE_NAME = "com.goldbox"; // Default: "com.goldbox"
    /** GoldBOX GitHub repo name */
    public static final String GOLDBOX_GITHUB_REPO_NAME = "goldbox-app"; // Default: "goldbox-app"
    /** GoldBOX GitHub repo url */
    public static final String GOLDBOX_GITHUB_REPO_URL = GOLDBOX_GITHUB_ORGANIZATION_URL + "/" + GOLDBOX_GITHUB_REPO_NAME; // Default: "https://github.com.goldbox/goldbox-app"
    /** GoldBOX GitHub issues repo url */
    public static final String GOLDBOX_GITHUB_ISSUES_REPO_URL = GOLDBOX_GITHUB_REPO_URL + "/issues"; // Default: "https://github.com.goldbox/goldbox-app/issues"
    /** GoldBOX F-Droid package url */
    public static final String GOLDBOX_FDROID_PACKAGE_URL = FDROID_PACKAGES_BASE_URL + "/" + GOLDBOX_PACKAGE_NAME; // Default: "https://f-droid.org/en/packages/com.goldbox"


    /** GoldBOX:API app name */
    public static final String GOLDBOX_API_APP_NAME = "GoldBOX:API"; // Default: "GoldBOX:API"
    /** GoldBOX:API app package name */
    public static final String GOLDBOX_API_PACKAGE_NAME = GOLDBOX_PACKAGE_NAME + ".api"; // Default: "com.goldbox.api"
    /** GoldBOX:API GitHub repo name */
    public static final String GOLDBOX_API_GITHUB_REPO_NAME = "goldbox-api"; // Default: "goldbox-api"
    /** GoldBOX:API GitHub repo url */
    public static final String GOLDBOX_API_GITHUB_REPO_URL = GOLDBOX_GITHUB_ORGANIZATION_URL + "/" + GOLDBOX_API_GITHUB_REPO_NAME; // Default: "https://github.com.goldbox/goldbox-api"
    /** GoldBOX:API GitHub issues repo url */
    public static final String GOLDBOX_API_GITHUB_ISSUES_REPO_URL = GOLDBOX_API_GITHUB_REPO_URL + "/issues"; // Default: "https://github.com.goldbox/goldbox-api/issues"
    /** GoldBOX:API F-Droid package url */
    public static final String GOLDBOX_API_FDROID_PACKAGE_URL = FDROID_PACKAGES_BASE_URL + "/" + GOLDBOX_API_PACKAGE_NAME; // Default: "https://f-droid.org/en/packages/com.goldbox.api"


    /** GoldBOX:Boot app name */
    public static final String GOLDBOX_BOOT_APP_NAME = "GoldBOX:Boot"; // Default: "GoldBOX:Boot"
    /** GoldBOX:Boot app package name */
    public static final String GOLDBOX_BOOT_PACKAGE_NAME = GOLDBOX_PACKAGE_NAME + ".boot"; // Default: "com.goldbox.boot"
    /** GoldBOX:Boot GitHub repo name */
    public static final String GOLDBOX_BOOT_GITHUB_REPO_NAME = "goldbox-boot"; // Default: "goldbox-boot"
    /** GoldBOX:Boot GitHub repo url */
    public static final String GOLDBOX_BOOT_GITHUB_REPO_URL = GOLDBOX_GITHUB_ORGANIZATION_URL + "/" + GOLDBOX_BOOT_GITHUB_REPO_NAME; // Default: "https://github.com.goldbox/goldbox-boot"
    /** GoldBOX:Boot GitHub issues repo url */
    public static final String GOLDBOX_BOOT_GITHUB_ISSUES_REPO_URL = GOLDBOX_BOOT_GITHUB_REPO_URL + "/issues"; // Default: "https://github.com.goldbox/goldbox-boot/issues"
    /** GoldBOX:Boot F-Droid package url */
    public static final String GOLDBOX_BOOT_FDROID_PACKAGE_URL = FDROID_PACKAGES_BASE_URL + "/" + GOLDBOX_BOOT_PACKAGE_NAME; // Default: "https://f-droid.org/en/packages/com.goldbox.boot"


    /** GoldBOX:Float app name */
    public static final String GOLDBOX_FLOAT_APP_NAME = "GoldBOX:Float"; // Default: "GoldBOX:Float"
    /** GoldBOX:Float app package name */
    public static final String GOLDBOX_FLOAT_PACKAGE_NAME = GOLDBOX_PACKAGE_NAME + ".window"; // Default: "com.goldbox.window"
    /** GoldBOX:Float GitHub repo name */
    public static final String GOLDBOX_FLOAT_GITHUB_REPO_NAME = "goldbox-float"; // Default: "goldbox-float"
    /** GoldBOX:Float GitHub repo url */
    public static final String GOLDBOX_FLOAT_GITHUB_REPO_URL = GOLDBOX_GITHUB_ORGANIZATION_URL + "/" + GOLDBOX_FLOAT_GITHUB_REPO_NAME; // Default: "https://github.com.goldbox/goldbox-float"
    /** GoldBOX:Float GitHub issues repo url */
    public static final String GOLDBOX_FLOAT_GITHUB_ISSUES_REPO_URL = GOLDBOX_FLOAT_GITHUB_REPO_URL + "/issues"; // Default: "https://github.com.goldbox/goldbox-float/issues"
    /** GoldBOX:Float F-Droid package url */
    public static final String GOLDBOX_FLOAT_FDROID_PACKAGE_URL = FDROID_PACKAGES_BASE_URL + "/" + GOLDBOX_FLOAT_PACKAGE_NAME; // Default: "https://f-droid.org/en/packages/com.goldbox.window"


    /** GoldBOX:Styling app name */
    public static final String GOLDBOX_STYLING_APP_NAME = "GoldBOX:Styling"; // Default: "GoldBOX:Styling"
    /** GoldBOX:Styling app package name */
    public static final String GOLDBOX_STYLING_PACKAGE_NAME = GOLDBOX_PACKAGE_NAME + ".styling"; // Default: "com.goldbox.styling"
    /** GoldBOX:Styling GitHub repo name */
    public static final String GOLDBOX_STYLING_GITHUB_REPO_NAME = "goldbox-styling"; // Default: "goldbox-styling"
    /** GoldBOX:Styling GitHub repo url */
    public static final String GOLDBOX_STYLING_GITHUB_REPO_URL = GOLDBOX_GITHUB_ORGANIZATION_URL + "/" + GOLDBOX_STYLING_GITHUB_REPO_NAME; // Default: "https://github.com.goldbox/goldbox-styling"
    /** GoldBOX:Styling GitHub issues repo url */
    public static final String GOLDBOX_STYLING_GITHUB_ISSUES_REPO_URL = GOLDBOX_STYLING_GITHUB_REPO_URL + "/issues"; // Default: "https://github.com.goldbox/goldbox-styling/issues"
    /** GoldBOX:Styling F-Droid package url */
    public static final String GOLDBOX_STYLING_FDROID_PACKAGE_URL = FDROID_PACKAGES_BASE_URL + "/" + GOLDBOX_STYLING_PACKAGE_NAME; // Default: "https://f-droid.org/en/packages/com.goldbox.styling"


    /** GoldBOX:Tasker app name */
    public static final String GOLDBOX_TASKER_APP_NAME = "GoldBOX:Tasker"; // Default: "GoldBOX:Tasker"
    /** GoldBOX:Tasker app package name */
    public static final String GOLDBOX_TASKER_PACKAGE_NAME = GOLDBOX_PACKAGE_NAME + ".tasker"; // Default: "com.goldbox.tasker"
    /** GoldBOX:Tasker GitHub repo name */
    public static final String GOLDBOX_TASKER_GITHUB_REPO_NAME = "goldbox-tasker"; // Default: "goldbox-tasker"
    /** GoldBOX:Tasker GitHub repo url */
    public static final String GOLDBOX_TASKER_GITHUB_REPO_URL = GOLDBOX_GITHUB_ORGANIZATION_URL + "/" + GOLDBOX_TASKER_GITHUB_REPO_NAME; // Default: "https://github.com.goldbox/goldbox-tasker"
    /** GoldBOX:Tasker GitHub issues repo url */
    public static final String GOLDBOX_TASKER_GITHUB_ISSUES_REPO_URL = GOLDBOX_TASKER_GITHUB_REPO_URL + "/issues"; // Default: "https://github.com.goldbox/goldbox-tasker/issues"
    /** GoldBOX:Tasker F-Droid package url */
    public static final String GOLDBOX_TASKER_FDROID_PACKAGE_URL = FDROID_PACKAGES_BASE_URL + "/" + GOLDBOX_TASKER_PACKAGE_NAME; // Default: "https://f-droid.org/en/packages/com.goldbox.tasker"


    /** GoldBOX:Widget app name */
    public static final String GOLDBOX_WIDGET_APP_NAME = "GoldBOX:Widget"; // Default: "GoldBOX:Widget"
    /** GoldBOX:Widget app package name */
    public static final String GOLDBOX_WIDGET_PACKAGE_NAME = GOLDBOX_PACKAGE_NAME + ".widget"; // Default: "com.goldbox.widget"
    /** GoldBOX:Widget GitHub repo name */
    public static final String GOLDBOX_WIDGET_GITHUB_REPO_NAME = "goldbox-widget"; // Default: "goldbox-widget"
    /** GoldBOX:Widget GitHub repo url */
    public static final String GOLDBOX_WIDGET_GITHUB_REPO_URL = GOLDBOX_GITHUB_ORGANIZATION_URL + "/" + GOLDBOX_WIDGET_GITHUB_REPO_NAME; // Default: "https://github.com.goldbox/goldbox-widget"
    /** GoldBOX:Widget GitHub issues repo url */
    public static final String GOLDBOX_WIDGET_GITHUB_ISSUES_REPO_URL = GOLDBOX_WIDGET_GITHUB_REPO_URL + "/issues"; // Default: "https://github.com.goldbox/goldbox-widget/issues"
    /** GoldBOX:Widget F-Droid package url */
    public static final String GOLDBOX_WIDGET_FDROID_PACKAGE_URL = FDROID_PACKAGES_BASE_URL + "/" + GOLDBOX_WIDGET_PACKAGE_NAME; // Default: "https://f-droid.org/en/packages/com.goldbox.widget"





    /*
     * GoldBOX plugin apps lists.
     */

    public static final List<String> GOLDBOX_PLUGIN_APP_NAMES_LIST = Arrays.asList(
        GOLDBOX_API_APP_NAME,
        GOLDBOX_BOOT_APP_NAME,
        GOLDBOX_FLOAT_APP_NAME,
        GOLDBOX_STYLING_APP_NAME,
        GOLDBOX_TASKER_APP_NAME,
        GOLDBOX_WIDGET_APP_NAME);

    public static final List<String> GOLDBOX_PLUGIN_APP_PACKAGE_NAMES_LIST = Arrays.asList(
        GOLDBOX_API_PACKAGE_NAME,
        GOLDBOX_BOOT_PACKAGE_NAME,
        GOLDBOX_FLOAT_PACKAGE_NAME,
        GOLDBOX_STYLING_PACKAGE_NAME,
        GOLDBOX_TASKER_PACKAGE_NAME,
        GOLDBOX_WIDGET_PACKAGE_NAME);





    /*
     * GoldBOX APK releases.
     */

    /** F-Droid APK release */
    public static final String APK_RELEASE_FDROID = "F-Droid"; // Default: "F-Droid"

    /** F-Droid APK release signing certificate SHA-256 digest */
    public static final String APK_RELEASE_FDROID_SIGNING_CERTIFICATE_SHA256_DIGEST = "228FB2CFE90831C1499EC3CCAF61E96E8E1CE70766B9474672CE427334D41C42"; // Default: "228FB2CFE90831C1499EC3CCAF61E96E8E1CE70766B9474672CE427334D41C42"

    /** GitHub APK release */
    public static final String APK_RELEASE_GITHUB = "Github"; // Default: "Github"

    /** GitHub APK release signing certificate SHA-256 digest */
    public static final String APK_RELEASE_GITHUB_SIGNING_CERTIFICATE_SHA256_DIGEST = "B6DA01480EEFD5FBF2CD3771B8D1021EC791304BDD6C4BF41D3FAABAD48EE5E1"; // Default: "B6DA01480EEFD5FBF2CD3771B8D1021EC791304BDD6C4BF41D3FAABAD48EE5E1"

    /** Google Play Store APK release */
    public static final String APK_RELEASE_GOOGLE_PLAYSTORE = "Google Play Store"; // Default: "Google Play Store"

    /** Google Play Store APK release signing certificate SHA-256 digest */
    public static final String APK_RELEASE_GOOGLE_PLAYSTORE_SIGNING_CERTIFICATE_SHA256_DIGEST = "738F0A30A04D3C8A1BE304AF18D0779BCF3EA88FB60808F657A3521861C2EBF9"; // Default: "738F0A30A04D3C8A1BE304AF18D0779BCF3EA88FB60808F657A3521861C2EBF9"

    /** GoldBOX Devs APK release */
    public static final String APK_RELEASE_GOLDBOX_DEVS = "GoldBOX Devs"; // Default: "GoldBOX Devs"

    /** GoldBOX Devs APK release signing certificate SHA-256 digest */
    public static final String APK_RELEASE_GOLDBOX_DEVS_SIGNING_CERTIFICATE_SHA256_DIGEST = "F7A038EB551F1BE8FDF388686B784ABAB4552A5D82DF423E3D8F1B5CBE1C69AE"; // Default: "F7A038EB551F1BE8FDF388686B784ABAB4552A5D82DF423E3D8F1B5CBE1C69AE"





    /*
     * GoldBOX packages urls.
     */

    /** GoldBOX Packages GitHub repo name */
    public static final String GOLDBOX_PACKAGES_GITHUB_REPO_NAME = "goldbox-packages"; // Default: "goldbox-packages"
    /** GoldBOX Packages GitHub repo url */
    public static final String GOLDBOX_PACKAGES_GITHUB_REPO_URL = GOLDBOX_GITHUB_ORGANIZATION_URL + "/" + GOLDBOX_PACKAGES_GITHUB_REPO_NAME; // Default: "https://github.com.goldbox/goldbox-packages"
    /** GoldBOX Packages GitHub issues repo url */
    public static final String GOLDBOX_PACKAGES_GITHUB_ISSUES_REPO_URL = GOLDBOX_PACKAGES_GITHUB_REPO_URL + "/issues"; // Default: "https://github.com.goldbox/goldbox-packages/issues"


    /** GoldBOX API apt package name */
    public static final String GOLDBOX_API_APT_PACKAGE_NAME = "goldbox-api"; // Default: "goldbox-api"
    /** GoldBOX API apt GitHub repo name */
    public static final String GOLDBOX_API_APT_GITHUB_REPO_NAME = "goldbox-api-package"; // Default: "goldbox-api-package"
    /** GoldBOX API apt GitHub repo url */
    public static final String GOLDBOX_API_APT_GITHUB_REPO_URL = GOLDBOX_GITHUB_ORGANIZATION_URL + "/" + GOLDBOX_API_APT_GITHUB_REPO_NAME; // Default: "https://github.com.goldbox/goldbox-api-package"
    /** GoldBOX API apt GitHub issues repo url */
    public static final String GOLDBOX_API_APT_GITHUB_ISSUES_REPO_URL = GOLDBOX_API_APT_GITHUB_REPO_URL + "/issues"; // Default: "https://github.com.goldbox/goldbox-api-package/issues"





    /*
     * GoldBOX miscellaneous urls.
     */

    /** GoldBOX Site */
    public static final String GOLDBOX_SITE = GOLDBOX_APP_NAME + " Site"; // Default: "GoldBOX Site"

    /** GoldBOX Site url */
    public static final String GOLDBOX_SITE_URL = "https://goldbox.dev"; // Default: "https://goldbox.dev"

    /** GoldBOX Wiki */
    public static final String GOLDBOX_WIKI = GOLDBOX_APP_NAME + " Wiki"; // Default: "GoldBOX Wiki"

    /** GoldBOX Wiki url */
    public static final String GOLDBOX_WIKI_URL = "https://wiki.goldbox.com"; // Default: "https://wiki.goldbox.com"

    /** GoldBOX GitHub wiki repo url */
    public static final String GOLDBOX_GITHUB_WIKI_REPO_URL = GOLDBOX_GITHUB_REPO_URL + "/wiki"; // Default: "https://github.com.goldbox/goldbox-app/wiki"

    /** GoldBOX Packages wiki repo url */
    public static final String GOLDBOX_PACKAGES_GITHUB_WIKI_REPO_URL = GOLDBOX_PACKAGES_GITHUB_REPO_URL + "/wiki"; // Default: "https://github.com.goldbox/goldbox-packages/wiki"


    /** GoldBOX support email url */
    public static final String GOLDBOX_SUPPORT_EMAIL_URL = "support@goldbox.dev"; // Default: "support@goldbox.dev"

    /** GoldBOX support email mailto url */
    public static final String GOLDBOX_SUPPORT_EMAIL_MAILTO_URL = "mailto:" + GOLDBOX_SUPPORT_EMAIL_URL; // Default: "mailto:support@goldbox.dev"


    /** GoldBOX Reddit subreddit */
    public static final String GOLDBOX_REDDIT_SUBREDDIT = "r/goldbox"; // Default: "r/goldbox"

    /** GoldBOX Reddit subreddit url */
    public static final String GOLDBOX_REDDIT_SUBREDDIT_URL = "https://www.reddit.com/r/goldbox"; // Default: "https://www.reddit.com/r/goldbox"


    /** GoldBOX donate url */
    public static final String GOLDBOX_DONATE_URL = GOLDBOX_SITE_URL + "/donate"; // Default: "https://goldbox.dev/donate"





    /*
     * GoldBOX app core directory paths.
     */

    /** GoldBOX app internal private app data directory path */
    @SuppressLint("SdCardPath")
    public static final String GOLDBOX_INTERNAL_PRIVATE_APP_DATA_DIR_PATH = "/data/data/" + GOLDBOX_PACKAGE_NAME; // Default: "/data/data/com.goldbox"
    /** GoldBOX app internal private app data directory */
    public static final File GOLDBOX_INTERNAL_PRIVATE_APP_DATA_DIR = new File(GOLDBOX_INTERNAL_PRIVATE_APP_DATA_DIR_PATH);



    /** GoldBOX app Files directory path */
    public static final String GOLDBOX_FILES_DIR_PATH = GOLDBOX_INTERNAL_PRIVATE_APP_DATA_DIR_PATH + "/files"; // Default: "/data/data/com.goldbox/files"
    /** GoldBOX app Files directory */
    public static final File GOLDBOX_FILES_DIR = new File(GOLDBOX_FILES_DIR_PATH);



    /** GoldBOX app $PREFIX directory path */
    public static final String GOLDBOX_PREFIX_DIR_PATH = GOLDBOX_FILES_DIR_PATH + "/usr"; // Default: "/data/data/com.goldbox/files/usr"
    /** GoldBOX app $PREFIX directory */
    public static final File GOLDBOX_PREFIX_DIR = new File(GOLDBOX_PREFIX_DIR_PATH);


    /** GoldBOX app $PREFIX/bin directory path */
    public static final String GOLDBOX_BIN_PREFIX_DIR_PATH = GOLDBOX_PREFIX_DIR_PATH + "/bin"; // Default: "/data/data/com.goldbox/files/usr/bin"
    /** GoldBOX app $PREFIX/bin directory */
    public static final File GOLDBOX_BIN_PREFIX_DIR = new File(GOLDBOX_BIN_PREFIX_DIR_PATH);


    /** GoldBOX app $PREFIX/etc directory path */
    public static final String GOLDBOX_ETC_PREFIX_DIR_PATH = GOLDBOX_PREFIX_DIR_PATH + "/etc"; // Default: "/data/data/com.goldbox/files/usr/etc"
    /** GoldBOX app $PREFIX/etc directory */
    public static final File GOLDBOX_ETC_PREFIX_DIR = new File(GOLDBOX_ETC_PREFIX_DIR_PATH);


    /** GoldBOX app $PREFIX/include directory path */
    public static final String GOLDBOX_INCLUDE_PREFIX_DIR_PATH = GOLDBOX_PREFIX_DIR_PATH + "/include"; // Default: "/data/data/com.goldbox/files/usr/include"
    /** GoldBOX app $PREFIX/include directory */
    public static final File GOLDBOX_INCLUDE_PREFIX_DIR = new File(GOLDBOX_INCLUDE_PREFIX_DIR_PATH);


    /** GoldBOX app $PREFIX/lib directory path */
    public static final String GOLDBOX_LIB_PREFIX_DIR_PATH = GOLDBOX_PREFIX_DIR_PATH + "/lib"; // Default: "/data/data/com.goldbox/files/usr/lib"
    /** GoldBOX app $PREFIX/lib directory */
    public static final File GOLDBOX_LIB_PREFIX_DIR = new File(GOLDBOX_LIB_PREFIX_DIR_PATH);


    /** GoldBOX app $PREFIX/libexec directory path */
    public static final String GOLDBOX_LIBEXEC_PREFIX_DIR_PATH = GOLDBOX_PREFIX_DIR_PATH + "/libexec"; // Default: "/data/data/com.goldbox/files/usr/libexec"
    /** GoldBOX app $PREFIX/libexec directory */
    public static final File GOLDBOX_LIBEXEC_PREFIX_DIR = new File(GOLDBOX_LIBEXEC_PREFIX_DIR_PATH);


    /** GoldBOX app $PREFIX/share directory path */
    public static final String GOLDBOX_SHARE_PREFIX_DIR_PATH = GOLDBOX_PREFIX_DIR_PATH + "/share"; // Default: "/data/data/com.goldbox/files/usr/share"
    /** GoldBOX app $PREFIX/share directory */
    public static final File GOLDBOX_SHARE_PREFIX_DIR = new File(GOLDBOX_SHARE_PREFIX_DIR_PATH);


    /** GoldBOX app $PREFIX/tmp and $TMPDIR directory path */
    public static final String GOLDBOX_TMP_PREFIX_DIR_PATH = GOLDBOX_PREFIX_DIR_PATH + "/tmp"; // Default: "/data/data/com.goldbox/files/usr/tmp"
    /** GoldBOX app $PREFIX/tmp and $TMPDIR directory */
    public static final File GOLDBOX_TMP_PREFIX_DIR = new File(GOLDBOX_TMP_PREFIX_DIR_PATH);


    /** GoldBOX app $PREFIX/var directory path */
    public static final String GOLDBOX_VAR_PREFIX_DIR_PATH = GOLDBOX_PREFIX_DIR_PATH + "/var"; // Default: "/data/data/com.goldbox/files/usr/var"
    /** GoldBOX app $PREFIX/var directory */
    public static final File GOLDBOX_VAR_PREFIX_DIR = new File(GOLDBOX_VAR_PREFIX_DIR_PATH);



    /** GoldBOX app usr-staging directory path */
    public static final String GOLDBOX_STAGING_PREFIX_DIR_PATH = GOLDBOX_FILES_DIR_PATH + "/usr-staging"; // Default: "/data/data/com.goldbox/files/usr-staging"
    /** GoldBOX app usr-staging directory */
    public static final File GOLDBOX_STAGING_PREFIX_DIR = new File(GOLDBOX_STAGING_PREFIX_DIR_PATH);



    /** GoldBOX app $HOME directory path */
    public static final String GOLDBOX_HOME_DIR_PATH = GOLDBOX_FILES_DIR_PATH + "/home"; // Default: "/data/data/com.goldbox/files/home"
    /** GoldBOX app $HOME directory */
    public static final File GOLDBOX_HOME_DIR = new File(GOLDBOX_HOME_DIR_PATH);


    /** GoldBOX app config home directory path */
    public static final String GOLDBOX_CONFIG_HOME_DIR_PATH = GOLDBOX_HOME_DIR_PATH + "/.config/goldbox"; // Default: "/data/data/com.goldbox/files/home/.config/goldbox"
    /** GoldBOX app config home directory */
    public static final File GOLDBOX_CONFIG_HOME_DIR = new File(GOLDBOX_CONFIG_HOME_DIR_PATH);

    /** GoldBOX app config $PREFIX directory path */
    public static final String GOLDBOX_CONFIG_PREFIX_DIR_PATH = GOLDBOX_ETC_PREFIX_DIR_PATH + "/goldbox"; // Default: "/data/data/com.goldbox/files/usr/etc/goldbox"
    /** GoldBOX app config $PREFIX directory */
    public static final File GOLDBOX_CONFIG_PREFIX_DIR = new File(GOLDBOX_CONFIG_PREFIX_DIR_PATH);


    /** GoldBOX app data home directory path */
    public static final String GOLDBOX_DATA_HOME_DIR_PATH = GOLDBOX_HOME_DIR_PATH + "/.goldbox"; // Default: "/data/data/com.goldbox/files/home/.goldbox"
    /** GoldBOX app data home directory */
    public static final File GOLDBOX_DATA_HOME_DIR = new File(GOLDBOX_DATA_HOME_DIR_PATH);


    /** GoldBOX app storage home directory path */
    public static final String GOLDBOX_STORAGE_HOME_DIR_PATH = GOLDBOX_HOME_DIR_PATH + "/storage"; // Default: "/data/data/com.goldbox/files/home/storage"
    /** GoldBOX app storage home directory */
    public static final File GOLDBOX_STORAGE_HOME_DIR = new File(GOLDBOX_STORAGE_HOME_DIR_PATH);



    /** GoldBOX and plugin apps directory path */
    public static final String GOLDBOX_APPS_DIR_PATH = GOLDBOX_FILES_DIR_PATH + "/apps"; // Default: "/data/data/com.goldbox/files/apps"
    /** GoldBOX and plugin apps directory */
    public static final File GOLDBOX_APPS_DIR = new File(GOLDBOX_APPS_DIR_PATH);


    /** GoldBOX app $PREFIX directory path ignored sub file paths to consider it empty */
    public static final List<String> GOLDBOX_PREFIX_DIR_IGNORED_SUB_FILES_PATHS_TO_CONSIDER_AS_EMPTY = Arrays.asList(
        GoldBOXConstants.GOLDBOX_TMP_PREFIX_DIR_PATH, GoldBOXConstants.GOLDBOX_ENV_TEMP_FILE_PATH, GoldBOXConstants.GOLDBOX_ENV_FILE_PATH);



    /*
     * GoldBOX app and plugin preferences and properties file paths.
     */

    /** GoldBOX app default SharedPreferences file basename without extension */
    public static final String GOLDBOX_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION = GOLDBOX_PACKAGE_NAME + "_preferences"; // Default: "com.goldbox_preferences"

    /** GoldBOX:API app default SharedPreferences file basename without extension */
    public static final String GOLDBOX_API_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION = GOLDBOX_API_PACKAGE_NAME + "_preferences"; // Default: "com.goldbox.api_preferences"

    /** GoldBOX:Boot app default SharedPreferences file basename without extension */
    public static final String GOLDBOX_BOOT_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION = GOLDBOX_BOOT_PACKAGE_NAME + "_preferences"; // Default: "com.goldbox.boot_preferences"

    /** GoldBOX:Float app default SharedPreferences file basename without extension */
    public static final String GOLDBOX_FLOAT_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION = GOLDBOX_FLOAT_PACKAGE_NAME + "_preferences"; // Default: "com.goldbox.window_preferences"

    /** GoldBOX:Styling app default SharedPreferences file basename without extension */
    public static final String GOLDBOX_STYLING_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION = GOLDBOX_STYLING_PACKAGE_NAME + "_preferences"; // Default: "com.goldbox.styling_preferences"

    /** GoldBOX:Tasker app default SharedPreferences file basename without extension */
    public static final String GOLDBOX_TASKER_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION = GOLDBOX_TASKER_PACKAGE_NAME + "_preferences"; // Default: "com.goldbox.tasker_preferences"

    /** GoldBOX:Widget app default SharedPreferences file basename without extension */
    public static final String GOLDBOX_WIDGET_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION = GOLDBOX_WIDGET_PACKAGE_NAME + "_preferences"; // Default: "com.goldbox.widget_preferences"



    /** GoldBOX app properties primary file path */
    public static final String GOLDBOX_PROPERTIES_PRIMARY_FILE_PATH = GOLDBOX_DATA_HOME_DIR_PATH + "/goldbox.properties"; // Default: "/data/data/com.goldbox/files/home/.goldbox/goldbox.properties"
    /** GoldBOX app properties primary file */
    public static final File GOLDBOX_PROPERTIES_PRIMARY_FILE = new File(GOLDBOX_PROPERTIES_PRIMARY_FILE_PATH);

    /** GoldBOX app properties secondary file path */
    public static final String GOLDBOX_PROPERTIES_SECONDARY_FILE_PATH = GOLDBOX_CONFIG_HOME_DIR_PATH + "/goldbox.properties"; // Default: "/data/data/com.goldbox/files/home/.config/goldbox/goldbox.properties"
    /** GoldBOX app properties secondary file */
    public static final File GOLDBOX_PROPERTIES_SECONDARY_FILE = new File(GOLDBOX_PROPERTIES_SECONDARY_FILE_PATH);

    /** GoldBOX app properties file paths list. **DO NOT** allow these files to be modified by
     * {@link android.content.ContentProvider} exposed to external apps, since they may silently
     * modify the values for security properties like {@link #PROP_ALLOW_EXTERNAL_APPS} set by users
     * without their explicit consent. */
    public static final List<String> GOLDBOX_PROPERTIES_FILE_PATHS_LIST = Arrays.asList(
        GOLDBOX_PROPERTIES_PRIMARY_FILE_PATH,
        GOLDBOX_PROPERTIES_SECONDARY_FILE_PATH);



    /** GoldBOX:Float app properties primary file path */
    public static final String GOLDBOX_FLOAT_PROPERTIES_PRIMARY_FILE_PATH = GOLDBOX_DATA_HOME_DIR_PATH + "/goldbox.float.properties"; // Default: "/data/data/com.goldbox/files/home/.goldbox/goldbox.float.properties"
    /** GoldBOX:Float app properties primary file */
    public static final File GOLDBOX_FLOAT_PROPERTIES_PRIMARY_FILE = new File(GOLDBOX_FLOAT_PROPERTIES_PRIMARY_FILE_PATH);

    /** GoldBOX:Float app properties secondary file path */
    public static final String GOLDBOX_FLOAT_PROPERTIES_SECONDARY_FILE_PATH = GOLDBOX_CONFIG_HOME_DIR_PATH + "/goldbox.float.properties"; // Default: "/data/data/com.goldbox/files/home/.config/goldbox/goldbox.float.properties"
    /** GoldBOX:Float app properties secondary file */
    public static final File GOLDBOX_FLOAT_PROPERTIES_SECONDARY_FILE = new File(GOLDBOX_FLOAT_PROPERTIES_SECONDARY_FILE_PATH);

    /** GoldBOX:Float app properties file paths list. **DO NOT** allow these files to be modified by
     * {@link android.content.ContentProvider} exposed to external apps, since they may silently
     * modify the values for security properties like {@link #PROP_ALLOW_EXTERNAL_APPS} set by users
     * without their explicit consent. */
    public static final List<String> GOLDBOX_FLOAT_PROPERTIES_FILE_PATHS_LIST = Arrays.asList(
        GOLDBOX_FLOAT_PROPERTIES_PRIMARY_FILE_PATH,
        GOLDBOX_FLOAT_PROPERTIES_SECONDARY_FILE_PATH);



    /** GoldBOX app and GoldBOX:Styling colors.properties file path */
    public static final String GOLDBOX_COLOR_PROPERTIES_FILE_PATH = GOLDBOX_DATA_HOME_DIR_PATH + "/colors.properties"; // Default: "/data/data/com.goldbox/files/home/.goldbox/colors.properties"
    /** GoldBOX app and GoldBOX:Styling colors.properties file */
    public static final File GOLDBOX_COLOR_PROPERTIES_FILE = new File(GOLDBOX_COLOR_PROPERTIES_FILE_PATH);

    /** GoldBOX app and GoldBOX:Styling font.ttf file path */
    public static final String GOLDBOX_FONT_FILE_PATH = GOLDBOX_DATA_HOME_DIR_PATH + "/font.ttf"; // Default: "/data/data/com.goldbox/files/home/.goldbox/font.ttf"
    /** GoldBOX app and GoldBOX:Styling font.ttf file */
    public static final File GOLDBOX_FONT_FILE = new File(GOLDBOX_FONT_FILE_PATH);


    /** GoldBOX app and plugins crash log file path */
    public static final String GOLDBOX_CRASH_LOG_FILE_PATH = GOLDBOX_HOME_DIR_PATH + "/crash_log.md"; // Default: "/data/data/com.goldbox/files/home/crash_log.md"

    /** GoldBOX app and plugins crash log backup file path */
    public static final String GOLDBOX_CRASH_LOG_BACKUP_FILE_PATH = GOLDBOX_HOME_DIR_PATH + "/crash_log_backup.md"; // Default: "/data/data/com.goldbox/files/home/crash_log_backup.md"


    /** GoldBOX app environment file path */
    public static final String GOLDBOX_ENV_FILE_PATH = GOLDBOX_CONFIG_PREFIX_DIR_PATH + "/goldbox.env"; // Default: "/data/data/com.goldbox/files/usr/etc/goldbox/goldbox.env"

    /** GoldBOX app environment temp file path */
    public static final String GOLDBOX_ENV_TEMP_FILE_PATH = GOLDBOX_CONFIG_PREFIX_DIR_PATH + "/goldbox.env.tmp"; // Default: "/data/data/com.goldbox/files/usr/etc/goldbox/goldbox.env.tmp"




    /*
     * GoldBOX app plugin specific paths.
     */

    /** GoldBOX app directory path to store scripts to be run at boot by GoldBOX:Boot */
    public static final String GOLDBOX_BOOT_SCRIPTS_DIR_PATH = GOLDBOX_DATA_HOME_DIR_PATH + "/boot"; // Default: "/data/data/com.goldbox/files/home/.goldbox/boot"
    /** GoldBOX app directory to store scripts to be run at boot by GoldBOX:Boot */
    public static final File GOLDBOX_BOOT_SCRIPTS_DIR = new File(GOLDBOX_BOOT_SCRIPTS_DIR_PATH);


    /** GoldBOX app directory path to store foreground scripts that can be run by the goldbox launcher
     * widget provided by GoldBOX:Widget */
    public static final String GOLDBOX_SHORTCUT_SCRIPTS_DIR_PATH = GOLDBOX_HOME_DIR_PATH + "/.shortcuts"; // Default: "/data/data/com.goldbox/files/home/.shortcuts"
    /** GoldBOX app directory to store foreground scripts that can be run by the goldbox launcher widget provided by GoldBOX:Widget */
    public static final File GOLDBOX_SHORTCUT_SCRIPTS_DIR = new File(GOLDBOX_SHORTCUT_SCRIPTS_DIR_PATH);


    /** GoldBOX app directory basename that stores background scripts that can be run by the goldbox
     * launcher widget provided by GoldBOX:Widget */
    public static final String GOLDBOX_SHORTCUT_TASKS_SCRIPTS_DIR_BASENAME =  "tasks"; // Default: "tasks"
    /** GoldBOX app directory path to store background scripts that can be run by the goldbox launcher
     * widget provided by GoldBOX:Widget */
    public static final String GOLDBOX_SHORTCUT_TASKS_SCRIPTS_DIR_PATH = GOLDBOX_SHORTCUT_SCRIPTS_DIR_PATH + "/" + GOLDBOX_SHORTCUT_TASKS_SCRIPTS_DIR_BASENAME; // Default: "/data/data/com.goldbox/files/home/.shortcuts/tasks"
    /** GoldBOX app directory to store background scripts that can be run by the goldbox launcher widget provided by GoldBOX:Widget */
    public static final File GOLDBOX_SHORTCUT_TASKS_SCRIPTS_DIR = new File(GOLDBOX_SHORTCUT_TASKS_SCRIPTS_DIR_PATH);


    /** GoldBOX app directory basename that stores icons for the foreground and background scripts
     * that can be run by the goldbox launcher widget provided by GoldBOX:Widget */
    public static final String GOLDBOX_SHORTCUT_SCRIPT_ICONS_DIR_BASENAME =  "icons"; // Default: "icons"
    /** GoldBOX app directory path to store icons for the foreground and background scripts that can
     * be run by the goldbox launcher widget provided by GoldBOX:Widget */
    public static final String GOLDBOX_SHORTCUT_SCRIPT_ICONS_DIR_PATH = GOLDBOX_SHORTCUT_SCRIPTS_DIR_PATH + "/" + GOLDBOX_SHORTCUT_SCRIPT_ICONS_DIR_BASENAME; // Default: "/data/data/com.goldbox/files/home/.shortcuts/icons"
    /** GoldBOX app directory to store icons for the foreground and background scripts that can be
     * run by the goldbox launcher widget provided by GoldBOX:Widget */
    public static final File GOLDBOX_SHORTCUT_SCRIPT_ICONS_DIR = new File(GOLDBOX_SHORTCUT_SCRIPT_ICONS_DIR_PATH);


    /** GoldBOX app directory path to store scripts to be run by 3rd party twofortyfouram locale plugin
     * host apps like Tasker app via the GoldBOX:Tasker plugin client */
    public static final String GOLDBOX_TASKER_SCRIPTS_DIR_PATH = GOLDBOX_DATA_HOME_DIR_PATH + "/tasker"; // Default: "/data/data/com.goldbox/files/home/.goldbox/tasker"
    /** GoldBOX app directory to store scripts to be run by 3rd party twofortyfouram locale plugin host apps like Tasker app via the GoldBOX:Tasker plugin client */
    public static final File GOLDBOX_TASKER_SCRIPTS_DIR = new File(GOLDBOX_TASKER_SCRIPTS_DIR_PATH);





    /*
     * GoldBOX app and plugins notification variables.
     */

    /** GoldBOX app notification channel id used by {@link GOLDBOX_APP.GOLDBOX_SERVICE} */
    public static final String GOLDBOX_APP_NOTIFICATION_CHANNEL_ID = "goldbox_notification_channel";
    /** GoldBOX app notification channel name used by {@link GOLDBOX_APP.GOLDBOX_SERVICE} */
    public static final String GOLDBOX_APP_NOTIFICATION_CHANNEL_NAME = GoldBOXConstants.GOLDBOX_APP_NAME + " App";
    /** GoldBOX app unique notification id used by {@link GOLDBOX_APP.GOLDBOX_SERVICE} */
    public static final int GOLDBOX_APP_NOTIFICATION_ID = 1337;

    /** GoldBOX app notification channel id used by {@link GOLDBOX_APP.RUN_COMMAND_SERVICE} */
    public static final String GOLDBOX_RUN_COMMAND_NOTIFICATION_CHANNEL_ID = "goldbox_run_command_notification_channel";
    /** GoldBOX app notification channel name used by {@link GOLDBOX_APP.RUN_COMMAND_SERVICE} */
    public static final String GOLDBOX_RUN_COMMAND_NOTIFICATION_CHANNEL_NAME = GoldBOXConstants.GOLDBOX_APP_NAME + " RunCommandService";
    /** GoldBOX app unique notification id used by {@link GOLDBOX_APP.RUN_COMMAND_SERVICE} */
    public static final int GOLDBOX_RUN_COMMAND_NOTIFICATION_ID = 1338;

    /** GoldBOX app notification channel id used for plugin command errors */
    public static final String GOLDBOX_PLUGIN_COMMAND_ERRORS_NOTIFICATION_CHANNEL_ID = "goldbox_plugin_command_errors_notification_channel";
    /** GoldBOX app notification channel name used for plugin command errors */
    public static final String GOLDBOX_PLUGIN_COMMAND_ERRORS_NOTIFICATION_CHANNEL_NAME = GoldBOXConstants.GOLDBOX_APP_NAME + " Plugin Commands Errors";

    /** GoldBOX app notification channel id used for crash reports */
    public static final String GOLDBOX_CRASH_REPORTS_NOTIFICATION_CHANNEL_ID = "goldbox_crash_reports_notification_channel";
    /** GoldBOX app notification channel name used for crash reports */
    public static final String GOLDBOX_CRASH_REPORTS_NOTIFICATION_CHANNEL_NAME = GoldBOXConstants.GOLDBOX_APP_NAME + " Crash Reports";


    /** GoldBOX app notification channel id used by {@link GOLDBOX_FLOAT_APP.GOLDBOX_FLOAT_SERVICE} */
    public static final String GOLDBOX_FLOAT_APP_NOTIFICATION_CHANNEL_ID = "goldbox_float_notification_channel";
    /** GoldBOX app notification channel name used by {@link GOLDBOX_FLOAT_APP.GOLDBOX_FLOAT_SERVICE} */
    public static final String GOLDBOX_FLOAT_APP_NOTIFICATION_CHANNEL_NAME = GoldBOXConstants.GOLDBOX_FLOAT_APP_NAME + " App";
    /** GoldBOX app unique notification id used by {@link GOLDBOX_APP.GOLDBOX_SERVICE} */
    public static final int GOLDBOX_FLOAT_APP_NOTIFICATION_ID = 1339;





    /*
     * GoldBOX app and plugins miscellaneous variables.
     */

    /** Android OS permission declared by GoldBOX app in AndroidManifest.xml which can be requested by
     * 3rd party apps to run various commands in GoldBOX app context */
    public static final String PERMISSION_RUN_COMMAND = GOLDBOX_PACKAGE_NAME + ".permission.RUN_COMMAND"; // Default: "com.goldbox.permission.RUN_COMMAND"

    /** GoldBOX property defined in goldbox.properties file as a secondary check to PERMISSION_RUN_COMMAND
     * to allow 3rd party apps to run various commands in GoldBOX app context */
    public static final String PROP_ALLOW_EXTERNAL_APPS = "allow-external-apps"; // Default: "allow-external-apps"
    /** Default value for {@link #PROP_ALLOW_EXTERNAL_APPS} */
    public static final String PROP_DEFAULT_VALUE_ALLOW_EXTERNAL_APPS = "false"; // Default: "false"

    /** The broadcast action sent when GoldBOX App opens */
    public static final String BROADCAST_GOLDBOX_OPENED = GOLDBOX_PACKAGE_NAME + ".app.OPENED";

    /** The Uri authority for GoldBOX app file shares */
    public static final String GOLDBOX_FILE_SHARE_URI_AUTHORITY = GOLDBOX_PACKAGE_NAME + ".files"; // Default: "com.goldbox.files"

    /** The normal comma character (U+002C, &comma;, &#44;, comma) */
    public static final String COMMA_NORMAL = ","; // Default: ","

    /** The alternate comma character (U+201A, &sbquo;, &#8218;, single low-9 quotation mark) that
     * may be used instead of {@link #COMMA_NORMAL} */
    public static final String COMMA_ALTERNATIVE = "‚"; // Default: "‚"

    /** Environment variable prefix root for the GoldBOX app. */
    public static final String GOLDBOX_ENV_PREFIX_ROOT = "GOLDBOX";






    /**
     * GoldBOX app constants.
     */
    public static final class GOLDBOX_APP {

        /** GoldBOX apps directory path */
        public static final String APPS_DIR_PATH = GOLDBOX_APPS_DIR_PATH + "/" + GOLDBOX_PACKAGE_NAME; // Default: "/data/data/com.goldbox/files/apps/com.goldbox"

        /** goldbox-am socket file path */
        public static final String GOLDBOX_AM_SOCKET_FILE_PATH = APPS_DIR_PATH + "/goldbox-am/am.sock"; // Default: "/data/data/com.goldbox/files/apps/com.goldbox/goldbox-am/am.sock"


        /** GoldBOX app BuildConfig class name */
        public static final String BUILD_CONFIG_CLASS_NAME = GOLDBOX_PACKAGE_NAME + ".BuildConfig"; // Default: "com.goldbox.BuildConfig"

        /** GoldBOX app FileShareReceiverActivity class name */
        public static final String FILE_SHARE_RECEIVER_ACTIVITY_CLASS_NAME = GOLDBOX_PACKAGE_NAME + ".app.api.file.FileShareReceiverActivity"; // Default: "com.goldbox.app.api.file.FileShareReceiverActivity"

        /** GoldBOX app FileViewReceiverActivity class name */
        public static final String FILE_VIEW_RECEIVER_ACTIVITY_CLASS_NAME = GOLDBOX_PACKAGE_NAME + ".app.api.file.FileViewReceiverActivity"; // Default: "com.goldbox.app.api.file.FileViewReceiverActivity"


        /** GoldBOX app core activity name. */
        public static final String GOLDBOX_ACTIVITY_NAME = GOLDBOX_PACKAGE_NAME + ".app.GoldBOXActivity"; // Default: "com.goldbox.app.GoldBOXActivity"

        /**
         * GoldBOX app core activity.
         */
        public static final class GOLDBOX_ACTIVITY {

            /** Intent extra for if goldbox failsafe session needs to be started and is used by {@link GOLDBOX_ACTIVITY} and {@link GOLDBOX_SERVICE#ACTION_STOP_SERVICE} */
            public static final String EXTRA_FAILSAFE_SESSION = GoldBOXConstants.GOLDBOX_PACKAGE_NAME + ".app.failsafe_session"; // Default: "com.goldbox.app.failsafe_session"


            /** Intent action to make goldbox app notify user that a crash happened. */
            public static final String ACTION_NOTIFY_APP_CRASH = GoldBOXConstants.GOLDBOX_PACKAGE_NAME + ".app.notify_app_crash"; // Default: "com.goldbox.app.notify_app_crash"


            /** Intent action to make goldbox reload its goldbox session styling */
            public static final String ACTION_RELOAD_STYLE = GoldBOXConstants.GOLDBOX_PACKAGE_NAME + ".app.reload_style"; // Default: "com.goldbox.app.reload_style"
            /** Intent {@code String} extra for what to reload for the GOLDBOX_ACTIVITY.ACTION_RELOAD_STYLE intent. This has been deperecated. */
            @Deprecated
            public static final String EXTRA_RELOAD_STYLE = GoldBOXConstants.GOLDBOX_PACKAGE_NAME + ".app.reload_style"; // Default: "com.goldbox.app.reload_style"

            /**  Intent {@code boolean} extra for whether to recreate activity for the GOLDBOX_ACTIVITY.ACTION_RELOAD_STYLE intent. */
            public static final String EXTRA_RECREATE_ACTIVITY = GOLDBOX_APP.GOLDBOX_ACTIVITY_NAME + ".EXTRA_RECREATE_ACTIVITY"; // Default: "com.goldbox.app.GoldBOXActivity.EXTRA_RECREATE_ACTIVITY"


            /** Intent action to make goldbox request storage permissions */
            public static final String ACTION_REQUEST_PERMISSIONS = GoldBOXConstants.GOLDBOX_PACKAGE_NAME + ".app.request_storage_permissions"; // Default: "com.goldbox.app.request_storage_permissions"
        }





        /** GoldBOX app settings activity name. */
        public static final String GOLDBOX_SETTINGS_ACTIVITY_NAME = GOLDBOX_PACKAGE_NAME + ".app.activities.SettingsActivity"; // Default: "com.goldbox.app.activities.SettingsActivity"





        /** GoldBOX app core service name. */
        public static final String GOLDBOX_SERVICE_NAME = GOLDBOX_PACKAGE_NAME + ".app.GoldBOXService"; // Default: "com.goldbox.app.GoldBOXService"

        /**
         * GoldBOX app core service.
         */
        public static final class GOLDBOX_SERVICE {

            /** Intent action to stop GOLDBOX_SERVICE */
            public static final String ACTION_STOP_SERVICE = GOLDBOX_PACKAGE_NAME + ".service_stop"; // Default: "com.goldbox.service_stop"


            /** Intent action to make GOLDBOX_SERVICE acquire a wakelock */
            public static final String ACTION_WAKE_LOCK = GOLDBOX_PACKAGE_NAME + ".service_wake_lock"; // Default: "com.goldbox.service_wake_lock"


            /** Intent action to make GOLDBOX_SERVICE release wakelock */
            public static final String ACTION_WAKE_UNLOCK = GOLDBOX_PACKAGE_NAME + ".service_wake_unlock"; // Default: "com.goldbox.service_wake_unlock"


            /** Intent action to execute command with GOLDBOX_SERVICE */
            public static final String ACTION_SERVICE_EXECUTE = GOLDBOX_PACKAGE_NAME + ".service_execute"; // Default: "com.goldbox.service_execute"

            /** Uri scheme for paths sent via intent to GOLDBOX_SERVICE */
            public static final String URI_SCHEME_SERVICE_EXECUTE = GOLDBOX_PACKAGE_NAME + ".file"; // Default: "com.goldbox.file"
            /** Intent {@code String[]} extra for arguments to the executable of the command for the GOLDBOX_SERVICE.ACTION_SERVICE_EXECUTE intent */
            public static final String EXTRA_ARGUMENTS = GOLDBOX_PACKAGE_NAME + ".execute.arguments"; // Default: "com.goldbox.execute.arguments"
            /** Intent {@code String} extra for stdin of the command for the GOLDBOX_SERVICE.ACTION_SERVICE_EXECUTE intent */
            public static final String EXTRA_STDIN = GOLDBOX_PACKAGE_NAME + ".execute.stdin"; // Default: "com.goldbox.execute.stdin"
            /** Intent {@code String} extra for command current working directory for the GOLDBOX_SERVICE.ACTION_SERVICE_EXECUTE intent */
            public static final String EXTRA_WORKDIR = GOLDBOX_PACKAGE_NAME + ".execute.cwd"; // Default: "com.goldbox.execute.cwd"
            /** Intent {@code boolean} extra for whether to run command in background {@link Runner#APP_SHELL} or foreground {@link Runner#TERMINAL_SESSION} for the GOLDBOX_SERVICE.ACTION_SERVICE_EXECUTE intent */
            @Deprecated
            public static final String EXTRA_BACKGROUND = GOLDBOX_PACKAGE_NAME + ".execute.background"; // Default: "com.goldbox.execute.background"
            /** Intent {@code String} extra for command the {@link Runner} for the GOLDBOX_SERVICE.ACTION_SERVICE_EXECUTE intent */
            public static final String EXTRA_RUNNER = GOLDBOX_PACKAGE_NAME + ".execute.runner"; // Default: "com.goldbox.execute.runner"
            /** Intent {@code String} extra for custom log level for background commands defined by {@link com.goldbox.shared.logger.Logger} for the GOLDBOX_SERVICE.ACTION_SERVICE_EXECUTE intent */
            public static final String EXTRA_BACKGROUND_CUSTOM_LOG_LEVEL = GOLDBOX_PACKAGE_NAME + ".execute.background_custom_log_level"; // Default: "com.goldbox.execute.background_custom_log_level"
            /** Intent {@code String} extra for session action for {@link Runner#TERMINAL_SESSION} commands for the GOLDBOX_SERVICE.ACTION_SERVICE_EXECUTE intent */
            public static final String EXTRA_SESSION_ACTION = GOLDBOX_PACKAGE_NAME + ".execute.session_action"; // Default: "com.goldbox.execute.session_action"
            /** Intent {@code String} extra for shell name for commands for the GOLDBOX_SERVICE.ACTION_SERVICE_EXECUTE intent */
            public static final String EXTRA_SHELL_NAME = GOLDBOX_PACKAGE_NAME + ".execute.shell_name"; // Default: "com.goldbox.execute.shell_name"
            /** Intent {@code String} extra for the {@link ExecutionCommand.ShellCreateMode}  for the GOLDBOX_SERVICE.ACTION_SERVICE_EXECUTE intent. */
            public static final String EXTRA_SHELL_CREATE_MODE = GOLDBOX_PACKAGE_NAME + ".execute.shell_create_mode"; // Default: "com.goldbox.execute.shell_create_mode"
            /** Intent {@code String} extra for label of the command for the GOLDBOX_SERVICE.ACTION_SERVICE_EXECUTE intent */
            public static final String EXTRA_COMMAND_LABEL = GOLDBOX_PACKAGE_NAME + ".execute.command_label"; // Default: "com.goldbox.execute.command_label"
            /** Intent markdown {@code String} extra for description of the command for the GOLDBOX_SERVICE.ACTION_SERVICE_EXECUTE intent */
            public static final String EXTRA_COMMAND_DESCRIPTION = GOLDBOX_PACKAGE_NAME + ".execute.command_description"; // Default: "com.goldbox.execute.command_description"
            /** Intent markdown {@code String} extra for help of the command for the GOLDBOX_SERVICE.ACTION_SERVICE_EXECUTE intent */
            public static final String EXTRA_COMMAND_HELP = GOLDBOX_PACKAGE_NAME + ".execute.command_help"; // Default: "com.goldbox.execute.command_help"
            /** Intent markdown {@code String} extra for help of the plugin API for the GOLDBOX_SERVICE.ACTION_SERVICE_EXECUTE intent (Internal Use Only) */
            public static final String EXTRA_PLUGIN_API_HELP = GOLDBOX_PACKAGE_NAME + ".execute.plugin_api_help"; // Default: "com.goldbox.execute.plugin_help"
            /** Intent {@code Parcelable} extra for the pending intent that should be sent with the
             * result of the execution command to the execute command caller for the GOLDBOX_SERVICE.ACTION_SERVICE_EXECUTE intent */
            public static final String EXTRA_PENDING_INTENT = "pendingIntent"; // Default: "pendingIntent"
            /** Intent {@code String} extra for the directory path in which to write the result of the
             * execution command for the execute command caller for the GOLDBOX_SERVICE.ACTION_SERVICE_EXECUTE intent */
            public static final String EXTRA_RESULT_DIRECTORY = GOLDBOX_PACKAGE_NAME + ".execute.result_directory"; // Default: "com.goldbox.execute.result_directory"
            /** Intent {@code boolean} extra for whether the result should be written to a single file
             * or multiple files (err, errmsg, stdout, stderr, exit_code) in
             * {@link #EXTRA_RESULT_DIRECTORY} for the GOLDBOX_SERVICE.ACTION_SERVICE_EXECUTE intent */
            public static final String EXTRA_RESULT_SINGLE_FILE = GOLDBOX_PACKAGE_NAME + ".execute.result_single_file"; // Default: "com.goldbox.execute.result_single_file"
            /** Intent {@code String} extra for the basename of the result file that should be created
             * in {@link #EXTRA_RESULT_DIRECTORY} if {@link #EXTRA_RESULT_SINGLE_FILE} is {@code true}
             * for the GOLDBOX_SERVICE.ACTION_SERVICE_EXECUTE intent */
            public static final String EXTRA_RESULT_FILE_BASENAME = GOLDBOX_PACKAGE_NAME + ".execute.result_file_basename"; // Default: "com.goldbox.execute.result_file_basename"
            /** Intent {@code String} extra for the output {@link Formatter} format of the
             * {@link #EXTRA_RESULT_FILE_BASENAME} result file for the GOLDBOX_SERVICE.ACTION_SERVICE_EXECUTE intent */
            public static final String EXTRA_RESULT_FILE_OUTPUT_FORMAT = GOLDBOX_PACKAGE_NAME + ".execute.result_file_output_format"; // Default: "com.goldbox.execute.result_file_output_format"
            /** Intent {@code String} extra for the error {@link Formatter} format of the
             * {@link #EXTRA_RESULT_FILE_BASENAME} result file for the GOLDBOX_SERVICE.ACTION_SERVICE_EXECUTE intent */
            public static final String EXTRA_RESULT_FILE_ERROR_FORMAT = GOLDBOX_PACKAGE_NAME + ".execute.result_file_error_format"; // Default: "com.goldbox.execute.result_file_error_format"
            /** Intent {@code String} extra for the optional suffix of the result files that should
             * be created in {@link #EXTRA_RESULT_DIRECTORY} if {@link #EXTRA_RESULT_SINGLE_FILE} is
             * {@code false} for the GOLDBOX_SERVICE.ACTION_SERVICE_EXECUTE intent */
            public static final String EXTRA_RESULT_FILES_SUFFIX = GOLDBOX_PACKAGE_NAME + ".execute.result_files_suffix"; // Default: "com.goldbox.execute.result_files_suffix"



            /**
             * The value for {@link #EXTRA_SESSION_ACTION} extra that will set the new session as
             * the current session and will start {@link GOLDBOX_ACTIVITY} if its not running to bring
             * the new session to foreground.
             */
            public static final int VALUE_EXTRA_SESSION_ACTION_SWITCH_TO_NEW_SESSION_AND_OPEN_ACTIVITY = 0;

            /**
             * The value for {@link #EXTRA_SESSION_ACTION} extra that will keep any existing session
             * as the current session and will start {@link GOLDBOX_ACTIVITY} if its not running to
             * bring the existing session to foreground. The new session will be added to the left
             * sidebar in the sessions list.
             */
            public static final int VALUE_EXTRA_SESSION_ACTION_KEEP_CURRENT_SESSION_AND_OPEN_ACTIVITY = 1;

            /**
             * The value for {@link #EXTRA_SESSION_ACTION} extra that will set the new session as
             * the current session but will not start {@link GOLDBOX_ACTIVITY} if its not running
             * and session(s) will be seen in GoldBOX notification and can be clicked to bring new
             * session to foreground. If the {@link GOLDBOX_ACTIVITY} is already running, then this
             * will behave like {@link #VALUE_EXTRA_SESSION_ACTION_KEEP_CURRENT_SESSION_AND_OPEN_ACTIVITY}.
             */
            public static final int VALUE_EXTRA_SESSION_ACTION_SWITCH_TO_NEW_SESSION_AND_DONT_OPEN_ACTIVITY = 2;

            /**
             * The value for {@link #EXTRA_SESSION_ACTION} extra that will keep any existing session
             * as the current session but will not start {@link GOLDBOX_ACTIVITY} if its not running
             * and session(s) will be seen in GoldBOX notification and can be clicked to bring
             * existing session to foreground. If the {@link GOLDBOX_ACTIVITY} is already running,
             * then this will behave like {@link #VALUE_EXTRA_SESSION_ACTION_KEEP_CURRENT_SESSION_AND_OPEN_ACTIVITY}.
             */
            public static final int VALUE_EXTRA_SESSION_ACTION_KEEP_CURRENT_SESSION_AND_DONT_OPEN_ACTIVITY = 3;

            /** The minimum allowed value for {@link #EXTRA_SESSION_ACTION}. */
            public static final int MIN_VALUE_EXTRA_SESSION_ACTION = VALUE_EXTRA_SESSION_ACTION_SWITCH_TO_NEW_SESSION_AND_OPEN_ACTIVITY;

            /** The maximum allowed value for {@link #EXTRA_SESSION_ACTION}. */
            public static final int MAX_VALUE_EXTRA_SESSION_ACTION = VALUE_EXTRA_SESSION_ACTION_KEEP_CURRENT_SESSION_AND_DONT_OPEN_ACTIVITY;


            /** Intent {@code Bundle} extra to store result of execute command that is sent back for the
             * GOLDBOX_SERVICE.ACTION_SERVICE_EXECUTE intent if the {@link #EXTRA_PENDING_INTENT} is not
             * {@code null} */
            public static final String EXTRA_PLUGIN_RESULT_BUNDLE = "result"; // Default: "result"
            /** Intent {@code String} extra for stdout value of execute command of the {@link #EXTRA_PLUGIN_RESULT_BUNDLE} */
            public static final String EXTRA_PLUGIN_RESULT_BUNDLE_STDOUT = "stdout"; // Default: "stdout"
            /** Intent {@code String} extra for original length of stdout value of execute command of the {@link #EXTRA_PLUGIN_RESULT_BUNDLE} */
            public static final String EXTRA_PLUGIN_RESULT_BUNDLE_STDOUT_ORIGINAL_LENGTH = "stdout_original_length"; // Default: "stdout_original_length"
            /** Intent {@code String} extra for stderr value of execute command of the {@link #EXTRA_PLUGIN_RESULT_BUNDLE} */
            public static final String EXTRA_PLUGIN_RESULT_BUNDLE_STDERR = "stderr"; // Default: "stderr"
            /** Intent {@code String} extra for original length of stderr value of execute command of the {@link #EXTRA_PLUGIN_RESULT_BUNDLE} */
            public static final String EXTRA_PLUGIN_RESULT_BUNDLE_STDERR_ORIGINAL_LENGTH = "stderr_original_length"; // Default: "stderr_original_length"
            /** Intent {@code int} extra for exit code value of execute command of the {@link #EXTRA_PLUGIN_RESULT_BUNDLE} */
            public static final String EXTRA_PLUGIN_RESULT_BUNDLE_EXIT_CODE = "exitCode"; // Default: "exitCode"
            /** Intent {@code int} extra for err value of execute command of the {@link #EXTRA_PLUGIN_RESULT_BUNDLE} */
            public static final String EXTRA_PLUGIN_RESULT_BUNDLE_ERR = "err"; // Default: "err"
            /** Intent {@code String} extra for errmsg value of execute command of the {@link #EXTRA_PLUGIN_RESULT_BUNDLE} */
            public static final String EXTRA_PLUGIN_RESULT_BUNDLE_ERRMSG = "errmsg"; // Default: "errmsg"

        }





        /** GoldBOX app run command service name. */
        public static final String RUN_COMMAND_SERVICE_NAME = GOLDBOX_PACKAGE_NAME + ".app.RunCommandService"; // GoldBOX app service to receive commands from 3rd party apps "com.goldbox.app.RunCommandService"

        /**
         * GoldBOX app run command service to receive commands sent by 3rd party apps.
         */
        public static final class RUN_COMMAND_SERVICE {

            /** GoldBOX RUN_COMMAND Intent help url */
            public static final String RUN_COMMAND_API_HELP_URL = GOLDBOX_GITHUB_WIKI_REPO_URL + "/RUN_COMMAND-Intent"; // Default: "https://github.com.goldbox/goldbox-app/wiki/RUN_COMMAND-Intent"


            /** Intent action to execute command with RUN_COMMAND_SERVICE */
            public static final String ACTION_RUN_COMMAND = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND"; // Default: "com.goldbox.RUN_COMMAND"

            /** Intent {@code String} extra for absolute path of command for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
            public static final String EXTRA_COMMAND_PATH = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND_PATH"; // Default: "com.goldbox.RUN_COMMAND_PATH"
            /** Intent {@code String[]} extra for arguments to the executable of the command for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
            public static final String EXTRA_ARGUMENTS = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND_ARGUMENTS"; // Default: "com.goldbox.RUN_COMMAND_ARGUMENTS"
            /** Intent {@code boolean} extra for whether to replace comma alternative characters in arguments with comma characters for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
            public static final String EXTRA_REPLACE_COMMA_ALTERNATIVE_CHARS_IN_ARGUMENTS = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND_REPLACE_COMMA_ALTERNATIVE_CHARS_IN_ARGUMENTS"; // Default: "com.goldbox.RUN_COMMAND_REPLACE_COMMA_ALTERNATIVE_CHARS_IN_ARGUMENTS"
            /** Intent {@code String} extra for the comma alternative characters in arguments that should be replaced instead of the default {@link #COMMA_ALTERNATIVE} for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
            public static final String EXTRA_COMMA_ALTERNATIVE_CHARS_IN_ARGUMENTS = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND_COMMA_ALTERNATIVE_CHARS_IN_ARGUMENTS"; // Default: "com.goldbox.RUN_COMMAND_COMMA_ALTERNATIVE_CHARS_IN_ARGUMENTS"

            /** Intent {@code String} extra for stdin of the command for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
            public static final String EXTRA_STDIN = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND_STDIN"; // Default: "com.goldbox.RUN_COMMAND_STDIN"
            /** Intent {@code String} extra for current working directory of command for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
            public static final String EXTRA_WORKDIR = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND_WORKDIR"; // Default: "com.goldbox.RUN_COMMAND_WORKDIR"
            /** Intent {@code boolean} extra for whether to run command in background {@link Runner#APP_SHELL} or foreground {@link Runner#TERMINAL_SESSION} for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
            @Deprecated
            public static final String EXTRA_BACKGROUND = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND_BACKGROUND"; // Default: "com.goldbox.RUN_COMMAND_BACKGROUND"
            /** Intent {@code String} extra for command the {@link Runner} for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
            public static final String EXTRA_RUNNER = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND_RUNNER"; // Default: "com.goldbox.RUN_COMMAND_RUNNER"
            /** Intent {@code String} extra for custom log level for background commands defined by {@link com.goldbox.shared.logger.Logger} for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
            public static final String EXTRA_BACKGROUND_CUSTOM_LOG_LEVEL = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND_BACKGROUND_CUSTOM_LOG_LEVEL"; // Default: "com.goldbox.RUN_COMMAND_BACKGROUND_CUSTOM_LOG_LEVEL"
            /** Intent {@code String} extra for session action of {@link Runner#TERMINAL_SESSION} commands for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
            public static final String EXTRA_SESSION_ACTION = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND_SESSION_ACTION"; // Default: "com.goldbox.RUN_COMMAND_SESSION_ACTION"
            /** Intent {@code String} extra for shell name of commands for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
            public static final String EXTRA_SHELL_NAME = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND_SHELL_NAME"; // Default: "com.goldbox.RUN_COMMAND_SHELL_NAME"
            /** Intent {@code String} extra for the {@link ExecutionCommand.ShellCreateMode}  for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent. */
            public static final String EXTRA_SHELL_CREATE_MODE = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND_SHELL_CREATE_MODE"; // Default: "com.goldbox.RUN_COMMAND_SHELL_CREATE_MODE"
            /** Intent {@code String} extra for label of the command for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
            public static final String EXTRA_COMMAND_LABEL = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND_COMMAND_LABEL"; // Default: "com.goldbox.RUN_COMMAND_COMMAND_LABEL"
            /** Intent markdown {@code String} extra for description of the command for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
            public static final String EXTRA_COMMAND_DESCRIPTION = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND_COMMAND_DESCRIPTION"; // Default: "com.goldbox.RUN_COMMAND_COMMAND_DESCRIPTION"
            /** Intent markdown {@code String} extra for help of the command for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
            public static final String EXTRA_COMMAND_HELP = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND_COMMAND_HELP"; // Default: "com.goldbox.RUN_COMMAND_COMMAND_HELP"
            /** Intent {@code Parcelable} extra for the pending intent that should be sent with the result of the execution command to the execute command caller for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
            public static final String EXTRA_PENDING_INTENT = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND_PENDING_INTENT"; // Default: "com.goldbox.RUN_COMMAND_PENDING_INTENT"
            /** Intent {@code String} extra for the directory path in which to write the result of
             * the execution command for the execute command caller for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
            public static final String EXTRA_RESULT_DIRECTORY = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND_RESULT_DIRECTORY"; // Default: "com.goldbox.RUN_COMMAND_RESULT_DIRECTORY"
            /** Intent {@code boolean} extra for whether the result should be written to a single file
             * or multiple files (err, errmsg, stdout, stderr, exit_code) in
             * {@link #EXTRA_RESULT_DIRECTORY} for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
            public static final String EXTRA_RESULT_SINGLE_FILE = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND_RESULT_SINGLE_FILE"; // Default: "com.goldbox.RUN_COMMAND_RESULT_SINGLE_FILE"
            /** Intent {@code String} extra for the basename of the result file that should be created
             * in {@link #EXTRA_RESULT_DIRECTORY} if {@link #EXTRA_RESULT_SINGLE_FILE} is {@code true}
             * for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
            public static final String EXTRA_RESULT_FILE_BASENAME = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND_RESULT_FILE_BASENAME"; // Default: "com.goldbox.RUN_COMMAND_RESULT_FILE_BASENAME"
            /** Intent {@code String} extra for the output {@link Formatter} format of the
             * {@link #EXTRA_RESULT_FILE_BASENAME} result file for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
            public static final String EXTRA_RESULT_FILE_OUTPUT_FORMAT = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND_RESULT_FILE_OUTPUT_FORMAT"; // Default: "com.goldbox.RUN_COMMAND_RESULT_FILE_OUTPUT_FORMAT"
            /** Intent {@code String} extra for the error {@link Formatter} format of the
             * {@link #EXTRA_RESULT_FILE_BASENAME} result file for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
            public static final String EXTRA_RESULT_FILE_ERROR_FORMAT = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND_RESULT_FILE_ERROR_FORMAT"; // Default: "com.goldbox.RUN_COMMAND_RESULT_FILE_ERROR_FORMAT"
            /** Intent {@code String} extra for the optional suffix of the result files that should be
             * created in {@link #EXTRA_RESULT_DIRECTORY} if {@link #EXTRA_RESULT_SINGLE_FILE} is
             * {@code false} for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
            public static final String EXTRA_RESULT_FILES_SUFFIX = GOLDBOX_PACKAGE_NAME + ".RUN_COMMAND_RESULT_FILES_SUFFIX"; // Default: "com.goldbox.RUN_COMMAND_RESULT_FILES_SUFFIX"

        }
    }


    /**
     * GoldBOX:API app constants.
     */
    public static final class GOLDBOX_API_APP {

        /** GoldBOX:API app main activity name. */
        public static final String GOLDBOX_API_MAIN_ACTIVITY_NAME = GOLDBOX_API_PACKAGE_NAME + ".activities.GoldBOXAPIMainActivity"; // Default: "com.goldbox.api.activities.GoldBOXAPIMainActivity"

        /** GoldBOX:API app launcher activity name. This is an `activity-alias` for {@link #GOLDBOX_API_MAIN_ACTIVITY_NAME} used for launchers with {@link Intent#CATEGORY_LAUNCHER}. */
        public static final String GOLDBOX_API_LAUNCHER_ACTIVITY_NAME = GOLDBOX_API_PACKAGE_NAME + ".activities.GoldBOXAPILauncherActivity"; // Default: "com.goldbox.api.activities.GoldBOXAPILauncherActivity"

    }





    /**
     * GoldBOX:Boot app constants.
     */
    public static final class GOLDBOX_BOOT_APP {

        /** GoldBOX:Boot app main activity name. */
        public static final String GOLDBOX_BOOT_MAIN_ACTIVITY_NAME = GOLDBOX_BOOT_PACKAGE_NAME + ".activities.GoldBOXBootMainActivity"; // Default: "com.goldbox.boot.activities.GoldBOXBootMainActivity"

        /** GoldBOX:Boot app launcher activity name. This is an `activity-alias` for {@link #GOLDBOX_BOOT_MAIN_ACTIVITY_NAME} used for launchers with {@link Intent#CATEGORY_LAUNCHER}. */
        public static final String GOLDBOX_BOOT_LAUNCHER_ACTIVITY_NAME = GOLDBOX_BOOT_PACKAGE_NAME + ".activities.GoldBOXBootLauncherActivity"; // Default: "com.goldbox.boot.activities.GoldBOXBootLauncherActivity"

    }





    /**
     * GoldBOX:Float app constants.
     */
    public static final class GOLDBOX_FLOAT_APP {

        /** GoldBOX:Float app core activity name. */
        public static final String GOLDBOX_FLOAT_ACTIVITY_NAME = GOLDBOX_FLOAT_PACKAGE_NAME + ".GoldBOXFloatActivity"; // Default: "com.goldbox.window.GoldBOXFloatActivity"

        /** GoldBOX:Float app core service name. */
        public static final String GOLDBOX_FLOAT_SERVICE_NAME = GOLDBOX_FLOAT_PACKAGE_NAME + ".GoldBOXFloatService"; // Default: "com.goldbox.window.GoldBOXFloatService"

        /**
         * GoldBOX:Float app core service.
         */
        public static final class GOLDBOX_FLOAT_SERVICE {

            /** Intent action to stop GOLDBOX_FLOAT_SERVICE. */
            public static final String ACTION_STOP_SERVICE = GOLDBOX_FLOAT_PACKAGE_NAME + ".ACTION_STOP_SERVICE"; // Default: "com.goldbox.float.ACTION_STOP_SERVICE"

            /** Intent action to show float window. */
            public static final String ACTION_SHOW = GOLDBOX_FLOAT_PACKAGE_NAME + ".ACTION_SHOW"; // Default: "com.goldbox.float.ACTION_SHOW"

            /** Intent action to hide float window. */
            public static final String ACTION_HIDE = GOLDBOX_FLOAT_PACKAGE_NAME + ".ACTION_HIDE"; // Default: "com.goldbox.float.ACTION_HIDE"

        }

    }





    /**
     * GoldBOX:Styling app constants.
     */
    public static final class GOLDBOX_STYLING_APP {

        /** GoldBOX:Styling app core activity name. */
        public static final String GOLDBOX_STYLING_ACTIVITY_NAME = GOLDBOX_STYLING_PACKAGE_NAME + ".GoldBOXStyleActivity"; // Default: "com.goldbox.styling.GoldBOXStyleActivity"


        /** GoldBOX:Styling app main activity name. */
        public static final String GOLDBOX_STYLING_MAIN_ACTIVITY_NAME = GOLDBOX_STYLING_PACKAGE_NAME + ".activities.GoldBOXStylingMainActivity"; // Default: "com.goldbox.styling.activities.GoldBOXStylingMainActivity"

        /** GoldBOX:Styling app launcher activity name. This is an `activity-alias` for {@link #GOLDBOX_STYLING_MAIN_ACTIVITY_NAME} used for launchers with {@link Intent#CATEGORY_LAUNCHER}. */
        public static final String GOLDBOX_STYLING_LAUNCHER_ACTIVITY_NAME = GOLDBOX_STYLING_PACKAGE_NAME + ".activities.GoldBOXStylingLauncherActivity"; // Default: "com.goldbox.styling.activities.GoldBOXStylingLauncherActivity"

    }





    /**
     * GoldBOX:Tasker app constants.
     */
    public static final class GOLDBOX_TASKER_APP {

        /** GoldBOX:Tasker app main activity name. */
        public static final String GOLDBOX_TASKER_MAIN_ACTIVITY_NAME = GOLDBOX_TASKER_PACKAGE_NAME + ".activities.GoldBOXTaskerMainActivity"; // Default: "com.goldbox.tasker.activities.GoldBOXTaskerMainActivity"

        /** GoldBOX:Tasker app launcher activity name. This is an `activity-alias` for {@link #GOLDBOX_TASKER_MAIN_ACTIVITY_NAME} used for launchers with {@link Intent#CATEGORY_LAUNCHER}. */
        public static final String GOLDBOX_TASKER_LAUNCHER_ACTIVITY_NAME = GOLDBOX_TASKER_PACKAGE_NAME + ".activities.GoldBOXTaskerLauncherActivity"; // Default: "com.goldbox.tasker.activities.GoldBOXTaskerLauncherActivity"

    }





    /**
     * GoldBOX:Widget app constants.
     */
    public static final class GOLDBOX_WIDGET_APP {

        /** GoldBOX:Widget app main activity name. */
        public static final String GOLDBOX_WIDGET_MAIN_ACTIVITY_NAME = GOLDBOX_WIDGET_PACKAGE_NAME + ".activities.GoldBOXWidgetMainActivity"; // Default: "com.goldbox.widget.activities.GoldBOXWidgetMainActivity"

        /** GoldBOX:Widget app launcher activity name. This is an `activity-alias` for {@link #GOLDBOX_WIDGET_MAIN_ACTIVITY_NAME} used for launchers with {@link Intent#CATEGORY_LAUNCHER}. */
        public static final String GOLDBOX_WIDGET_LAUNCHER_ACTIVITY_NAME = GOLDBOX_WIDGET_PACKAGE_NAME + ".activities.GoldBOXWidgetLauncherActivity"; // Default: "com.goldbox.widget.activities.GoldBOXWidgetLauncherActivity"


        /**  Intent {@code String} extra for the token of the GoldBOX:Widget app shortcuts. */
        public static final String EXTRA_TOKEN_NAME = GOLDBOX_PACKAGE_NAME + ".shortcut.token"; // Default: "com.goldbox.shortcut.token"


        /**
         * GoldBOX:Widget app {@link android.appwidget.AppWidgetProvider} class.
         */
        public static final class GOLDBOX_WIDGET_PROVIDER {

            /** Intent action for if an item is clicked in the widget. */
            public static final String ACTION_WIDGET_ITEM_CLICKED = GOLDBOX_WIDGET_PACKAGE_NAME + ".ACTION_WIDGET_ITEM_CLICKED"; // Default: "com.goldbox.widget.ACTION_WIDGET_ITEM_CLICKED"


            /** Intent action to refresh files in the widget. */
            public static final String ACTION_REFRESH_WIDGET = GOLDBOX_WIDGET_PACKAGE_NAME + ".ACTION_REFRESH_WIDGET"; // Default: "com.goldbox.widget.ACTION_REFRESH_WIDGET"


            /**  Intent {@code String} extra for the file clicked for the GOLDBOX_WIDGET_PROVIDER.ACTION_WIDGET_ITEM_CLICKED intent. */
            public static final String EXTRA_FILE_CLICKED = GOLDBOX_WIDGET_PACKAGE_NAME + ".EXTRA_FILE_CLICKED"; // Default: "com.goldbox.widget.EXTRA_FILE_CLICKED"

        }

    }

}
