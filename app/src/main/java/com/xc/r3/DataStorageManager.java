package com.xc.r3;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.List;

public class DataStorageManager {

    private static final List<String> ALLOWED_MODELS = Arrays.asList(
            "AIR3-7.2",
            "AIR3-7.3",
            "AIR3-7.3+",
            "AIR3-7.35",
            "AIR3-7.35+"
    );

    private static DataStorageManager instance;
    private Context context;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "model_preferences";
    private static final String SELECTED_MODEL_KEY = "selected_model";

    private DataStorageManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static DataStorageManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataStorageManager(context);
        }
        return instance;
    }

    public List<String> getAllowedModels() {
        return ALLOWED_MODELS;
    }

    public String getSelectedModel() {
        return sharedPreferences.getString(SELECTED_MODEL_KEY, "");
    }

    public void saveSelectedModel(String model) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SELECTED_MODEL_KEY, model);
        editor.apply();
    }
}