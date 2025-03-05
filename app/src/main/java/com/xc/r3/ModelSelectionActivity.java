package com.xc.r3;

import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.xc.r3.R;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class ModelSelectionActivity extends AppCompatActivity {

    private DataStoreManager dataStoreManager;
    private List<String> modelList;
    private List<String> modelDisplayList;
    private Map<String, String> modelDisplayMap;
    private Spinner modelSpinner;
    private String deviceName;
    private String previousSelection;
    private boolean isSpinnerInitialized = false;
    private TextView linkTextView;
    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the device brand is AIR3
        if (!android.os.Build.BRAND.equals("AIR3")) {
            showBrandErrorDialog();
            return;
        }

        setContentView(R.layout.activity_model_selection);

        dataStoreManager = new DataStoreManager(this);
        modelSpinner = findViewById(R.id.model_spinner);
        Button buttonConfirm = findViewById(R.id.button_confirm);
        linkTextView = findViewById(R.id.linkTextView);

        deviceName = getDeviceName();
        DataStoreManager.ModelListResult modelListResult = dataStoreManager.initModelLists(deviceName);
        modelList = modelListResult.modelList;
        modelDisplayList = modelListResult.modelDisplayList;
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

        // Initialize previousSelection
        previousSelection = (defaultModelIndex != -1)
                ? modelList.get(defaultModelIndex)
                : deviceName;

        setupSpinnerListener();

        buttonConfirm.setOnClickListener(v -> {
            disposables.add(dataStoreManager.saveManualModelSelected(true)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Void>() {
                        @Override
                        public void accept(Void unused) throws Throwable {
                            // Do nothing on success
                        }
                    }, throwable -> Timber.e(throwable, "Error saving manual model selection")));

            String selectedDisplayString = modelSpinner.getSelectedItem().toString();
            String selectedModel = modelDisplayMap.get(selectedDisplayString);

            if (selectedModel == null) {
                saveSelectedModel(deviceName);
            } else {
                saveSelectedModel(selectedModel);
            }
            setResult(RESULT_OK);
            finish();
        });

        // Set the click listener for the linkTextView
        linkTextView.setOnClickListener(v -> {
            String url = "https://www.fly-air3.com/ufaqs/which-version-of-air3-is-it/";
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
    }
    private void setupSpinnerListener() {
        modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Timber.d("onItemSelected called");
                if (isSpinnerInitialized) {
                    String selectedDisplayString = parent.getItemAtPosition(position).toString();
                    String selectedModel = modelDisplayMap.get(selectedDisplayString);

                    if (!previousSelection.equals(selectedModel)) {
                        if (selectedModel == null) {
                            showDeviceNameConfirmationDialog();
                        } else {
                            saveSelectedModel(selectedModel);
                        }
                    }
                } else {
                    isSpinnerInitialized = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
    }

    private void showBrandErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(getString(R.string.brand_error_message))
                .setPositiveButton(getString(R.string.ok), (dialog, which) -> finishAffinity())
                .setCancelable(false)
                .show();
    }

    private void showDeviceNameConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.device_name_confirmation_title))
                .setMessage(getString(R.string.device_name_confirmation_message))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    // Save the device name as the selected model
                    saveSelectedModel(deviceName);
                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.no), (dialog, which) -> {
                    // Reset the selection to the previous one
                    int previousSelectionIndex = modelList.indexOf(previousSelection);
                    if (previousSelectionIndex != -1) {
                        modelSpinner.setSelection(previousSelectionIndex);
                    }
                    dialog.dismiss();
                })
                .show();
    }

    private void saveSelectedModel(String selectedModel) {
        disposables.add(
                dataStoreManager.saveSelectedModel(selectedModel)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Void>() {
                            @Override
                            public void accept(Void unused) throws Throwable {
                                previousSelection = selectedModel;
                                Timber.d("Model saved: %s", selectedModel);
                            }
                        }, throwable -> Timber.e(throwable, "Error saving selected model"))
        );
    }
    private String getDeviceName() {
        String deviceName = Settings.Global.getString(
                getContentResolver(),
                Settings.Global.DEVICE_NAME
        );
        return deviceName != null ? deviceName : getString(R.string.unknown_device);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }

}