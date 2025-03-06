package com.xc.r3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import java.util.List;

import timber.log.Timber;
import timber.log.Timber.DebugTree;

public class MainActivity extends CommonActivity {
    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 2323;
    private ProgressBar progressBar;
    private Menu menu;
    private LaunchManager launchManager;
    private DataStorageManager dataStorageManager;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        launchManager = new LaunchManager(this);
        user = User.getInstance(this);
        if (!Settings.canDrawOverlays(this)) {
            launchManager.requestPermission();
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree() {
                @Override
                protected void log(int priority, String tag, @NonNull String message, Throwable t) {
                    super.log(priority, "MonLog", message, t);
                }
            });
            Timber.v("Démarrage de Timber!");
        }

        // Get the DataStorageManager instance
        dataStorageManager = DataStorageManager.getInstance(this);

        // Retrieve the selected model
        String selectedModel = dataStorageManager.getSelectedModel();

        // Log the selected model
        Timber.d("Selected Model: %s", selectedModel);

        // Set the action bar title
        setActionBarTitleWithSelectedModel();

        this.progressBar = findViewById(R.id.progressBar);
        this.preferences = new Preferences(this);
        notificationDateFichier = false;
        notificationAccesInternet = false;
        boot = false;
        Intent intent = this.getIntent();
        if (intent != null) {
            boot = intent.getBooleanExtra(Util.BOOT, false);
            notificationAccesInternet = intent.getBooleanExtra(Util.NOTIFICATION_ACCES_INTERNET, false);
            notificationDateFichier = intent.getBooleanExtra(Util.NOTIFICATION_DATE_FICHIER, false);
        }
        if (boot)
            launchManager.traitementLorsDuBoot(intent, user.downloadFichierOpenAir(), user.lancerXCTrackBoot());
        else if (notificationAccesInternet)
            afficherMessageAccesInternet();
        else if (notificationDateFichier) {
            Fichier fichier = fichiers.get(0);
            if (fichier != null) launchManager.afficherMessageWarning(fichier);
        }

        ImageView imageXCTrackLaunch = findViewById(R.id.imageXCTrackLaunch);
        ImageView imageXCTrackInterface = findViewById(R.id.imageXCTrackInterface);
        ImageView imageXCGuideLaunch = findViewById(R.id.imageXCGuideLaunch);
        ImageView imageCheckForUpgrades = findViewById(R.id.imageCheckForUpgrades);

        imageXCTrackLaunch.setOnClickListener(v -> launchManager.lancerXCTrack());
        imageXCTrackInterface.setOnClickListener(v -> lancerInterfaceActivity());
        imageXCGuideLaunch.setOnClickListener(v -> launchManager.lancerXCGuide());
        imageCheckForUpgrades.setOnClickListener(v -> launchManager.afficherDialogueUpgrades());
    }
    private void setActionBarTitleWithSelectedModel() {
        String selectedModel = dataStorageManager.getSelectedModel();
        String finalSelectedModel = selectedModel.isEmpty() ? Build.MODEL : selectedModel;
        String androidVersion = Build.VERSION.RELEASE;
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("AIR³ Manager - " + finalSelectedModel + " - Android " + androidVersion);
        }
    }

    private void lancerInterfaceActivity() {
        Intent intent = new Intent(this, InterfaceActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        launchManager.setMenuItemsDownload(menu, user.downloadFichierOpenAir());
        return true;
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                launchManager.afficherDialoguePreferences();
                break;
            case R.id.action_interfaces:
                lancerInterfaceActivity();
                break;
            case R.id.action_download_fichier:
                launchManager.downloadListeFichiersOpenAir();
                break;
            case R.id.action_liste_fichiers:
                Intent intent = new Intent(this, ListActivity.class);
                startActivityForResult(intent, MainActivity.LISTE);
                break;
            case R.id.action_upgrades:
                launchManager.afficherDialogueUpgrades();
                break;
            case R.id.action_about:
                launchManager.afficherAbout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void downloadListeFichiersTerminee(List<Fichier> fichiers) {
        super.downloadListeFichiersTerminee(fichiers);
        launchManager.downloadListeFichiersTerminee(fichiers);
    }

    public void retablirAffichageWidgets() {
        launchManager.retablirAffichageWidgets();
    }

    @Override
    public void creationFichierTerminee(Fichier fichier) {
        super.creationFichierTerminee(fichier);
        launchManager.creationFichierTerminee(fichier);
    }

    @Override
    protected void lancerRequete(int operation, Fichier fichier) {
        super.lancerRequete(operation, fichier);
        launchManager.lancerRequete(operation, fichier);
    }

    public void downloadFichierTermine(Fichier fichier) {
        super.downloadFichierTermine(fichier, "");
        launchManager.downloadFichierTermine(fichier);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PREFERENCES) {
            launchManager.chooseAccount(user.downloadFichierOpenAir(), user.getUser() != null && user.getToken() != null);
        } else if (requestCode == MainActivity.LISTE) {
            if (resultCode == ListActivity.PAS_ACCES_INTERNET) {
                afficherMessageAccesInternet();
            }
        } else if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                afficherMessage("Acces refusé", "Erreur", ICONE_APPLICATION);
            } else {
                afficherMessage("Acces autorisé", "Info", ICONE_APPLICATION);
            }
        }
    }
    @Override
    protected void finDemandeAccessFichiers() {
    }

    public void downloadListeFichiersOpenAir() {
        launchManager.downloadListeFichiersOpenAir();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void afficherMessage(String message, String titre, int typeIcone) {
        launchManager.afficherMessage(message, titre, typeIcone);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void afficherMessageWarning(Fichier fichier) {
        launchManager.afficherMessageWarning(fichier);
    }

    void afficherAttente() {
        launchManager.afficherAttente();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setActionBarTitleWithSelectedModel();
    }

    public void setProgressBarVisibility(int visibility) {
        progressBar.setVisibility(visibility);
    }
}