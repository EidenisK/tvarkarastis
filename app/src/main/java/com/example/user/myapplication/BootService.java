package com.example.user.myapplication;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootService extends IntentService{
    public BootService() {
        super("BootService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences mPrefs = getSharedPreferences("label", 0);
        if(mPrefs.getBoolean("priminimai", false))
            Funkcijos.nustatytiPriminimus(mPrefs, getApplicationContext());
        if(mPrefs.getBoolean("autoUpdate", true))
            Funkcijos.nustatytiPamokuAtnaujinima(getApplicationContext(), mPrefs);
    }
}
