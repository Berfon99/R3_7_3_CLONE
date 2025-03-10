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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import static android.content.Context.MODE_PRIVATE;
import static com.xc.r3.CommonActivity.FICHIERS;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

public class GestionFichiers {

    private static final String XCBS_PATH = "xcbs/";
    private static final String DIR_FICHIERS_TEMP = "temp";
    private static final String ACTION_FILE_COPY = "air3manager.intent.action.FILE_COPY";
    private static final String THEME_PATH = "theme/";
    public static final String FICHIER_OA = "MOST_RECENT_OA.txt";
    private static final String FICHIER_CLEARPILOT_BIGGER_CITIES = "ClearPilot-AIR3-big-cities.xml"; // Noms des villes en plus grand

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

    public boolean copierFichierClearPilotBiggerCities() {
        boolean copie = true;
        try {
            creerFichierClearPilotBiggerCitiesEnLocal(FICHIER_CLEARPILOT_BIGGER_CITIES);
            sauverFichierDansXCTrack(FICHIER_CLEARPILOT_BIGGER_CITIES);
        } catch (Exception ex) {
            copie = false;
            Timber.e(ex);
            Toast.makeText(activity, "erreur lors de la copie fichier clearpilot-bigger-cities.xml\n", Toast.LENGTH_LONG).show();
        }
        return copie;
    }

