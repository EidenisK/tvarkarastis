package com.example.user.myapplication;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

public class MyNewIntentService extends IntentService{
    private static final int NOTIFICATION_ID = 3;
    String prev_string = "NULL";

    public MyNewIntentService() {
        super("MyNewIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String task = "NONE";

        Bundle b = intent.getExtras();
        if(b != null)
            task = b.getString("task", "NONE");

        switch (task) {
            case "show":
                displayNotification(b);
                break;
            case "update":
                Funkcijos.nustatytiPriminimus(getSharedPreferences("label", 0), getApplicationContext());
                break;
            case "remove":
                Funkcijos.removeNotification(this, NOTIFICATION_ID);
                break;
            case "download":
                downloadString();
                break;
            default:
                Toast.makeText(getApplicationContext(), R.string.nepavyko_gauti_uzduoties, Toast.LENGTH_LONG).show();
                break;
        }
    }

    void displayNotification(Bundle b) {
        String title = b.getString("title", "ERROR");
        SharedPreferences mPrefs = getSharedPreferences("label", 0);

        String info = "ERROR";
        int informacija = mPrefs.getInt("informacija", 0);
        switch (informacija) {
            case 0: info = b.getString("details", "ERROR"); break;
            case 1: info = b.getString("pabaiga", "ERROR"); break;
        }

        Funkcijos.displayNotification(this, title, info,
                mPrefs.getBoolean("vibracija", true),
                mPrefs.getBoolean("panaikinimai", false));
    }

    BroadcastReceiver message = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences mPrefs = getSharedPreferences("label", 0);
            String curr_string = mPrefs.getString("pamokos", "NULL");
            if(!curr_string.equals(prev_string))
                Funkcijos.displayNotification(context, "Tvarkaraštis", "Pasikeitė pamokų tvarkaraštis",
                        mPrefs.getBoolean("vibracija", true), true);
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(message);
        }
    };

    void downloadString() {
        SharedPreferences mPrefs = getSharedPreferences("label", 0);
        prev_string = mPrefs.getString("pamokos", "NULL");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(message, new IntentFilter("lesson_download_finished"));
        Funkcijos.gautiInformacija(getApplicationContext(), mPrefs, "pamokos");
    }
}
