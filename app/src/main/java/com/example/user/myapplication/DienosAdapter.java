package com.example.user.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class DienosAdapter extends RecyclerView.Adapter<DienosAdapter.MyViewHolder> {

    private List<String> dienosList;
    private SharedPreferences mPrefs;
    private Context myContext;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView pavadinimas;

        /**Randame teksto laukelius*/
        public MyViewHolder(final View view) {
            super(view);
            pavadinimas = (TextView) view.findViewById(R.id.pavadinimas);
            view.setBackgroundColor(!mPrefs.getBoolean("darkTheme", false) ? ContextCompat.getColor(myContext, R.color.defaultItemColor) : ContextCompat.getColor(myContext, R.color.defaultItemColor1));
        }
    }

    public DienosAdapter(List<String> dienosList, SharedPreferences SP, Context C) {
        this.dienosList = dienosList;
        mPrefs = SP;
        myContext = C;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dienos_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    /**pamokos kintamuosius irasome i atitinkamus teksto laukelius*/
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String diena = dienosList.get(position);
        holder.pavadinimas.setText(diena);
    }

    @Override
    public int getItemCount() {
        return dienosList.size();
    }
}

