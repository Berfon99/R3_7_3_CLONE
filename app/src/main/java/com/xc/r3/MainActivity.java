package com.xc.r3;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import timber.log.Timber;
import timber.log.Timber.DebugTree;

public class MainActivity extends CommonActivity {
    private static final String UPGRADES = "http://www.fly-air3.com/en/software-upgrade-instructions/";
    private static final String AIR3_UPGRADER_PACKAGE_NAME = "com.xc.air3upgrader";
    private static final String AIR3_UPGRADER_DOWNLOAD_URL = "https://ftp.fly-air3.com/Latest_Software_Download/AIR3_Upgrader/";
    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 2323;
    private ProgressBar progressBar;
    private ImageView imageXCTrack;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.content);
        if (!Settings.canDrawOverlays(this)) {
            requestPermission();
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
        this.imageXCTrack = findViewById(R.id.imageXCTrack);
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
            traitementLorsDuBoot(intent);
        else if (notificationAccesInternet)
            afficherMessageAccesInternet();
        else if (notificationDateFichier) {
            Fichier fichier = fichiers.get(0);
            if (fichier != null) afficherMessageWarning(fichier);
        }
    }

    private void requestPermission() {
        // Check if Android M or higher
        // Show alert dialog to the user saying a separate permission is needed
        // Launch the settings activity if the user prefers
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + this.getPackageName()));
        startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
    }

    private void lancerInterfaceActivity() {
        Intent intent = new Intent(this, InterfaceActivity.class);
        startActivity(intent);
    }

    void afficherAttente() {
        this.progressBar.setVisibility(View.VISIBLE);
    }

    @SuppressLint("LogNotTimber")
    private void traitementLorsDuBoot(Intent intent) {
        Log.i("MonLog", "Trt lors du boot");
        if (user.downloadFichierOpenAir()) {
            downloadListeFichiersOpenAir(null);
        } else if (user.lancerXCTrackBoot()) {
            Util.lancerXCTRact(this);
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu=menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setMenuItemsDownload(menu);
        return true;
    }
    private void setMenuItemsDownload(Menu menu) {
        menu.getItem(2).setVisible(this.user.downloadFichierOpenAir());
        menu.getItem(3).setVisible(this.user.downloadFichierOpenAir());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                afficherDialoguePreferences();
                break;
            case R.id.action_interfaces:
                lancerInterfaceActivity();
                break;
            case R.id.action_download_fichier:
                downloadListeFichiersOpenAir();
                break;
            case R.id.action_liste_fichiers:
                Intent intent = new Intent(this, ListActivity.class);
                startActivityForResult(intent, MainActivity.LISTE);
                break;
            case R.id.action_upgrades:
                afficherDialogueUpgrades();
                break;
            case R.id.action_about:
                afficherAbout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void afficherDialogueUpgrades() {
        if (isAppInstalled(AIR3_UPGRADER_PACKAGE_NAME)) {
            lancerAir3Upgrader();
        } else {
            afficherMessageInstallationAir3Upgrader();
        }
    }

    private void lancerAir3Upgrader() {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(AIR3_UPGRADER_PACKAGE_NAME);
        if (launchIntent != null) {
            startActivity(launchIntent);
        } else {
            // Gérer le cas où l'application est installée mais ne peut pas être lancée
            afficherMessage("Erreur", "Impossible de lancer AIR³ Upgrader", ICONE_APPLICATION);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void afficherMessageInstallationAir3Upgrader() {
        String titre = getString(R.string.titre_installation_upgrader);
        String message = getString(R.string.message_installation_upgrader);
        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_MinWidth);
        alert.setTitle(titre);
        alert.setMessage("\n" + message);
        alert.setPositiveButton(R.string.bouton_download, (dialog, id) -> lancerNavigateur(AIR3_UPGRADER_DOWNLOAD_URL));
        alert.setIcon(getDrawable(R.drawable.air3manager));
        alert.show();
    }

    private void lancerNavigateur(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private boolean isAppInstalled(String packageName) {
        try {
            getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    private void afficherAbout() {
        Configuration configuration = GestionFichiers.lireConfiguration(this);
        String titre = getString(R.string.titre_a_propos_de) + " " + getString(R.string.app_name);
        String version = getString(R.string.version) + " : " + configuration.getVersion();
        String developpeur = getString(R.string.developpe_par) + " : O. Legrand";
        String web = getString(R.string.web) + " : " + "www.fly-air3.com";
        String message = version + "\n" + web + "\n" + developpeur;
        afficherMessage(message, titre, ICONE_APPLICATION);
    }

    private void afficherDialoguePreferences() {
        Intent intent = new Intent(this, PreferencesActivity.class);
        this.startActivityForResult(intent, MainActivity.PREFERENCES);
    }

    public void lancerXCTrack(View view) {
        Util.lancerXCTRact(this);
    }

    public void downloadListeFichiersTerminee(List<Fichier> fichiers) {
        super.downloadListeFichiersTerminee(fichiers);
        if (fichiers != null && !fichiers.isEmpty()) {
            Fichier fichier = fichiers.get(0);
            if (fichier.isDateFromFileNameToday())
                lancerRequete(AsyncCallWS.DOWNLOADER, fichier);
            else {
                if (boot) {
                    String message = this.getString(R.string.fichier) + " : " + fichier.getName() + "\n" + getString(R.string.mauvaise_date);
                    lancerNotification(message, Util.NOTIFICATION_DATE_FICHIER);
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                } else {
                    afficherMessageWarning(fichier);
                }
            }
        } else if (boot) {
            if (user.lancerXCTrackBoot()) {
                Util.lancerXCTRact(this);
            }
            this.getIntent().putExtra(Util.BOOT, false);
            finish();
        }
    }

    public void retablirAffichageWidgets() {
        if (!boot) {
            this.progressBar.setVisibility(View.GONE);
            this.imageXCTrack.setEnabled(true);
        }
    }

    @Override
    public void creationFichierTerminee(Fichier fichier) {
        super.creationFichierTerminee(fichier);
        if (boot) {
            if (user.lancerXCTrackBoot()) {
                Util.lancerXCTRact(this);
            }
            this.finish();
        }
    }

    @Override
    protected void lancerRequete(int operation, Fichier fichier) {
        super.lancerRequete(operation, fichier);
        if (!boot) {
            this.imageXCTrack.setEnabled(false);
            this.progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void downloadFichierTermine(Fichier fichier) {
        super.downloadFichierTermine(fichier, "");
        if (boot) {
            if (user.lancerXCTrackBoot()) {
                Util.lancerXCTRact(this);
            }
            this.getIntent().putExtra(Util.BOOT, false);
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PREFERENCES) {
            if (user.downloadFichierOpenAir()) {
                if (user.getUser() == null
                        || user.getToken() == null) {
                    chooseAccount();
                }
            }
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

    public void downloadListeFichiersOpenAir(View view) {
        this.downloadListeFichiersOpenAir();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void afficherMessage(String message, String titre, int typeIcone) {
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_MinWidth);
        alert.setTitle(titre);
        alert.setMessage("\n" + message);
        if (typeIcone == ICONE_APPLICATION) alert.setIcon(getDrawable(R.drawable.air3manager));
        else if (typeIcone == ICONE_ATTENTION) alert.setIcon(getDrawable(R.drawable.ic_warning));
        alert.show();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void afficherMessageWarning(Fichier fichier) {
        // la date dans le nom du fichier n'est pas la date du jour
        String titre = this.getString(R.string.fichier) + " : " + fichier.getName();
        String message = getString(R.string.mauvaise_date) + "\n\n" + getString(R.string.sauver_fichier);
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_MinWidth);
        alert.setTitle(titre);
        alert.setMessage("\n" + message);
        alert.setPositiveButton(R.string.envoyer, (dialog, id) -> MainActivity.this.lancerRequete(AsyncCallWS.DOWNLOADER, fichier));
        alert.setNegativeButton(R.string.annuler, (dialog, which) -> dialog.dismiss());
        alert.setIcon(getDrawable(R.drawable.ic_warning));
        alert.show();
    }
}
