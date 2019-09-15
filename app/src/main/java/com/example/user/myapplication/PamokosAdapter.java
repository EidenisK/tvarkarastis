package com.example.user.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.EventLog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.List;

public class PamokosAdapter extends RecyclerView.Adapter<PamokosAdapter.MyViewHolder> {

    private List<Pamoka> pamokosList;
    Context myContext;
    SharedPreferences mPrefs;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView pavadinimas, laikas, /*info,*/ numeris;

        /**Randame teksto laukelius*/
        public MyViewHolder(View view, Context context) {
            super(view);
            pavadinimas = (TextView) view.findViewById(R.id.pavadinimas);
            laikas = (TextView) view.findViewById(R.id.laikas);
            //info = (TextView) view.findViewById(R.id.mokytojas);
            numeris = (TextView) view.findViewById(R.id.numeris);
        }
    }

    public PamokosAdapter(List<Pamoka> pamokosList, SharedPreferences SP, Context context) {
        this.pamokosList = pamokosList;
        mPrefs = SP;
        myContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pamokos_list_row, parent, false);
        return new MyViewHolder(itemView, myContext);
    }

    /**pamokos kintamuosius irasome i atitinkamus teksto laukelius*/
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Pamoka pamoka = pamokosList.get(position);
        if(pamoka.getPavadinimas().equals("")) { //JEIGU LANGAS
            //holder.laikas.setText("");
            if(mPrefs.getBoolean("rodytiLangoLaika", false)) {
                holder.laikas.setText(pamoka.getLaikas());
                holder.laikas.setGravity(Gravity.CENTER);
                holder.pavadinimas.setText("");
            } else {
                holder.laikas.setText("");
                holder.pavadinimas.setText("///");
            }

            //holder.pavadinimas.setText("");
            //holder.info.setText("///");

            holder.numeris.setText("");

            //holder.info.setGravity(Gravity.CENTER);
            holder.pavadinimas.setGravity(Gravity.CENTER);

            holder.itemView.setOnClickListener(null);
            holder.itemView.setOnLongClickListener(null);
        } else {
            holder.pavadinimas.setText(pamoka.getPavadinimas());
            //holder.info.setText(pamoka.getMokytojai());
            holder.laikas.setText(pamoka.getLaikas());
            holder.laikas.setGravity(Gravity.LEFT);
            holder.numeris.setText(Integer.toString(pamoka.getNumeris()));

            //holder.info.setGravity(Gravity.START);
            holder.pavadinimas.setGravity(Gravity.START);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent tinklalapioIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pamoka.nuoroda()));
                    tinklalapioIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    myContext.startActivity(tinklalapioIntent);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    try {
                        Calendar beginTime = Calendar.getInstance();
                        beginTime.set(Calendar.HOUR_OF_DAY, pamoka.int_laikas[0][0]);
                        beginTime.set(Calendar.MINUTE, pamoka.int_laikas[0][1]);
                        beginTime.set(Calendar.SECOND, 0);
                        while (beginTime.get(Calendar.DAY_OF_WEEK) != pamoka.sav_diena + Calendar.MONDAY)
                            beginTime.add(Calendar.DAY_OF_MONTH, 1);

                        Calendar endTime = (Calendar) beginTime.clone();
                        endTime.set(Calendar.HOUR_OF_DAY, pamoka.int_laikas[1][0]);
                        endTime.set(Calendar.MINUTE, pamoka.int_laikas[1][1]);

                        Intent intent = new Intent(Intent.ACTION_INSERT)
                                .setData(CalendarContract.Events.CONTENT_URI)
                                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                                .putExtra(CalendarContract.Events.TITLE, pamoka.pavadinimas);

                        if (intent.resolveActivity(myContext.getPackageManager()) != null)
                            myContext.startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(myContext, "Klaida atidarant kalendoriaus programėlę", Toast.LENGTH_LONG).show();
                    }
                    return false;
                }
            });
        }
        holder.itemView.setBackgroundColor(selectBackgroundColor(position));
    }

    private int selectBackgroundColor(int position) {

        Tvarkarastis tvarkarastis = Funkcijos.getTvarkarastis(mPrefs);
        int pasirinktaDiena = mPrefs.getInt("pasirinktaDiena", -1);

        Calendar checkCal1 = Calendar.getInstance(); //Dabartinės pamokos pabaiga
        checkCal1.set(Calendar.HOUR_OF_DAY, tvarkarastis.pamokos[0][position].int_laikas[1][0]);
        checkCal1.set(Calendar.MINUTE, tvarkarastis.pamokos[0][position].int_laikas[1][1]);

        /*checkCal1.set(Calendar.HOUR_OF_DAY, tvarkarastis.intLaikas[position *2 +1][0]);
        checkCal1.set(Calendar.MINUTE, tvarkarastis.intLaikas[position *2 +1][1]);*/
        checkCal1.set(Calendar.SECOND, 0);

        Calendar checkCal2 = Calendar.getInstance();
        if(position == 0) { //Pirmos pamokos pradžia
            checkCal2.set(Calendar.HOUR_OF_DAY, tvarkarastis.pamokos[0][0].int_laikas[0][0]);
            checkCal2.set(Calendar.MINUTE, tvarkarastis.pamokos[0][0].int_laikas[0][1]);

            /*checkCal2.set(Calendar.HOUR_OF_DAY, tvarkarastis.intLaikas[position *2][0]);
            checkCal2.set(Calendar.MINUTE, tvarkarastis.intLaikas[position *2][1]);*/
        } else { //Buvusios pamokos pabaiga
            checkCal2.set(Calendar.HOUR_OF_DAY, tvarkarastis.pamokos[0][position -1].int_laikas[1][0]);
            checkCal2.set(Calendar.MINUTE, tvarkarastis.pamokos[0][position -1].int_laikas[1][1]);

            /*checkCal2.set(Calendar.HOUR_OF_DAY, tvarkarastis.intLaikas[position *2 -1][0]);
            checkCal2.set(Calendar.MINUTE, tvarkarastis.intLaikas[position *2 -1][1]);*/
        }
        checkCal2.set(Calendar.SECOND, 0);

        Calendar current = Calendar.getInstance();

        if(pasirinktaDiena == current.get(Calendar.DAY_OF_WEEK) -2 && checkCal1.after(current) && !checkCal2.after(current))
            return (!mPrefs.getBoolean("darkTheme", false)) ? ContextCompat.getColor(myContext, R.color.grayBackgroundColor) : ContextCompat.getColor(myContext, R.color.grayBackgroundColor1);
        else
            return !mPrefs.getBoolean("darkTheme", false) ? ContextCompat.getColor(myContext, R.color.backgroundColor) : ContextCompat.getColor(myContext, R.color.backgroundColor1);
    }

    @Override
    public int getItemCount() {
        return pamokosList.size();
    }
}

