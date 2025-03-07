package com.xc.r3;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
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
    private Spinner spinnerTheme;
    private int[] iconesModes;
    private int[] iconesThemes;
    private ImageView image;
    private ViewChangeInterface vueChangeInterface;
    private Configuration configuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interface);
        ActionBar actionBar = getSupportActionBar();  //Make sure you are extending ActionBarActivity
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        this.image = findViewById(R.id.imageView);
        this.user = User.getInstance(this);
        this.configuration = GestionFichiers.lireConfiguration(this);

        initIconesModes();
        initSpinnerMode();

        initIconesThemes();
        initSpinnerTheme();
    }

    @Override
    protected void finDemandeAccessFichiers() {

    }

    @Override
    public void afficherMessage(String message, String titre, int typeIcone) {
    }

    private void initIconesModes() {
        this.iconesModes = new int[5];
        this.iconesModes[0] = R.drawable.mode_kiss;
        this.iconesModes[1] = R.drawable.mode_easy;
        this.iconesModes[2] = R.drawable.mode_expert;
        this.iconesModes[3] = R.drawable.mode_paramotor;
        this.iconesModes[4] = R.drawable.mode_balloon;
    }

    private void initIconesThemes() {
        this.iconesThemes = new int[3];
        this.iconesThemes[0] = R.drawable.theme_black;
        this.iconesThemes[1] = R.drawable.theme_white;
        this.iconesThemes[2] = R.drawable.theme_e_ink;
    }

    private void initSpinnerMode() {
        SpinnerAdapterAir3 adapterMode;
        if (screenIsSmall())
            adapterMode = new SpinnerAdapterAir3(this, this.iconesModes, configuration.getLibellesModesCourts());
        else
            adapterMode = new SpinnerAdapterAir3(this, this.iconesModes, configuration.getLibellesModesLongs());
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

    private void initSpinnerTheme() {
        // adapter theme
        SpinnerAdapterAir3 adapterTheme = new SpinnerAdapterAir3(this, this.iconesThemes, configuration.getLibellesThemes());
        this.spinnerTheme = findViewById(R.id.spinnerTheme);
        this.spinnerTheme.setAdapter(adapterTheme);
        int indiceTheme = this.user.chargerPreferenceTheme();
        this.spinnerTheme.setSelection(indiceTheme);
        this.spinnerTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
            int indiceTheme = this.spinnerTheme.getSelectedItemPosition();
            ItemInterface mode = configuration.getMode(indiceMode);
            ItemInterface theme = configuration.getTheme(indiceTheme);
            nomImage = IMAGE_PATH + mode.getId() + "_" + theme.getId() + ".png";
            InputStream in = getAssets().open(nomImage);
            Drawable drawable = Drawable.createFromStream(in, null);
            this.image.setImageDrawable(drawable);
        } catch (Exception ex) {
            Timber.e("erreur chargement image : %s", nomImage);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sauver();
        finish();
    }

    private void sauver() {
        int indiceMode = this.spinnerMode.getSelectedItemPosition();
        int indiceTheme = this.spinnerTheme.getSelectedItemPosition();
        this.user.setPreferencesInterface(indiceMode, indiceTheme);
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
        String message = getString(R.string.confirmer_interface);
        String titre = getString(R.string.titre_interface);
        AlertDialog.Builder alert = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog);
        alert.setTitle(titre);
        alert.setMessage("\n" + message);
        alert.setPositiveButton(R.string.change, (dialog, id) -> InterfaceActivity.this.executerChangementInterface());
        alert.setNegativeButton(R.string.annuler, (dialog, which) -> dialog.dismiss());
        String mode = this.spinnerMode.getSelectedItem().toString();
        String theme = this.spinnerTheme.getSelectedItem().toString();
        vueChangeInterface = new ViewChangeInterface(this, mode, theme);
        alert.setView(vueChangeInterface);
        alert.setIcon(getDrawable(R.drawable.ic_warning));
        alert.show();
    }

    private void executerChangementInterface() {
        if (this.vueChangeInterface.isModeChecked())
            changeMode();
        if (this.vueChangeInterface.isThemeChecked())
            changeTheme();
        this.gestionFichiers.copierFichierHyperPilotBiggerCities(); // Noms des villes en plus grand
    }

    private void changeTheme() {
        String message = getString(R.string.changement_interface_termine);
        int indice = this.spinnerTheme.getSelectedItemPosition();
        String nomFichier = configuration.getTheme(indice).getXcbs();
        String id = configuration.getTheme(indice).getId();
        copierFichierBootstrap(id, nomFichier, message);
    }

    private void changeMode() {
        String message = getString(R.string.changement_interface_termine);
        int indice = this.spinnerMode.getSelectedItemPosition();
        String nomFichier = configuration.getMode(indice).getXcbs();
        String id = configuration.getMode(indice).getId();
        copierFichierBootstrap(id, nomFichier, message);
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

    private void executerResetInterface() {
        String message = getString(R.string.reset_termine);
        String nomFichier = configuration.getReset();
        copierFichierBootstrap("reset", nomFichier, message);
        this.gestionFichiers.copierFichierHyperPilotBiggerCities();
    }

    private void copierFichierBootstrap(String id, String nomFichier, String message) {
        boolean copie = this.gestionFichiers.copyBootstrapFile(id, nomFichier + ".xcbs");
        if (!copie)
            message = getString(R.string.erreur_copie);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}