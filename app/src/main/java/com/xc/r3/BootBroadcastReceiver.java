package com.xc.r3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.i("BootBroadcastReceiver.onReceive() called");
        Timber.i("OnReceive..." + intent.getAction());
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Util.lancerMainActivity(context, true);
        }
    }
}