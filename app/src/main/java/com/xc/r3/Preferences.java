package com.xc.r3;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Preferences {

    private static final String KEY_USER = "user";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_NOMS_FICHIERS = "liste fichiers";

    private final SharedPreferences preferences;

    public Preferences(Context context) {
        preferences = context
                .getSharedPreferences("preferences", Context.MODE_PRIVATE);
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

}