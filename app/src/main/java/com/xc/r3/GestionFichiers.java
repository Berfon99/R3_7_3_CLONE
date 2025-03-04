package com.xc.r3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import static android.content.Context.MODE_PRIVATE;
import static com.xc.r3.CommonActivity.FICHIERS;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

public class GestionFichiers {
    private static final String DIR_FICHIERS_TEMP = "temp";
    private static final String ACTION_FILE_COPY = "air3manager.intent.action.FILE_COPY";
    private static final String XCBS_PATH = "xcbs/";
    private static final String THEME_PATH = "theme/";
    public static final String FICHIER_OA = "MOST_RECENT_OA.txt";
    private static final String FICHIER_HYPERPILOT_BIGGER_CITIES = "hyperpilot-bigger-cities.xml"; // Noms des villes en plus grand
    private static final String FICHIER_CONFIGURATION = "configuration.json";
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private final CommonActivity activity;

    public GestionFichiers(CommonActivity activity) {
        this.activity = activity;
        verifyStoragePermissions();
    }

    public boolean copierFichierHyperPilotBiggerCities() {
        boolean copie = true;
        try {
            creerFichierHyperPilotBiggerCitiesEnLocal(FICHIER_HYPERPILOT_BIGGER_CITIES);
            sauverFichierDansXCTrack(FICHIER_HYPERPILOT_BIGGER_CITIES);
        } catch (Exception ex) {
            copie = false;
            Timber.e(ex);
            Toast.makeText(activity, "erreur lors de la copie fichier hyperpilot-bigger-cities.xml\n", Toast.LENGTH_LONG).show();
        }
        return copie;
    }

    private void creerFichierHyperPilotBiggerCitiesEnLocal(String fichierHyperpilotBiggerCities) throws IOException {
        InputStream in = null;
        FileOutputStream out = null;
        try {
            in = activity.getAssets().open(THEME_PATH + fichierHyperpilotBiggerCities);
            String contenuFichier = CharStreams.toString(new InputStreamReader(
                    in, Charsets.UTF_8));
            creerFichierEnLocal(contenuFichier, fichierHyperpilotBiggerCities);
        } finally {
            if (out != null) out.close();
            if (in != null) in.close();
        }
    }

    @SuppressLint("TimberArgCount")
    public boolean copyBootstrapFile(String id, String nomFichier) {
        boolean copie = true;
        try {
            creerFichierXcbsEnLocal(id, nomFichier);
            sauverFichierDansXCTrack(nomFichier);
        } catch (Exception ex) {
            copie = false;
            Timber.e(ex);
            Toast.makeText(activity, "erreur lors de la copie fichier bootstrap\n", Toast.LENGTH_LONG).show();
        }
        return copie;
    }

    private void creerFichierXcbsEnLocal(String id, String nomFichier) throws Exception {
        InputStream in = null;
        FileOutputStream out = null;
        try {
            in = activity.getAssets().open(XCBS_PATH + id + "/" + nomFichier);
            String contenuFichier = CharStreams.toString(new InputStreamReader(
                    in, Charsets.UTF_8));
            creerFichierEnLocal(contenuFichier, nomFichier);
        } finally {
            if (out != null) out.close();
            if (in != null) in.close();
        }
    }

    public void gererRepertoireFichiersTemporaires() {
        File root = new File(this.activity.getApplicationContext().getFilesDir(), DIR_FICHIERS_TEMP);
        if (root.exists()) { // on supprime les fichiers existants
            File[] files = root.listFiles();
            if (files != null) {
                Arrays.stream(files).forEach(File::delete);
            }
        } else {
            root.mkdirs();
        }
    }

