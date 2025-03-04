package com.xc.r3;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class ListActivity extends CommonActivity {
    public static final int PAS_ACCES_INTERNET = 99;
    private static final int DEMANDE_ACCES_FICHIERS = 98;

    private String nomFichierSelectionne;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_bar);
        downloadListeFichiersOpenAir();
    }

    public void initialiserListView() {
        this.setContentView(R.layout.activity_list);
        ActionBar actionBar = getSupportActionBar();  //Make sure you are extending ActionBarActivity
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        RecyclerView mRecyclerView = findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)

        String[] tabNomsFichiers = this.getTabNomsFichiers();
        RecyclerView.Adapter mAdapter = new FichierAdapter(tabNomsFichiers);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();    //Call the back button's method
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void fichierSelectionne(View view) {
        Timber.i( "fichier sélectionné");
        String message = getString(R.string.sauver_fichier);
        this.nomFichierSelectionne = ((TextView) view).getText().toString();
        String titre = this.getString(R.string.fichier) + " :" + nomFichierSelectionne;
        afficherMessage(message, titre, ICONE_APPLICATION);
    }

    @Override
    public void downloadListeFichiersTerminee(List<Fichier> fichiers) {
        super.downloadListeFichiersTerminee(fichiers);
        if (fichiers==null || fichiers.isEmpty()) {
            setResult(ListActivity.PAS_ACCES_INTERNET);
            finish();
        } else {
            initialiserListView();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void afficherMessage(String message, String titre, int typeIcone) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_MinWidth);
        alert.setTitle(titre);
        alert.setMessage("\n" + message);
        alert.setPositiveButton(R.string.envoyer, (dialog, id) -> ListActivity.this.downloaderFichierSelectionne());
        alert.setNegativeButton(R.string.annuler, (dialog, which) -> dialog.dismiss());
        if (typeIcone == ICONE_APPLICATION) alert.setIcon(getDrawable(R.drawable.air3manager));
        else if (typeIcone == ICONE_ATTENTION) alert.setIcon(getDrawable(R.drawable.ic_warning));
        alert.show();
    }

    @Override
    public void finDemandeAccessFichiers() {
        setResult(ListActivity.DEMANDE_ACCES_FICHIERS);
        finish();
    }

    private void downloaderFichierSelectionne() {
        Timber.i( "downloader fichier : %s", this.nomFichierSelectionne);
        this.setContentView(R.layout.activity_progress_bar);
        for (Fichier f : this.fichiers) {
            if (this.nomFichierSelectionne.contains(f.getName())) {
                lancerRequete(AsyncCallWS.DOWNLOADER, f);
                Timber.i("requete downloader fichier ");
                break;
            }
        }
    }

    public void creationFichierTerminee(Fichier fichier) {
        initialiserListView();
        super.creationFichierTerminee(fichier);
    }

    @Override
    public void downloadFichierTermine(Fichier fichier, String reponse) {
        initialiserListView();
        super.downloadFichierTermine(fichier, reponse);
    }
}
