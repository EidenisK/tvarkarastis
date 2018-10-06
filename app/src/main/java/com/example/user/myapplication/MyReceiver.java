package com.example.user.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver{
    public MyReceiver() { }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, MyNewIntentService.class);

        Bundle b = intent.getExtras();
        if(b != null) {
            intent1.putExtra("title", b.getString("title"));
            intent1.putExtra("details", b.getString("details"));
            intent1.putExtra("task", b.getString("task"));
            intent1.putExtra("pabaiga", b.getString("pabaiga"));
            intent1.putExtra("pabH", b.getString("pabH"));
            intent1.putExtra("pabM", b.getString("pabM"));
        }
        context.startService(intent1);
    }
}
