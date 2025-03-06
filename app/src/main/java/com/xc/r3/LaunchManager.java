package com.xc.r3;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import java.util.List;

public class LaunchManager {
    private static final String UPGRADES = "http://www.fly-air3.com/en/software-upgrade-instructions/";
    private static final String AIR3_UPGRADER_PACKAGE_NAME = "com.xc.air3upgrader";
    private static final String AIR3_UPGRADER_DOWNLOAD_URL = "https://ftp.fly-air3.com/Latest_Software_Download/AIR3_Upgrader/";
    private final MainActivity mainActivity;

    public LaunchManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void requestPermission() {
        // Check if Android M or higher
        // Show alert dialog to the user saying a separate permission is needed
        // Launch the settings activity if the user prefers
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + mainActivity.getPackageName()));
        mainActivity.startActivityForResult(intent, MainActivity.ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
    }

    public void lancerXCTrack() {
        Util.lancerXCTrack(mainActivity);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void afficherDialogueUpgrades() {
        if (isAppInstalled(AIR3_UPGRADER_PACKAGE_NAME)) {
            lancerAir3Upgrader();
        } else {
            afficherMessageInstallationAir3Upgrader();
        }
    }

    private void lancerAir3Upgrader() {
        Intent launchIntent = mainActivity.getPackageManager().getLaunchIntentForPackage(AIR3_UPGRADER_PACKAGE_NAME);
        if (launchIntent != null) {
            mainActivity.startActivity(launchIntent);
        } else {
            // Gérer le cas où l'application est installée mais ne peut pas être lancée
            afficherMessage("Erreur", "Impossible de lancer AIR³ Upgrader", CommonActivity.ICONE_APPLICATION);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void afficherMessageInstallationAir3Upgrader() {
        String titre = mainActivity.getString(R.string.titre_installation_upgrader);
        String message = mainActivity.getString(R.string.message_installation_upgrader);
        AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity, R.style.Theme_AppCompat_Light_Dialog_MinWidth);
        alert.setTitle(titre);
        alert.setMessage("\n" + message);
        alert.setPositiveButton(R.string.bouton_download, (dialog, id) -> lancerNavigateur(AIR3_UPGRADER_DOWNLOAD_URL));
        alert.setIcon(mainActivity.getDrawable(R.drawable.air3manager));
        alert.show();
    }

    private void lancerNavigateur(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        mainActivity.startActivity(i);
    }

    public void downloadListeFichiersOpenAir() {
        mainActivity.downloadListeFichiersOpenAir();
    }

    public void downloadListeFichiersTerminee(List<Fichier> fichiers) {
        if (fichiers != null && !fichiers.isEmpty()) {
            Fichier fichier = fichiers.get(0);
            if (fichier.isDateFromFileNameToday())
                lancerRequete(AsyncCallWS.DOWNLOADER, fichier);
            else {
                if (mainActivity.boot) {
                    String message = mainActivity.getString(R.string.fichier) + " : " + fichier.getName() + "\n" + mainActivity.getString(R.string.mauvaise_date);
                    mainActivity.lancerNotification(message, Util.NOTIFICATION_DATE_FICHIER);
                } else {
                    afficherMessageWarning(fichier);
                }
            }
        } else if (mainActivity.boot) {
            //if (mainActivity.user.lancerXCTrackBoot()) {
            //    lancerXCTrack();
            //}
            mainActivity.getIntent().putExtra(Util.BOOT, false);
            mainActivity.finish();
        }
    }

    public void retablirAffichageWidgets() {
        if (!mainActivity.boot) {
            mainActivity.setProgressBarVisibility(View.GONE);
        }
    }

    public void creationFichierTerminee(Fichier fichier) {
        if (mainActivity.boot) {
            //if (mainActivity.user.lancerXCTrackBoot()) {
            //    lancerXCTrack();
            //}
            mainActivity.finish();
        }
    }

    public void lancerRequete(int operation, Fichier fichier) {
        if (!mainActivity.boot) {
            mainActivity.setProgressBarVisibility(View.GONE);
        }
    }

    public void downloadFichierTermine(Fichier fichier) {
        if (mainActivity.boot) {
            //if (mainActivity.user.lancerXCTrackBoot()) {
            //    lancerXCTrack();
            //}
            mainActivity.getIntent().putExtra(Util.BOOT, false);
            mainActivity.finish();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void afficherMessage(String message, String titre, int typeIcone) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity, R.style.Theme_AppCompat_Light_Dialog_MinWidth);
        alert.setTitle(titre);
        alert.setMessage("\n" + message);
        if (typeIcone == CommonActivity.ICONE_APPLICATION)
            alert.setIcon(mainActivity.getDrawable(R.drawable.air3manager));
        else if (typeIcone == CommonActivity.ICONE_ATTENTION)
            alert.setIcon(mainActivity.getDrawable(R.drawable.ic_warning));
        alert.show();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void afficherMessageWarning(Fichier fichier) {
        // la date dans le nom du fichier n'est pas la date du jour
        String titre = mainActivity.getString(R.string.fichier) + " : " + fichier.getName();
        String message = mainActivity.getString(R.string.mauvaise_date) + "\n\n" + mainActivity.getString(R.string.sauver_fichier);
        AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity, R.style.Theme_AppCompat_Light_Dialog_MinWidth);
        alert.setTitle(titre);
        alert.setMessage("\n" + message);
        alert.setPositiveButton(R.string.envoyer, (dialog, id) -> mainActivity.lancerRequete(AsyncCallWS.DOWNLOADER, fichier));
        alert.setNegativeButton(R.string.annuler, (dialog, which) -> dialog.dismiss());
        alert.setIcon(mainActivity.getDrawable(R.drawable.ic_warning));
        alert.show();
    }

    public void afficherAttente() {
        mainActivity.setProgressBarVisibility(View.VISIBLE);
    }

    @SuppressLint("LogNotTimber")
    public void traitementLorsDuBoot(Intent intent, boolean downloadFichierOpenAir, boolean lancerXCTrackBoot) {
        Log.i("MonLog", "Trt lors du boot");
        if (downloadFichierOpenAir) {
            downloadListeFichiersOpenAir();
        } else if (lancerXCTrackBoot) {
            lancerXCTrack();
        }
        mainActivity.finish();
    }

    public void afficherDialoguePreferences() {
        Intent intent = new Intent(mainActivity, PreferencesActivity.class);
        mainActivity.startActivityForResult(intent, MainActivity.PREFERENCES);
    }

    public void afficherAbout() {
        Intent intent = new Intent(mainActivity, AboutActivity.class);
        mainActivity.startActivity(intent);
    }

    public void chooseAccount(boolean downloadFichierOpenAir, boolean userHasAccount) {
        if (downloadFichierOpenAir && !userHasAccount) {
            mainActivity.chooseAccount();
        }
    }
    public boolean isAppInstalled(String packageName) {
        try {
            mainActivity.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void setMenuItemsDownload(Menu menu, boolean downloadFichierOpenAir) {
        menu.getItem(2).setVisible(downloadFichierOpenAir);
        menu.getItem(3).setVisible(downloadFichierOpenAir);
    }

    public void lancerXCGuide() {
        Intent launchIntent = mainActivity.getPackageManager().getLaunchIntentForPackage("indysoft.xc_guide");
        if (launchIntent != null) {
            mainActivity.startActivity(launchIntent);
        } else {
            // Gérer le cas où l'application est installée mais ne peut pas être lancée
            afficherMessage("Erreur", "Impossible de lancer XC Guide", CommonActivity.ICONE_APPLICATION);
        }
    }
}