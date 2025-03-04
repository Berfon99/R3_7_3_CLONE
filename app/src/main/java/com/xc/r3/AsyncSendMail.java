package com.xc.r3;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

class AsyncSendMail extends AsyncTask<Object, Void, Object[]> {

    @SuppressLint("StaticFieldLeak")
    private CommonActivity activity;

    @Override
    protected Object[] doInBackground(Object... params) {
        activity = (CommonActivity) params[0];
        String subject = (String) params[1];
        String message = (String) params[2];
        Object[] resultats = new Object[1];
        try {
            resultats[0] = true;
            GMailSender sender = new GMailSender("gigonali98@gmail.com", "ceciestlemdpde981");
            sender.sendMail(subject, message, "gigonali98@gmail.com", "info@fly-air3.com");
        } catch (Exception e) {
            resultats[0] = false;
        }
        return resultats;
    }

    @Override
    protected void onPostExecute(Object[] params) {
        if (this.activity instanceof MainActivity) {
            ((MainActivity) this.activity).retablirAffichageWidgets();
        }
        boolean operationReussie = (boolean) params[0];
        activity.afficherMessageMailEnvoye(operationReussie);
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }

}