    private void creerFichierEnLocal(String contenuFichier, String filename) {
        try {
            File root = new File(this.activity.getApplicationContext().getFilesDir(), DIR_FICHIERS_TEMP);
            File file = new File(root, filename);
            FileWriter writer = new FileWriter(file);
            writer.write(contenuFichier);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Toast.makeText(this.activity.getApplicationContext(), "Problems: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Timber.e(e);
        }
    }

    public void sauverFichierOpenair(Fichier fichier) {
        this.creerFichierEnLocal(fichier.getContenu(), "MOST_RECENT_OA.txt");
        this.sauverFichierDansXCTrack("MOST_RECENT_OA.txt");
        this.creerFichierEnLocal(fichier.getContenu(), fichier.getName());
        this.sauverFichierDansXCTrack(fichier.getName());
        this.activity.creationFichierTerminee(fichier);
    }

    @SuppressLint("BinaryOperationInTimber")
    private void sauverFichierDansXCTrack(String filename) {
        File root = new File(this.activity.getFilesDir(), DIR_FICHIERS_TEMP);
        File requestFile = new File(root, filename);
        try {
            Uri fileUri = FileProvider.getUriForFile(
                    this.activity,
                    "air3manager.fileprovider",
                    requestFile);
            activity.grantUriPermission("org.xcontest.XCTrack", fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent intent = new Intent();
            intent.setAction(ACTION_FILE_COPY);
            intent.setDataAndType(fileUri, "text/plain");
            intent.setComponent(new ComponentName("org.xcontest.XCTrack", "org.xcontest.XCTrack.FileCopyReceiver"));
            intent.setFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            activity.sendBroadcast(intent);
            Timber.i("File uri sended. File name : %s", filename);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this.activity, "Problems: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Timber.e(e);
        }
    }

    public void verifyStoragePermissions() {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public static void creerListeFichiers(CommonActivity activity, String reponse) {
        List<Fichier> fichiers = null;
        try {
            String messageErreur = activity.getString(R.string.erreur_internet);
            int indice = 8 + reponse.indexOf("\"files\": [");
            if (indice < 8) throw new Exception(messageErreur);
            String reponseTableFichiers = reponse.substring(indice, reponse.length() - 2);
            if (reponseTableFichiers.contains("[]")) { // aucun fichier partagé/reçu
                // on n'envoie plus de mail de demande d'accès
//                activity.afficherMessageDemandeAccesFichiersPartages();
                activity.afficherMessageErreurAccesFichier();
                return;
            }
            Gson gson = new Gson();
            Fichier[] tableFichiers = gson
                    .fromJson(reponseTableFichiers, Fichier[].class);
            fichiers = Arrays.asList(tableFichiers);
            if (fichiers.isEmpty()) throw new Exception(messageErreur);
            Comparator<Fichier> comparateur = (f1, f2) -> f2.getFormatedName().compareTo(f1.getFormatedName());
            boolean zoneDanger = activity.getUser().getDangerous();
            TreeSet<Fichier> ensembleFichiersTries = new TreeSet<>(comparateur);
            for (Fichier f : fichiers) {
                if (f.getName() != null && f.getName().toUpperCase().contains("OA") && f.getName().length() >= 8) {
                    if (f.isTypeDanger() && zoneDanger)
                        ensembleFichiersTries.add(f);
                    else if (!f.isTypeDanger() && !zoneDanger)
                        ensembleFichiersTries.add(f);
                }
            }
            fichiers = new ArrayList<>(CommonActivity.NB_FICHIERS);
            int i = 0;
            for (Fichier f : ensembleFichiersTries) {
                fichiers.add(i, f);
                i++;
                if (i == CommonActivity.NB_FICHIERS) break;
            }
        } catch (Exception ex) {
            Timber.e("creerListeFichiers : %s", ex.getMessage());
            activity.trtErreurAccesInternet();
        }
        activity.downloadListeFichiersTerminee(fichiers);
    }

    public void sauverFichiers() {
        FileOutputStream fos;
        try {
            fos = this.activity.openFileOutput(FICHIERS, MODE_PRIVATE);
            ObjectOutputStream outputStream = new ObjectOutputStream(fos);
            outputStream.writeObject(this.activity.getFichiers());
            outputStream.flush();
            outputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void chargerFichiers() {
        FileInputStream fis;
        try {
            fis = this.activity.openFileInput(FICHIERS);
            ObjectInputStream inputStream = new ObjectInputStream(fis);
            List<Fichier> fichiers = (List<Fichier>) inputStream.readObject();
            this.activity.setFichiers(fichiers);
            fis.close();
        } catch (Exception ex) {
            sauverFichiers();  // il va sauver la liste vide
        }
    }

    public static Configuration lireConfiguration(Activity activity) {
        Configuration configuration = new Configuration();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(activity.getAssets().open(FICHIER_CONFIGURATION)))) {
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                sb.append(mLine);
            }
            JSONObject jsonObject = new JSONObject(sb.toString());
            String version = jsonObject.get("version").toString();
            String reset = jsonObject.get("reset").toString();
            List<ItemInterface> modes = construireListe(jsonObject, "modes");
            List<ItemInterface> themes = construireListe(jsonObject, "themes");
            configuration.setVersion(version);
            configuration.setReset(reset);
            configuration.setModes(modes);
            configuration.setThemes(themes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return configuration;
    }

    private static List<ItemInterface> construireListe(JSONObject jsonFichier, String clef) throws
            JSONException {
        JSONArray jsonArray = jsonFichier.getJSONArray(clef);
        List<ItemInterface> liste = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jo = (JSONObject) jsonArray.get(i);
            String id = jo.get("id").toString();
            String libelle = jo.get("libelle").toString();
            String xcbs = jo.get("xcbs").toString();
            ItemInterface item = new ItemInterface(id, libelle, xcbs);
            liste.add(i, item);
        }
        return liste;
    }
}

