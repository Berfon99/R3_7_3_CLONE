package com.xc.r3;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class User {
    private static final String KEY_USER = "user";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_DANGEROUS = "dangerous";
    private static final String KEY_XCTRACK_BOOT = "XCTrack boot";
    private static final String KEY_DELAY_XCTRACK_ON_BOOT = "delay XCTrack on boot";
    private static final String KEY_DOWNLOAD_FICHIER_OPENAIR = "download fichier OA";
    private static final String KEY_MODE = "mode";
    private static final String KEY_THEME = "theme";
    private static final String KEY_DOWNLOAD_FICHIER_OPENAIR_ON_BOOT = "download fichier OA on boot";
    private static User user;
    private static SharedPreferences preferences;

    private User(Context context) {
        preferences = context
                .getSharedPreferences("auth", Context.MODE_PRIVATE);
    }

    public static User getInstance(Context context) {
        if (user == null) user = new User(context);
        return user;
    }

    public void setUser(String user) {
        Editor editor = preferences.edit();
        editor.putString(KEY_USER, user);
        editor.apply();
    }

    public void setToken(String password) {
        Editor editor = preferences.edit();
        editor.putString(KEY_TOKEN, password);
        editor.apply();
    }

    public String getUser() {
        return preferences.getString(KEY_USER, null);
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public boolean getDangerous() {
        return preferences.getBoolean(KEY_DANGEROUS, false);
    }

    public boolean lancerXCTrackBoot() {
        return preferences.getBoolean(KEY_XCTRACK_BOOT, true);
    }

    public boolean delayXCTrackOnBoot() {
        return preferences.getBoolean(KEY_DELAY_XCTRACK_ON_BOOT, false);
    }

    public boolean downloadFichierOpenAirOnBoot() {
        return preferences.getBoolean(KEY_DOWNLOAD_FICHIER_OPENAIR_ON_BOOT, false);
    }
    public boolean downloadFichierOpenAir() {
        return preferences.getBoolean(KEY_DOWNLOAD_FICHIER_OPENAIR, false);
    }
    public void setPreferencesBoolean(boolean bootXCtrack, boolean delayXTrackOnBoot, boolean download,boolean download_on_boot, boolean danger) {
        Editor editor = preferences.edit();
        editor.putBoolean(KEY_XCTRACK_BOOT, bootXCtrack);
        if ( bootXCtrack ) {
            editor.putBoolean(KEY_DELAY_XCTRACK_ON_BOOT, delayXTrackOnBoot);
        }
        editor.putBoolean(KEY_DOWNLOAD_FICHIER_OPENAIR, download);
        editor.putBoolean(KEY_DOWNLOAD_FICHIER_OPENAIR_ON_BOOT, download_on_boot);
        editor.putBoolean(KEY_DANGEROUS, danger);
        editor.apply();
    }

    public void setPreferencesInterface(int indiceMode, int indiceTheme) {
        Editor editor = preferences.edit();
        editor.putInt(KEY_MODE, indiceMode);
        editor.putInt(KEY_THEME, indiceTheme);
        editor.apply();
    }

    public int chargerPreferenceMode() {
        return preferences.getInt(KEY_MODE, 0);
    }

    public int chargerPreferenceTheme() {
        return preferences.getInt(KEY_THEME, 0);
    }

}