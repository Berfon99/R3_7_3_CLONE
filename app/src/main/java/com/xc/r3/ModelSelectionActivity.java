package com.xc.r3;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ModelSelectionActivity extends AppCompatActivity {

    private Spinner modelSpinner;
    private TextView linkTextView;
    private Button buttonConfirm;
    private String deviceName;
    private List<String> modelList;
    private DataStorageManager dataStorageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the DataStorageManager instance
        dataStorageManager = DataStorageManager.getInstance(this);

        // Check if a model is already selected
        String selectedModel = dataStorageManager.getSelectedModel();
        if (!selectedModel.isEmpty()) {
            // A model is already selected, launch MainActivity directly
            startActivity(new Intent(ModelSelectionActivity.this, MainActivity.class));
            finish(); // Finish ModelSelectionActivity so it's not on the back stack
            return;
        }

        setContentView(R.layout.activity_model_selection);

        // Check if the device brand is AIR3
        if (!Build.BRAND.equals("AIR3")) {
            showBrandErrorDialog();
            return;
        }
        Toast.makeText(this, "AIR3 brand detected", Toast.LENGTH_SHORT).show();

        modelSpinner = findViewById(R.id.model_spinner);
        linkTextView = findViewById(R.id.linkTextView);
        buttonConfirm = findViewById(R.id.button_confirm);

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
        if (allowedModels.contains(Build.MODEL)) {
            modelSpinner.setSelection(modelList.indexOf(Build.MODEL));
        } else {
            modelSpinner.setSelection(modelList.indexOf("Device name: " + deviceName));
        }

        // Set the click listener for the linkTextView
        linkTextView.setOnClickListener(v -> {
            String url = "https://www.fly-air3.com/ufaqs/which-version-of-air3-is-it/";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        // Set the click listener for the buttonConfirm
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedModel = modelSpinner.getSelectedItem().toString();
                if (selectedModel.equals("Device name: " + deviceName)) {
                    showDeviceNameConfirmationDialog(deviceName);
                } else {
                    dataStorageManager.saveSelectedModel(selectedModel);
                    startActivity(new Intent(ModelSelectionActivity.this, MainActivity.class));
                }
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

    private void showDeviceNameConfirmationDialog(String deviceName) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.device_name_confirmation_title))
                .setMessage(getString(R.string.device_name_confirmation_message, deviceName))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    dataStorageManager.saveSelectedModel(deviceName);
                    startActivity(new Intent(ModelSelectionActivity.this, MainActivity.class));
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
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}