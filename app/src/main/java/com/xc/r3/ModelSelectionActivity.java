package com.xc.r3;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class ModelSelectionActivity extends AppCompatActivity {

    private DataStoreManager dataStoreManager;
    private Spinner modelSpinner;
    private TextView linkTextView;
    private String deviceName;
    private List<String> modelList;
    private Map<String, String> modelDisplayMap;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the device brand is AIR3
        if (!android.os.Build.BRAND.equals("AIR3")) {
            showBrandErrorDialog();
            return;
        }

        setContentView(R.layout.activity_model_selection);

        dataStoreManager = DataStoreManager.getInstance(this);
        modelSpinner = findViewById(R.id.model_spinner);
        Button buttonConfirm = findViewById(R.id.button_confirm);
        linkTextView = findViewById(R.id.linkTextView);

        deviceName = getDeviceName();
        DataStoreManager.ModelListResult modelListResult = dataStoreManager.initModelLists(deviceName);
        modelList = modelListResult.modelList;
        List<String> modelDisplayList = modelListResult.modelDisplayList;
        modelDisplayMap = modelListResult.modelDisplayMap;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                modelDisplayList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelSpinner.setAdapter(adapter);

        // Determine the default model and set the selection
        String defaultModel = android.os.Build.MODEL;
        int defaultModelIndex = modelList.contains(defaultModel)
                ? modelList.indexOf(defaultModel)
                : modelList.indexOf(deviceName);

        modelSpinner.setSelection(defaultModelIndex);

        // Set up the spinner listener (no need to save here)
        modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Timber.d("onItemSelected called");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        // Set up the OK button listener
        buttonConfirm.setOnClickListener(v -> {
            String selectedDisplayString = modelSpinner.getSelectedItem().toString();
            String selectedModel = modelDisplayMap.get(selectedDisplayString);
            if (selectedModel == null) {
                selectedModel = deviceName;
            }

            // Save the selected model and the manual selection flag
            String finalSelectedModel = selectedModel;
            disposables.add(dataStoreManager.saveSelectedModel(finalSelectedModel)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Void>() {
                        @Override
                        public void accept(Void unused) throws Throwable {
                            Timber.d("Model saved successfully: %s", finalSelectedModel);
                            setResult(RESULT_OK);
                            finish();
                        }
                    }, throwable -> Timber.e(throwable, "Error saving model selection")));
        });

        // Set the click listener for the linkTextView
        linkTextView.setOnClickListener(v -> {
            String url = "https://www.fly-air3.com/ufaqs/which-version-of-air3-is-it/";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }

    private String getDeviceName() {
        return android.os.Build.MODEL;
    }

    private void showBrandErrorDialog() {
        // Implement your error dialog here
    }

    private void saveSelectedModel(String model) {
        // Implement your save logic here
    }
}