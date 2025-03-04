package com.xc.r3;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.AccountPicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

@SuppressWarnings("ALL")
public abstract class CommonActivity extends AppCompatActivity {
    public final static String SCOPE = "https://www.googleapis.com/auth/drive";
    public static final int ICONE_ATTENTION = 1;
    public static final int ICONE_APPLICATION = 2;
    public static final int NB_FICHIERS = 10;
    public static final int AUTHORIZATION_CODE = 1;
    public static final int ACCOUNT_CODE = 2;
    public static final int PREFERENCES = 3;
    public static final int LISTE = 4;
    public static final String FICHIERS = "Fichiers";
    public static final String CHANNEL_ID = "1";
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 68;
    private static final String ACCES_GOOGLE_DRIVE = "http://www.fly-air3.com/support/openair2air3/";
    public static String PACKAGE_NAME = "";
    private static final boolean ALARME_NOTIFICATION = true;
    protected User user;
    protected AccountManager accountManager;
    protected List<Fichier> fichiers = new ArrayList<>();
    protected GestionFichiers gestionFichiers;
    protected int operationEnCours;
    protected Preferences preferences;
    protected boolean boot;
    protected boolean notificationDateFichier;
    protected boolean notificationAccesInternet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PACKAGE_NAME = getApplicationContext().getPackageName();
        this.user = User.getInstance(this);
        this.preferences = new Preferences(this);
        this.accountManager = AccountManager.get(this);
        this.operationEnCours = 0;
        this.gestionFichiers=new GestionFichiers(this);
        this.gestionFichiers.chargerFichiers();
        this.gestionFichiers.gererRepertoireFichiersTemporaires();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        CharSequence name = "Channel 1";
        String description = "Description channel 1";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("1", name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.gestionFichiers.gererRepertoireFichiersTemporaires();
    }

