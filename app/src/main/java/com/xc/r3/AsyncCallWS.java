package com.xc.r3;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import com.xc.r3.Util;

public class AsyncCallWS extends AsyncTask<Object, Void, Object[]> {

    public static final int LISTER = 0;
    public static final int DOWNLOADER = 1;
    @SuppressLint("StaticFieldLeak")
    private CommonActivity activity;
    private Fichier fichier;

    @Override
    protected Object[] doInBackground(Object... params) {
        String reponse = "?";
        activity = (CommonActivity) params[0];
        Integer operation = (Integer) params[1];
        String token = (String) params[2];
        WebServiceRest ws = new WebServiceRest(token);
        switch (operation) {
            case LISTER:
                reponse = ws.getListeFichiersGson();
                break;
            case DOWNLOADER:
                fichier = (Fichier) params[3];
                reponse = ws.downloader(fichier);
                break;
        }
        Object[] resultats = new Object[2];
        resultats[0] = operation;
        resultats[1] = reponse;
        return resultats;
    }

    @Override
    protected void onPostExecute(Object[] params) {
        if (this.activity instanceof MainActivity) {
            ((MainActivity) this.activity).retablirAffichageWidgets();
        }
        Integer operation = (Integer) params[0];
        String reponse = (String) params[1];
        if (reponse.equals("401")) {
            // unauthorized
            this.activity.relancerRequeteAvecFreshToken(operation);
            return;
        } else if (reponse.length() < 4) {
            String message = this.activity.getString(R.string.erreur_requete) + " : " + reponse;
            String titre = this.activity.getString(R.string.erreur);
            Util.afficherMessage(this.activity, message, titre, CommonActivity.ICONE_ATTENTION); // Modified line
            return;
        }
        switch (operation) {
            case LISTER:
                GestionFichiers.creerListeFichiers(this.activity, reponse);
                break;
            case DOWNLOADER:
                this.fichier.setContenu(reponse);
                this.activity.downloadFichierTermine(fichier, reponse);
                break;
        }
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }

}