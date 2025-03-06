package com.xc.r3;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PreferencesActivity extends AppCompatActivity {

    private Switch switchXCTrackBoot;
    private Switch switchDownload;
    private Switch switchDelayXCTrackOnBoot;
    private Switch switchDownloadOnBoot;
    private Switch switchDanger;
    private User user;

    private Spinner modelSpinner;
    private Button buttonConfirm;
    private String deviceName;
    private List<String> modelList;
    private DataStorageManager dataStorageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // Initialize DataStorageManager and User
        dataStorageManager = DataStorageManager.getInstance(this);
        user = User.getInstance(this);

        // Initialize Switches
        switchXCTrackBoot = findViewById(R.id.switch_xctrack);
        switchDelayXCTrackOnBoot = findViewById(R.id.switch_xctrack_delay);
        switchDownloadOnBoot = findViewById(R.id.switch_download_on_boot);
        switchDownload = findViewById(R.id.switch_download);
        switchDanger = findViewById(R.id.switch_danger);

        // Initialize Model Selection UI
        modelSpinner = findViewById(R.id.model_spinner);
        buttonConfirm = findViewById(R.id.button_confirm);

        // Setup Model Selection
        setupModelSelection();

        // Setup Switch Listeners
        switchXCTrackBoot.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitchDelayXCTrackOnBoot());
        switchDownload.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitchesDownload());

        // Initialize Switch States
        initSwitches();
    }

    private void setupModelSelection() {
        // Get the allowed models from DataStorageManager
        List<String> allowedModels = dataStorageManager.getAllowedModels();

        // Get the device name
        deviceName = getDeviceName();

        // Create the modelList
        modelList = new ArrayList<>();
        modelList.addAll(allowedModels);
        modelList.add("Device name: " + deviceName);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modelList);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        modelSpinner.setAdapter(adapter);

        // Set the default selection
        String selectedModel = dataStorageManager.getSelectedModel();
        if (!selectedModel.isEmpty()) {
            modelSpinner.setSelection(modelList.indexOf(selectedModel));
        } else {
            modelSpinner.setSelection(modelList.indexOf("Device name: " + deviceName));
        }

        // Set the click listener for the buttonConfirm
        buttonConfirm.setOnClickListener(v -> {
            // Create a new variable inside the lambda
            String selectedModelInLambda = modelSpinner.getSelectedItem().toString();
            if (selectedModelInLambda.equals("Device name: " + deviceName)) {
                showDeviceNameConfirmationDialog(deviceName);
            }
            else {
                dataStorageManager.saveSelectedModel(selectedModelInLambda);
            }
        });
    }
    private void initSwitches() {
        switchXCTrackBoot.setChecked(user.lancerXCTrackBoot());
        setSwitchDelayXCTrackOnBoot();
        switchDelayXCTrackOnBoot.setChecked(user.delayXCTrackOnBoot());
        switchDownload.setChecked(user.downloadFichierOpenAir());
        setSwitchesDownload();
    }

    private void setSwitchDelayXCTrackOnBoot() {
        switchDelayXCTrackOnBoot.setEnabled(switchXCTrackBoot.isChecked());
        switchDelayXCTrackOnBoot.setChecked(switchXCTrackBoot.isChecked() && user.delayXCTrackOnBoot());
    }

    private void setSwitchesDownload() {
        switchDownloadOnBoot.setEnabled(switchDownload.isChecked());
        switchDownloadOnBoot.setChecked(switchDownload.isChecked() && user.downloadFichierOpenAirOnBoot());
        switchDanger.setEnabled(switchDownload.isChecked());
        switchDanger.setChecked(switchDownload.isChecked() && user.getDangerous());
    }

    private void showDeviceNameConfirmationDialog(String deviceName) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.device_name_confirmation_title))
                .setMessage(getString(R.string.device_name_confirmation_message))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    dataStorageManager.saveSelectedModel(deviceName);
                })
                .setNegativeButton(getString(R.string.no), (dialog, which) -> {
                    // Reset the spinner to the default selection
                    if (dataStorageManager.getAllowedModels().contains(Build.MODEL)) {
                        modelSpinner.setSelection(modelList.indexOf(Build.MODEL));
                    } else {
                        modelSpinner.setSelection(modelList.indexOf("Device name: " + deviceName));
                    }
                })
                .setCancelable(false)
                .show();
    }

    private String getDeviceName() {
        String deviceName = Settings.Global.getString(getContentResolver(), Settings.Global.DEVICE_NAME);
        if (deviceName == null || deviceName.isEmpty()) {
            return getString(R.string.unknown_device);
        }
        return deviceName;
    }

    @Override
    public void onBackPressed() {
        sauver();
        setResult(RESULT_OK);
        super.onBackPressed();
    }
    private void sauver() {
        boolean bootXCtrack = switchXCTrackBoot.isChecked();
        boolean delayXCtrackOnBoot = switchDelayXCTrackOnBoot.isChecked();
        boolean download = switchDownload.isChecked();
        boolean downloadOnBoot = switchDownloadOnBoot.isChecked();
        boolean danger = switchDanger.isChecked();
        user.setPreferencesBoolean(bootXCtrack, delayXCtrackOnBoot, download, downloadOnBoot, danger);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}