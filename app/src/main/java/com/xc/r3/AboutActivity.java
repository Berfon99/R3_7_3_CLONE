package com.xc.r3;

import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends CommonActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Utilisation de BuildConfig.VERSION_NAME pour récupérer la version
        String version = getString(R.string.version) + " : " + BuildConfig.VERSION_NAME;

        String developpeur = getString(R.string.developpe_par) + " : O. Legrand";
        String web = getString(R.string.web) + " : " + "www.fly-air3.com";
        String message = version + "\n" + web + "\n" + developpeur;

        TextView textViewAbout = findViewById(R.id.textViewAbout);
        textViewAbout.setText(message);
    }

    @Override
    protected void finDemandeAccessFichiers() {
        // Aucune action spécifique nécessaire dans cette activité
    }
}