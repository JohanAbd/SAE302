package com.example.sae302;

import android.os.Bundle;
import android.widget.Button; // <--- Import du bouton
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Récupération des textes
        String titre = getIntent().getStringExtra("titre");
        String description = getIntent().getStringExtra("desc");

        TextView tvTitre = findViewById(R.id.textViewTitre);
        TextView tvDesc = findViewById(R.id.textViewDesc);

        // Configuration du bouton Retour
        Button btnRetour = findViewById(R.id.btnRetour);
        btnRetour.setOnClickListener(v -> {
            finish(); // Ferme l'activité et revient en arrière
        });

        tvTitre.setText(titre);
        tvDesc.setText(description);
    }
}