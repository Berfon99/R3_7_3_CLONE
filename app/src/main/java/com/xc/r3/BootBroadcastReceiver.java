package com.xc.r3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;

public class BootBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.i("OnReceive..." + intent.getAction());
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            User user = User.getInstance(context);
            if (user.lancerXCGuideBoot()) {
                Util.lancerMainActivity(context, true);
            }
        }
    }
}