package com.xc.r3;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;
import timber.log.Timber.DebugTree;

public class MainActivity extends CommonActivity {
    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 2323;
    private ProgressBar progressBar;
    private Menu menu;
    private LaunchManager launchManager;
    private DataStoreManager dataStoreManager;
    private CompositeDisposable disposables = new CompositeDisposable();
    private ActivityResultLauncher<Intent> modelSelectionLauncher;
    private ActivityResultLauncher<Intent> listActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataStoreManager = DataStoreManager.getInstance(this);
        launchManager = new LaunchManager(this);
        modelSelectionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Continuez la configuration
                        continueSetup(savedInstanceState);
                    } else {
                        // Gérez l'annulation ou l'échec
                        finish(); // Ou toute autre action appropriée
                    }
                }
        );
        listActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == ListActivity.PAS_ACCES_INTERNET) {
                        afficherMessageAccesInternet();
                    }
                }
        );
        disposables.add(dataStoreManager.getManualModelSelected()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isManualModelSelected -> {
                    Timber.d("MainActivity: onCreate - isManualModelSelected: " + isManualModelSelected);
                    if (!isManualModelSelected) {
                        // Lancez l'activité de sélection de modèle
                        Intent intent = new Intent(MainActivity.this, ModelSelectionActivity.class);
                        modelSelectionLauncher.launch(intent);
                    } else {
                        continueSetup(savedInstanceState);
                    }
                }, throwable -> Timber.e(throwable, "Error getting manual model selection")));
    }

    private void continueSetup(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        //launchManager = new LaunchManager(this); // Removed because already initialized in onCreate
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
            launchManager.traitementLorsDuBoot(intent);
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
        setActionBarTitleWithSelectedModel();
    }

    private void setActionBarTitleWithSelectedModel() {
        disposables.add(dataStoreManager.getSelectedModel()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(selectedModel -> {
                    String finalSelectedModel = selectedModel != null ? selectedModel : Build.MODEL;
                    String androidVersion = Build.VERSION.RELEASE;
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle("AIR³ Upgrader - " + finalSelectedModel + " - Android " + androidVersion);
                    }
                }, throwable -> Timber.e(throwable, "Error getting selected model")));
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
        // Vérifiez d'abord si launchManager est initialisé
        if (launchManager != null) {
            launchManager.setMenuItemsDownload(menu);
        }
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
                listActivityLauncher.launch(intent);
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

    public void setProgressBarVisibility(int visibility) {
        progressBar.setVisibility(visibility);
    }

    private void configureAppWithModel(String modelName) {
        // Logique pour configurer l'application avec le modèle sélectionné
        Timber.d("Configuring app with model: " + modelName);
        // Vous pouvez ajouter ici votre logique spécifique de configuration
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Disposer des observables RxJava pour éviter les fuites de mémoire
        if (disposables != null) {
            disposables.dispose();
        }
    }
}
