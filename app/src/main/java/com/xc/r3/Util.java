package com.xc.r3;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import timber.log.Timber;

public class Util {
    public static final String BOOT = "boot";
    public static final String NOTIFICATION_DATE_FICHIER = "notification date fichier";
    public static final String NOTIFICATION_ACCES_INTERNET = "notification accès internet";
    private static final int DELAY = 10 * 1000; // délai de 10 secondes avant de lancer
    private static final String TAG = "Util";

    public static void lancerXCTrack(Activity activity) {
        Timber.i("Lancer XCTrack...");
        Intent launchIntent = activity.getPackageManager().getLaunchIntentForPackage("org.xcontest.XCTrack");
        if (launchIntent != null) {
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Timber.i("Lancement de XCTrack...");
            activity.startActivity(launchIntent);
        } else {
            Timber.i("Problème de nom de package avec XCTrack..");
        }
        activity.getIntent().putExtra(BOOT, false);
        activity.finish();
    }

    public static void lancerXCGuide(Activity activity) {
        Timber.i("Lancer XC Guide...");
        Intent launchIntent = activity.getPackageManager().getLaunchIntentForPackage("indysoft.xc_guide");
        if (launchIntent != null) {
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Timber.i("Lancement de XC Guide...");
            activity.startActivity(launchIntent);
        } else {
            Timber.i("Problème de nom de package avec XC Guide..");
        }
    }

    public static void lancerMainActivity(Context context, boolean boot) {
        Timber.i("Lancement MainActivity au boot ...");
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Util.BOOT, boot);
        int mPendingIntentId = 223344;
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_MUTABLE
            );
        } else {
            pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        }

        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long delay = getDelay(context);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + delay, pendingIntent);
    }

    private static long getDelay(Context context) {
        User user = User.getInstance(context);
        if (user.delayXCTrackOnBoot()) return DELAY;
        return 0;
    }

    public static void afficherMessage(Context context, String message, String titre, int typeIcone) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "afficherMessage: Runnable is running");
                if (context instanceof AppCompatActivity) {
                    AppCompatActivity activity = (AppCompatActivity) context;
                    Log.d(TAG, "afficherMessage: isFinishing() = " + activity.isFinishing());
                    Log.d(TAG, "afficherMessage: isDestroyed() = " + activity.isDestroyed());
                    if (!activity.isFinishing() && !activity.isDestroyed()) {
                        Log.d(TAG, "afficherMessage: Showing AlertDialog");
                        AlertDialog.Builder alert = new AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog);
                        alert.setTitle(titre);
                        alert.setMessage("\n" + message);
                        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        alert.show();
                    } else {
                        Log.d(TAG, "afficherMessage: Activity is finishing or destroyed, not showing AlertDialog");
                    }
                } else {
                    Log.e(TAG, "afficherMessage: Context is not an AppCompatActivity");
                }
            }
        });
    }
}