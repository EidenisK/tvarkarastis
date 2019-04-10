package com.example.user.myapplication;

import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private List<Pamoka> pamokos = new ArrayList<>();
    private Context mContext;
    private SharedPreferences mPrefs;
    private int colorMode;

    public ListRemoteViewsFactory(Context context, int cMode) {
        mContext = context;
        colorMode = cMode;
    }

    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        mPrefs = mContext.getSharedPreferences("label", 0);
        getLessonList();
    }

    public void onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
        pamokos.clear();
    }

    public int getCount() {
        return pamokos.size();
    }

    private void getLessonList() {
        pamokos.clear();
        Tvarkarastis tvarkarastis = Funkcijos.getTvarkarastis(mPrefs);
        int diena = Funkcijos.parinktiDiena(mPrefs, true);
        List<Pamoka> dienosPamokos = Arrays.asList(tvarkarastis.pamokos[diena]);

        int endIdx = dienosPamokos.size()-1;
        while(endIdx >= 0 && dienosPamokos.get(endIdx).getPavadinimas().equals(""))
            endIdx--;
        if(endIdx != -1) pamokos.addAll(dienosPamokos.subList(0, endIdx+1));
    }

    public RemoteViews getViewAt(int position) {
        // position will always range from 0 to getCount() - 1.

        // We construct a remote views item based on our widget item xml file, and set the
        // text based on the position;
        RemoteViews rv;
        if(colorMode == 1) {
            rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item_dark);
        } else {
            rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
        }
        Pamoka pamoka = pamokos.get(position);

        String pavadinimas = pamoka.pavadinimas;
        if(pavadinimas.equals("")) {
            rv.setViewVisibility(R.id.pavadinimas, View.GONE);
            rv.setViewVisibility(R.id.pavadinimas2, View.VISIBLE);


            //rv.setTextViewText(R.id.mokytojas, "");
            if(mPrefs.getBoolean("rodytiLangoLaika", false)) {
                rv.setTextViewText(R.id.laikas, pamoka.getLaikas());
                rv.setTextViewText(R.id.pavadinimas2, "");
            } else {
                rv.setTextViewText(R.id.laikas, "");
                rv.setTextViewText(R.id.pavadinimas2, "");
            }
            rv.setTextViewText(R.id.numeris, "");
        } else {
            rv.setViewVisibility(R.id.pavadinimas2, View.GONE);
            rv.setViewVisibility(R.id.pavadinimas, View.VISIBLE);
            rv.setTextViewText(R.id.pavadinimas, pavadinimas);

            //rv.setTextViewText(R.id.mokytojas, pamoka.getMokytojai());
            rv.setTextViewText(R.id.laikas, pamoka.getLaikas());
            rv.setTextViewText(R.id.numeris, Integer.toString(pamoka.getNumeris()));
        }

        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in StackWidgetProvider.
        Bundle extras = new Bundle();
        extras.putInt(MyWidgetProvider.EXTRA_ITEM, position);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.pavadinimas, fillInIntent);
        rv.setOnClickFillInIntent(R.id.pavadinimas2, fillInIntent);
        rv.setOnClickFillInIntent(R.id.laikas, fillInIntent);
        //rv.setOnClickFillInIntent(R.id.mokytojas, fillInIntent);
        rv.setOnClickFillInIntent(R.id.numeris, fillInIntent);

        // You can do heaving lifting in here, synchronously. For example, if you need to
        // process an image, fetch something from the network, etc., it is ok to do it here,
        // synchronously. A loading view will show up in lieu of the actual contents in the
        // interim.
        // Return the remote views object.
        return rv;
    }

    public RemoteViews getLoadingView() {
        // You can create a custom loading view (for instance when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        getLessonList();
        // This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
        // on the collection view corresponding to this factory. You can do heaving lifting in
        // here, synchronously. For example, if you need to process an image, fetch something
        // from the network, etc., it is ok to do it here, synchronously. The widget will remain
        // in its current state while work is being done here, so you don't need to worry about
        // locking up the widget.
    }
}