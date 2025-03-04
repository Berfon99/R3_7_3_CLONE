package com.xc.r3;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.*;

import java.util.Objects;

public class PreferencesActivity extends AppCompatActivity {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchXCTrackBoot;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchDownload;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchDelayXCTrackOnBoot;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchDownloadOnBoot;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchDanger;
    private User user;
    private AccountManager accountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        ActionBar actionBar = getSupportActionBar();  //Make sure you are extending ActionBarActivity
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        this.user = User.getInstance(this);
        this.switchXCTrackBoot = findViewById(R.id.switch_xctrack);
        this.switchDelayXCTrackOnBoot = findViewById(R.id.switch_xctrack_delay);
        this.switchDownloadOnBoot = findViewById(R.id.switch_download_on_boot);
        this.switchDownload = findViewById(R.id.switch_download);
        this.switchDanger = findViewById(R.id.switch_danger);
        this.switchXCTrackBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setSwitchDelayXCTrackOnBoot();
            }
        });
        this.switchDownload.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setSwitchesDownload();
            }
        });
        initSwitches();
    }

    private void initSwitches() {
        this.switchXCTrackBoot.setChecked(this.user.lancerXCTrackBoot());
        setSwitchDelayXCTrackOnBoot();
        this.switchDelayXCTrackOnBoot.setChecked(this.user.delayXCTrackOnBoot());
        this.switchDownload.setChecked(this.user.downloadFichierOpenAir());
        setSwitchesDownload();
    }

    private void setSwitchDelayXCTrackOnBoot() {
        if (this.switchXCTrackBoot.isChecked()) {
            this.switchDelayXCTrackOnBoot.setEnabled(true);
            this.switchDelayXCTrackOnBoot.setChecked(this.user.delayXCTrackOnBoot());
        } else {
            this.switchDelayXCTrackOnBoot.setEnabled(false);
            this.switchDelayXCTrackOnBoot.setChecked(false);
        }
    }

    private void setSwitchesDownload() {
        if (this.switchDownload.isChecked()) {
            this.switchDownloadOnBoot.setEnabled(true);
            this.switchDownloadOnBoot.setChecked(this.user.downloadFichierOpenAirOnBoot());
            this.switchDanger.setEnabled(true);
            this.switchDanger.setChecked(this.user.getDangerous());

        } else {
            this.switchDownloadOnBoot.setEnabled(false);
            this.switchDownloadOnBoot.setChecked(false);
            this.switchDanger.setEnabled(false);
            this.switchDanger.setChecked(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sauver();
        finish();
    }

    private void sauver() {
        boolean bootXCtrack = this.switchXCTrackBoot.isChecked();
        boolean delayXCtrackOnBoot = this.switchDelayXCTrackOnBoot.isChecked();
        boolean download = this.switchDownload.isChecked();
        boolean downloadOnBoot = this.switchDownloadOnBoot.isChecked();
        boolean danger = this.switchDanger.isChecked();
        this.user.setPreferencesBoolean(bootXCtrack, delayXCtrackOnBoot, download, downloadOnBoot, danger);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();    //Call the back button's method
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
