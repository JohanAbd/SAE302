package com.example.sae302;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class FailleAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Faille> list;

    public FailleAdapter(Context context, ArrayList<Faille> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() { return list.size(); }
    @Override
    public Object getItem(int i) { return list.get(i); }
    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_faille, parent, false);
        }

        Faille faille = list.get(position);

        // Liaison avec les IDs de votre fichier item_faille.xml
        TextView textTarget = convertView.findViewById(R.id.textTarget);
        TextView textSeverity = convertView.findViewById(R.id.textSeverity);
        TextView textDate = convertView.findViewById(R.id.textDate);
        View indicator = convertView.findViewById(R.id.severityIndicator);

        // Affichage des textes
        textTarget.setText(faille.getIp() + ":" + faille.getPort());
        textSeverity.setText(faille.getSeverity());
        textDate.setText(faille.getScanDate());

        // Gestion dynamique de la couleur selon la sévérité
        int color;
        if (faille.getSeverity().equals("Critical")) color = Color.parseColor("#721c24");
        else if (faille.getSeverity().equals("High")) color = Color.parseColor("#ef4444");
        else color = Color.parseColor("#10b981");

        indicator.setBackgroundColor(color);
        textSeverity.setTextColor(color);

        return convertView;
    }
}