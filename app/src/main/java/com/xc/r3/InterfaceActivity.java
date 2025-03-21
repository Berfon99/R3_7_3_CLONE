package com.xc.r3;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import java.io.InputStream;
import java.util.Objects;

import timber.log.Timber;

public class InterfaceActivity extends CommonActivity {

    private static final String IMAGE_PATH = "images/";
    private User user;
    private Spinner spinnerMode;
    private int[] iconesModes;
    private Configuration configuration;
    private DataStorageManager dataStorageManager;
    private ModelConfiguration modelConfiguration;
    private boolean finishActivity = false;
    private boolean messageDisplayed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("InterfaceActivity onCreate start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interface);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        this.user = User.getInstance(this);
        this.configuration = GestionFichiers.lireConfiguration(this);
        this.dataStorageManager = DataStorageManager.getInstance(this);
        String selectedModel = dataStorageManager.getSelectedModel();
        this.modelConfiguration = configuration.getModelConfiguration(selectedModel);

        // Set the ActionBar title with selected model and Android version
        setActionBarTitleWithSelectedModel();

        if (this.modelConfiguration == null) {
            Timber.w("Selected model '%s' is not compatible.", selectedModel);
            Util.afficherMessage(this, getString(R.string.device_not_compatible), getString(R.string.error), 0);
        } else {
            initIconesModes();
            initSpinnerMode();
        }
        Timber.d("InterfaceActivity onCreate completed successfully");
    }

    private void setActionBarTitleWithSelectedModel() {
        String selectedModel = dataStorageManager.getSelectedModel();
        String finalSelectedModel = selectedModel.isEmpty() ? Build.MODEL : selectedModel;
        String androidVersion = Build.VERSION.RELEASE;
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("AIR³ Manager - " + finalSelectedModel + " - Android " + androidVersion);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void finDemandeAccessFichiers() {
    }

    private void initIconesModes() {
        this.iconesModes = new int[5];
        this.iconesModes[0] = R.drawable.mode_kiss;
        this.iconesModes[1] = R.drawable.mode_easy;
        this.iconesModes[2] = R.drawable.mode_expert;
        this.iconesModes[3] = R.drawable.mode_paramotor;
        this.iconesModes[4] = R.drawable.mode_balloon;
    }

    private void initSpinnerMode() {
        SpinnerAdapterAir3 adapterMode;
        if (screenIsSmall())
            adapterMode = new SpinnerAdapterAir3(this, this.iconesModes, modelConfiguration.getLibellesModesCourts());
        else
            adapterMode = new SpinnerAdapterAir3(this, this.iconesModes, modelConfiguration.getLibellesModesLongs());
        this.spinnerMode = findViewById(R.id.spinnerMode);
        this.spinnerMode.setAdapter(adapterMode);
        int indiceMode = this.user.chargerPreferenceMode();
        this.spinnerMode.setSelection(indiceMode);
        this.spinnerMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                InterfaceActivity.this.modifierImage();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void modifierImage() {
        String nomImage = "";
        try {
            int indiceMode = this.spinnerMode.getSelectedItemPosition();
            if (configuration == null) {
                Timber.e("Configuration is null");
                return;
            }
            ItemInterface mode = modelConfiguration.getMode(indiceMode);
            nomImage = IMAGE_PATH + mode.getId() + ".png";
            InputStream in = getAssets().open(nomImage);
            Drawable drawable = Drawable.createFromStream(in, null);
        } catch (Exception ex) {
            Timber.e("erreur chargement image : %s", nomImage);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (spinnerMode != null) {
            sauver();
        }
    }

    private void sauver() {
        int indiceMode = this.spinnerMode.getSelectedItemPosition();
        this.user.setPreferencesInterface(indiceMode, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();    //Call the back button's method
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void resetXCTRack(View view) {
        afficherMessageConfirmationReset();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void afficherMessageConfirmationInterface(View view) {
        String message = getString(R.string.confirmer_reset);
        String titre = getString(R.string.titre_interface);
        AlertDialog.Builder alert = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog);
        alert.setTitle(titre);
        alert.setMessage("\n" + message);
        alert.setPositiveButton(R.string.change, (dialog, id) -> InterfaceActivity.this.executerChangementInterface());
        alert.setNegativeButton(R.string.annuler, (dialog, which) -> dialog.dismiss());
        alert.setIcon(getDrawable(R.drawable.ic_warning));
        alert.show();
    }

    private void executerChangementInterface() {
        changeMode();
        this.gestionFichiers.copierFichierClearPilotBiggerCities(); // Noms des villes en plus grand
    }

    private void changeMode() {
        String message = getString(R.string.changement_interface_termine);
        int indice = this.spinnerMode.getSelectedItemPosition();
        String id = modelConfiguration.getMode(indice).getId();
        String modelFolder = modelConfiguration.getFolder();
        copierFichierBootstrap(modelFolder, id, message);
    }
    private void executerResetInterface() {
        String message = getString(R.string.reset_termine);
        String modelFolder = modelConfiguration.getFolder();
        copierFichierBootstrap(modelFolder, "reset", message);
        this.gestionFichiers.copierFichierClearPilotBiggerCities();
    }
    private void copierFichierBootstrap(String modelFolder, String id, String message) {
        boolean copie = this.gestionFichiers.copyBootstrapFile(modelFolder, id);
        if (!copie)
            message = getString(R.string.erreur_copie);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void afficherMessageConfirmationReset() {
        String message = getString(R.string.confirmer_reset);
        String titre = getString(R.string.titre_reset);
        AlertDialog.Builder alert = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog);
        alert.setTitle(titre);
        alert.setMessage("\n" + message);
        alert.setPositiveButton(R.string.reset_interface, (dialog, id) -> InterfaceActivity.this.executerResetInterface());
        alert.setNegativeButton(R.string.annuler, (dialog, which) -> dialog.dismiss());
        alert.setIcon(getDrawable(R.drawable.ic_warning));
        alert.show();
    }

}