    private void creerFichierClearPilotBiggerCitiesEnLocal(String fichierClearpilotBiggerCities) throws IOException {
        InputStream in = null;
        FileOutputStream out = null;
        try {
            in = activity.getAssets().open(THEME_PATH + fichierClearpilotBiggerCities);
            String contenuFichier = CharStreams.toString(new InputStreamReader(
                    in, Charsets.UTF_8));
            creerFichierEnLocal(contenuFichier, fichierClearpilotBiggerCities);
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

    public void sauverFichierOpenair(Fichier fichier) {
        this.creerFichierEnLocal(fichier.getContenu(), "MOST_RECENT_OA.txt");
        this.sauverFichierDansXCTrack("MOST_RECENT_OA.txt");
        this.creerFichierEnLocal(fichier.getContenu(), fichier.getName());
        this.sauverFichierDansXCTrack(fichier.getName());
        this.activity.creationFichierTerminee(fichier);
    }

    @SuppressLint("BinaryOperationInTimber")

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

    @SuppressLint("TimberArgCount")
    public boolean copyBootstrapFile(String modelFolder, String id) {
        boolean copie = true;
        try {
            Timber.tag("GestionFichiers").i("copyBootstrapFile called with modelFolder: %s, id: %s", modelFolder, id);
            String filename = creerFichierXcbsEnLocal(modelFolder, id);
            sauverFichierDansXCTrack(filename);
        } catch (Exception ex) {
            copie = false;
            Timber.tag("GestionFichiers").e(ex, "Error in copyBootstrapFile");
            Toast.makeText(activity, "erreur lors de la copie fichier bootstrap\n", Toast.LENGTH_LONG).show();
        }
        return copie;
    }

    private String creerFichierXcbsEnLocal(String modelFolder, String id) throws Exception {
        InputStream in = null;
        FileOutputStream out = null;
        String nomFichier = "";
        try {
            Timber.tag("GestionFichiers").i("creerFichierXcbsEnLocal called with modelFolder: %s, id: %s", modelFolder, id);
            String[] files = activity.getAssets().list(XCBS_PATH + modelFolder + "/" + id);
            if (files != null && files.length > 0) {
                nomFichier = files[0];
                Timber.tag("GestionFichiers").i("File found in assets: %s", nomFichier);
            } else {
                Timber.tag("GestionFichiers").e("No file found in assets for modelFolder: %s, id: %s", modelFolder, id);
                throw new Exception("No file found in assets");
            }
            in = activity.getAssets().open(XCBS_PATH + modelFolder + "/" + id + "/" + nomFichier);
            Timber.tag("GestionFichiers").i("File opened from assets: %s", nomFichier);
            String contenuFichier = CharStreams.toString(new InputStreamReader(
                    in, Charsets.UTF_8));
            Timber.tag("GestionFichiers").i("File content read successfully: %s", nomFichier);
            creerFichierEnLocal(contenuFichier, nomFichier);
            Timber.tag("GestionFichiers").i("creerFichierEnLocal called with filename: %s", nomFichier);
            return nomFichier;
        } catch (Exception e) {
            Timber.tag("GestionFichiers").e(e, "Error in creerFichierXcbsEnLocal");
            throw e;
        } finally {
            if (out != null) out.close();
            if (in != null) in.close();
        }
    }
    private void creerFichierEnLocal(String contenuFichier, String filename) {
        try {
            Timber.tag("GestionFichiers").i("creerFichierEnLocal called with filename: %s", filename);
            File root = new File(this.activity.getApplicationContext().getFilesDir(), DIR_FICHIERS_TEMP);
            File file = new File(root, filename);
            FileWriter writer = new FileWriter(file);
            writer.write(contenuFichier);
            writer.flush();
            writer.close();
            Timber.tag("GestionFichiers").i("File created locally: %s", filename);
        } catch (IOException e) {
            Toast.makeText(this.activity.getApplicationContext(), "Problems: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Timber.tag("GestionFichiers").e(e, "Error in creerFichierEnLocal");
        }
    }
    @SuppressLint("BinaryOperationInTimber")
    private void sauverFichierDansXCTrack(String filename) {
        File root = new File(this.activity.getFilesDir(), DIR_FICHIERS_TEMP);
        File requestFile = new File(root, filename);
        Timber.tag("GestionFichiers").i("sauverFichierDansXCTrack called with filename: %s", filename);
        if (!requestFile.exists()) {
            Timber.tag("GestionFichiers").e("File does not exist: %s", requestFile.getAbsolutePath());
            return;
        }
        Timber.tag("GestionFichiers").i("File to copy: %s", requestFile.getAbsolutePath());
        try {
            Uri fileUri = FileProvider.getUriForFile(
                    this.activity,
                    "air3manager.fileprovider",
                    requestFile);
            Timber.tag("GestionFichiers").i("File URI generated: %s", fileUri.toString());
            activity.grantUriPermission("org.xcontest.XCTrack", fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent intent = new Intent();
            intent.setAction(ACTION_FILE_COPY);
            intent.setDataAndType(fileUri, "text/plain");
            intent.setComponent(new ComponentName("org.xcontest.XCTrack", "org.xcontest.XCTrack.FileCopyReceiver"));
            intent.setFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            Timber.tag("GestionFichiers").i("Intent created: %s", intent.toString());
            activity.sendBroadcast(intent);
            Timber.tag("GestionFichiers").i("File uri sended. File name : %s", filename);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this.activity, "Problems: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Timber.tag("GestionFichiers").e(e, "Error in sauverFichierDansXCTrack");
        }
    }
    private static ModelConfiguration construireModel(JSONObject jsonModel) throws JSONException {
        ModelConfiguration modelConfiguration = new ModelConfiguration();
        List<ItemInterface> modes = construireListe(jsonModel, "modes");
        modelConfiguration.setModes(modes);
        String folder = jsonModel.getString("folder");
        modelConfiguration.setFolder(folder);
        return modelConfiguration;
    }
    private static List<ItemInterface> construireListe(JSONObject jsonFichier, String clef) throws
            JSONException {
        JSONArray jsonArray = jsonFichier.getJSONArray(clef);
        List<ItemInterface> liste = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jo = (JSONObject) jsonArray.get(i);
            String id = jo.getString("id");
            String libelle = jo.getString("libelle");
            ItemInterface item = new ItemInterface(id, libelle);
            liste.add(item);
        }
        return liste;
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
            Map<String, ModelConfiguration> models = new HashMap<>();
            DataStorageManager dataStorageManager = DataStorageManager.getInstance(activity);
            for (String modelName : dataStorageManager.getAllowedModels()) {
                if (jsonObject.has(modelName)) {
                    JSONObject jsonModel = jsonObject.getJSONObject(modelName);
                    ModelConfiguration modelConfiguration = construireModel(jsonModel);
                    models.put(modelName, modelConfiguration);
                }
            }
            configuration.setModels(models);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return configuration;
    }

}

