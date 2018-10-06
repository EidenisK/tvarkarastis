package com.example.user.myapplication;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class MokiniaiAdapter extends RecyclerView.Adapter<MokiniaiAdapter.MyViewHolder> {

    private List<Mokinys> mokiniaiList;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView title;
        public RelativeLayout main_layout;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.vardas);
            main_layout = view.findViewById(R.id.mokinys_main_layout);
        }
    }

    public MokiniaiAdapter(List<Mokinys> mokiniaiList) {
        this.mokiniaiList = mokiniaiList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mokiniai_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        Mokinys mokinys = mokiniaiList.get(position);
        holder.title.setText(mokinys.getPazymetas() ? "★" + mokinys.getVardas() : mokinys.getVardas());

        /*holder.title.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return mokiniaiList.size();
    }
}

