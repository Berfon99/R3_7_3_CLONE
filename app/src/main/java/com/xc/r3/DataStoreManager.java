package com.xc.r3;

import android.content.Context;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.functions.Function;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.util.Log;
import com.xc.r3.R;
import timber.log.Timber;


public class DataStoreManager {
    private static final List<String> ALLOWED_MODELS = Arrays.asList(
            "AIR3-7.2",
            "AIR3-7.3",
            "AIR3-7.3+",
            "AIR3-7.35",
            "AIR3-7.35+"
    );

    private final Context context;
    private final RxDataStore<Preferences> dataStore;

    // Preferences Keys
    private static final Preferences.Key<String> SELECTED_MODEL =
            PreferencesKeys.stringKey("selected_model");
    private static final Preferences.Key<Boolean> MANUAL_MODEL_SELECTED =
            PreferencesKeys.booleanKey("manual_model_selected");

    public DataStoreManager(Context context) {
        this.context = context;
        this.dataStore = new RxPreferenceDataStoreBuilder(
                context, "app_preferences").build();
    }

    public ModelListResult initModelLists(String deviceName) {
        // Initialize the model list
        List<String> modelList = new ArrayList<>(ALLOWED_MODELS);

        // Check if the device name is already in the allowed models
        if (!modelList.contains(deviceName)) {
            modelList.add(deviceName);
        }

        // Initialize the display list and map
        List<String> modelDisplayList = new ArrayList<>();
        Map<String, String> modelDisplayMap = new HashMap<>();

        for (String model : ALLOWED_MODELS) {
            modelDisplayList.add(model);
            modelDisplayMap.put(model, model);
        }

        // Add the device name as the last item with the prefix
        String deviceNameDisplay = context.getString(R.string.device_name) + " " + deviceName;
        modelDisplayList.add(deviceNameDisplay);
        modelDisplayMap.put(deviceNameDisplay, null);

        return new ModelListResult(modelList, modelDisplayList, modelDisplayMap);
    }

    public boolean isDeviceModelSupported(String model) {
        return ALLOWED_MODELS.contains(model);
    }

    public Single<Void> saveSelectedModel(String model) {
        return dataStore.updateDataAsync(prefs -> {
            MutablePreferences mutablePreferences = prefs.toMutablePreferences();
            mutablePreferences.set(SELECTED_MODEL, model);
            return Single.just(mutablePreferences);
        }).map(preferences -> null);
    }

    public Observable<String> getSelectedModel() {
        return dataStore.data().map(prefs -> {
            String selectedModel = prefs.get(SELECTED_MODEL);
            return selectedModel == null ? "" : selectedModel;
        }).toObservable();
    }

    public Single<Void> saveManualModelSelected(boolean isManualModelSelected) {
        return dataStore.updateDataAsync(prefs -> {
                    MutablePreferences mutablePreferences = prefs.toMutablePreferences();
                    mutablePreferences.set(MANUAL_MODEL_SELECTED, isManualModelSelected);
                    return Single.just(mutablePreferences);
                }).map(preferences -> (Void) null) // Forcer Single<Void>
                .onErrorResumeNext(throwable -> {
                    Timber.e(throwable, "Error saving manual model selection");
                    return Single.just((Void) null); // Forcer Single<Void>
                });
    }
    public Observable<Boolean> getManualModelSelected() {
        return dataStore.data().map(prefs -> {
            Boolean manualModelSelected = prefs.get(MANUAL_MODEL_SELECTED);
            return manualModelSelected != null && manualModelSelected;
        }).onErrorReturnItem(false).toObservable();
    }

    // Helper class to return multiple values
    public static class ModelListResult {
        public final List<String> modelList;
        public final List<String> modelDisplayList;
        public final Map<String, String> modelDisplayMap;

        public ModelListResult(
                List<String> modelList,
                List<String> modelDisplayList,
                Map<String, String> modelDisplayMap
        ) {
            this.modelList = modelList;
            this.modelDisplayList = modelDisplayList;
            this.modelDisplayMap = modelDisplayMap;
        }
    }
}