    protected boolean screenIsSmall() {
        int screenMask = getResources().getConfiguration().screenLayout;
        if ((screenMask & Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_SMALL) {
            return true;
        }
        return (screenMask & Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_NORMAL;
    }

    public void chooseAccount() {
        if (Build.VERSION.SDK_INT < 26) {
            int hasGetAccountsPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.GET_ACCOUNTS);
            if (hasGetAccountsPermission != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.GET_ACCOUNTS)) {
                    afficherMessageDemandePermissions();
                    return;
                }
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.GET_ACCOUNTS},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
            invalidateToken();
            requestToken();
        } else {
            Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
                    false, null, null, null, null);
            startActivityForResult(intent, ACCOUNT_CODE);
        }
    }

    public void afficherMessageDemandeAccesFichiersPartages() {
        String message = getString(R.string.demande_acces);
        String titre = getString(R.string.titre_demande_acces);
        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_MinWidth);
        alert.setTitle(titre);
        alert.setMessage("\n" + message);
        alert.setPositiveButton(R.string.bouton_demande, (dialog, id) -> CommonActivity.this.afficherDemandeConfirmationEnvoieDemandeAcces());
        alert.setNegativeButton(R.string.annuler, (dialog, which) -> {
            dialog.dismiss();
            CommonActivity.this.finDemandeAccessFichiers();
        });
        alert.setIcon(getDrawable(R.drawable.ic_warning));
        alert.setCancelable(false);
        alert.show();
    }

    protected abstract void finDemandeAccessFichiers();

    private void executerEnvoyerDemandeAcces() {
        if (this instanceof MainActivity) {
            ((MainActivity) this).afficherAttente();
        }
        String subject = getString(R.string.sujet_email);
        String message = subject + " par : " + this.user.getUser();
        AsyncSendMail task = new AsyncSendMail();
        Object[] params = new Object[3];
        params[0] = this;
        params[1] = subject;
        params[2] = message;
        task.execute(params);
    }

    public void afficherMessageMailEnvoye(boolean operationReussie) {
        if (operationReussie) {
            afficherMessageBienEnvoye();
        } else {
            String message = getString(R.string.erreur_envoie_message);
            String titre = getString(R.string.attention);
            this.afficherMessage(message, titre, ICONE_ATTENTION);
        }
    }

    private void afficherMessageBienEnvoye() {
        String message = getString(R.string.message_envoye);
        String titre = getString(R.string.titre_message_envoye);
        AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_MinWidth);
        alert.setTitle(titre);
        alert.setMessage("\n" + message);
        alert.setPositiveButton(android.R.string.ok, (dialog, id) -> {
            dialog.dismiss();
            finDemandeAccessFichiers();
        });
        alert.setIcon(getDrawable(R.drawable.air3manager));
        alert.setCancelable(false);
        alert.show();
    }

    public void afficherDemandeConfirmationEnvoieDemandeAcces() {
        String message = getString(R.string.confirmer_demande_acces);
        String titre = getString(R.string.titre_confirmer_demande_acces);
        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_MinWidth);
        alert.setTitle(titre);
        alert.setMessage("\n" + message);
        alert.setPositiveButton(R.string.titre_confirmer_demande_acces, (dialog, id) -> CommonActivity.this.executerEnvoyerDemandeAcces());
        alert.setNegativeButton(R.string.annuler, (dialog, which) -> {
            dialog.dismiss();
            CommonActivity.this.finDemandeAccessFichiers();
        });
        alert.setIcon(getDrawable(R.drawable.ic_warning));
        alert.setCancelable(false);
        alert.show();
    }

    public void afficherMessageDemandePermissions() {
        String message = getString(R.string.demande_acces_contacts);
        String titre = getString(R.string.titre_demande_acces_contacts);
        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_MinWidth);
        alert.setTitle(titre);
        alert.setMessage("\n" + message);
        alert.setPositiveButton(R.string.bouton_demande_acces_contacts, (dialog, id) -> ActivityCompat.requestPermissions(CommonActivity.this,
                new String[]{Manifest.permission.GET_ACCOUNTS},
                REQUEST_CODE_ASK_PERMISSIONS));
        alert.setNegativeButton(R.string.annuler, (dialog, which) -> dialog.dismiss());
        alert.setIcon(getDrawable(R.drawable.ic_warning));
        alert.setCancelable(false);
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
                            false, null, null, null, null);
                    startActivityForResult(intent, ACCOUNT_CODE);
                } else {
                    // Permission Denied
                    Toast.makeText(this, "GET_ACCOUNTS Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    protected void requestToken() {
        Account userAccount = null;
        String user = this.user.getUser();
        for (Account account : accountManager.getAccountsByType("com.google")) {
            if (account.name.equals(user)) {
                userAccount = account;
                break;
            }
        }
        accountManager.getAuthToken(userAccount, "oauth2:" + SCOPE, null, this,
                new CommonActivity.OnTokenAcquired(), null);
    }

    /**
     * call this method if your token expired, or you want to request a new
     * token for whatever reason. call requestToken() again afterwards in order
     * to get a new token.
     */
    protected void invalidateToken() {
        AccountManager accountManager = AccountManager.get(this);
        accountManager.invalidateAuthToken("com.google",
                user.getToken());
        user.setToken(null);
    }

    public void handleError(Exception exception) {
        Timber.e(exception.toString());
    }

    public User getUser() {
        return this.user;
    }

    protected String[] getTabNomsFichiers() {
        String[] tabNomsFichiers;
        if (this.fichiers.isEmpty()) {
            tabNomsFichiers = new String[1];
            tabNomsFichiers[0] = this.getString(R.string.liste_vide);
        } else {
            tabNomsFichiers = new String[this.fichiers.size()];
            for (int i = 0; i < this.fichiers.size(); i++) {
                tabNomsFichiers[i] = fichiers.get(i).getName();
            }
        }
        return tabNomsFichiers;
    }

    public void trtErreurAccesInternet() {
        if (boot && user.downloadFichierOpenAir()) {
            String message = getString(R.string.erreur_internet);
            lancerNotification(message, Util.NOTIFICATION_ACCES_INTERNET);
        } else {
            afficherMessageAccesInternet();
        }
    }

    public void afficherMessageErreurAccesFichier() {
        String titre = this.getString(android.R.string.dialog_alert_title);
        String message = getString(R.string.erreur_acces_via_bouton);
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_MinWidth);
        alert.setTitle(titre);
        alert.setMessage(message);
        alert.setPositiveButton(getString(R.string.bouton_website), (dialog, id) -> lancerNavigateurAccesGoogleDrive());
        alert.setIcon(getDrawable(R.drawable.ic_warning));
        alert.show();
    }

    private void lancerNavigateurAccesGoogleDrive() {
        String url = ACCES_GOOGLE_DRIVE;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private class OnTokenAcquired implements AccountManagerCallback<Bundle> {
        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            try {
                Bundle bundle = result.getResult();
                Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if (launch != null) {
                    startActivityForResult(launch, AUTHORIZATION_CODE);
                } else {
                    String token = bundle
                            .getString(AccountManager.KEY_AUTHTOKEN);
                    user.setToken(token);
                    continuerApresIdentification();
                }
            } catch (Exception e) {
                trtErreurAccesInternet();
            }
        }
    }

    public void afficherMessageAccesInternet() {
        String titre = this.getString(android.R.string.dialog_alert_title);
        String message = getString(R.string.erreur_internet);
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_MinWidth);
        alert.setTitle(titre);
        alert.setMessage(message);
        alert.setPositiveButton(android.R.string.ok, (dialog, which) -> {
        });
        alert.setIcon(getDrawable(R.drawable.ic_warning));
        alert.show();
    }

    public void lancerNotification(String message, String butNotification) {
        Timber.i("lancement notification : %s", butNotification);
        // Create an explicit intent for an Activity
        String titre = this.getString(R.string.app_name);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(butNotification, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent; // = PendingIntent.getActivity(this, 0, intent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(
                    getApplication(),
                    0,
                    intent,
                    PendingIntent.FLAG_MUTABLE
            );
        } else {
            pendingIntent = PendingIntent.getActivity(
                    getApplication(),
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(titre).setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setChannelId(CHANNEL_ID).setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, mBuilder.build());
        if (ALARME_NOTIFICATION) {
            MediaPlayer mPlayer = MediaPlayer.create(this, R.raw.notification);
            mPlayer.start();
        }
    }

    protected void lancerRequete(int operation, Fichier fichier) {
        AsyncCallWS task = new AsyncCallWS();
        Object[] params = new Object[4];
        params[0] = this;
        params[1] = operation;
        params[2] = this.user.getToken();
        params[3] = fichier;
        task.execute(params);
    }

    public void downloadFichierTermine(Fichier fichier, String reponse) {
        if (!reponse.equals(WebServiceRest.ERREUR) && fichier != null) {
            String message = this.getString(R.string.message_download_fichier) + "\n\t" + fichier.getName();
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            this.gestionFichiers.sauverFichierOpenair(fichier);
        } else {
            afficherMessageAccesInternet();
            if (boot) {
                String message = getString(R.string.erreur_acces);
                this.lancerNotification(message, Util.NOTIFICATION_ACCES_INTERNET);
            }
        }
    }

    public void relancerRequeteAvecFreshToken(Integer operationEnCours) {
        this.operationEnCours = operationEnCours;
        invalidateToken();
        requestToken();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == AUTHORIZATION_CODE) {
                requestToken();
            } else if (requestCode == ACCOUNT_CODE) {
                String accountName = data
                        .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                user.setUser(accountName);

                // invalidate old tokens which might be cached. we want a fresh
                // one, which is guaranteed to work
                invalidateToken();
                requestToken();
            }
        }
    }

    public void creationFichierTerminee(Fichier fichier) {
        String message = getString(R.string.message_creation_fichier_terminee) +
                "\n\t" + GestionFichiers.FICHIER_OA +
                "\n\t" + fichier.getName();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    protected void continuerApresIdentification() {
        if (this.operationEnCours == AsyncCallWS.LISTER)
            lancerRequete(AsyncCallWS.LISTER, null);
    }

    public void downloadListeFichiersTerminee(List<Fichier> fichiers) {
        Timber.i("download ListeFichiers terminee. ");
        if (fichiers != null && !fichiers.isEmpty()) {
            this.fichiers = fichiers;
            gestionFichiers.sauverFichiers();
        }
    }

    public List<Fichier> getFichiers() {
        return fichiers;
    }

    public void setFichiers(List<Fichier> fichiers) {
        this.fichiers = fichiers;
    }

    public void downloadListeFichiersOpenAir() {
        this.operationEnCours = AsyncCallWS.LISTER;
        if (user.getUser() == null
                || user.getToken() == null) {
            chooseAccount();
        } else {
            lancerRequete(AsyncCallWS.LISTER, null);
        }
    }

    public abstract void afficherMessage(String message, String titre, int typeIcone);
}