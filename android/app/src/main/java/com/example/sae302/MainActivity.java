package com.example.sae302;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<Faille> listeFailles;
    private FailleAdapter adapter; // Utilisation d'un adaptateur personnalisé pour le style
    private RequestQueue queue;

    // REMPLACEZ PAR L'IP DE VOTRE VM (ex: http://192.168.1.50/api/vulns)
    private static final String URL_API = "http://192.168.1.110/api/vulns";
    // Note : 10.0.2.2 est l'adresse pour accéder à l'hôte depuis l'émulateur Android Studio

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialisation UI
        listView = findViewById(R.id.failleListView);
        Button btnRefresh = findViewById(R.id.btnRefresh);

        listeFailles = new ArrayList<>();
        // On utilise l'adapter personnalisé pour gérer les couleurs de sévérité
        adapter = new FailleAdapter(this, listeFailles);
        listView.setAdapter(adapter);

        queue = Volley.newRequestQueue(this);

        // 2. Événement clic sur le bouton Rafraîchir
        btnRefresh.setOnClickListener(v -> {
            recupererDonnees();
        });

        // 3. Événement clic sur un élément de la liste
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Faille selectionnee = listeFailles.get(position);

            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            // On passe les données à l'activité de détail
            intent.putExtra("titre", selectionnee.getIp() + (selectionnee.getPort().isEmpty() ? "" : ":" + selectionnee.getPort()));
            intent.putExtra("desc", selectionnee.getDescription());
            intent.putExtra("severity", selectionnee.getSeverity());
            intent.putExtra("date", selectionnee.getScanDate());

            startActivity(intent);
        });

        // Chargement initial au lancement
        recupererDonnees();
    }

    /**
     * Se connecte à l'API Java et récupère le JSON
     */
    private void recupererDonnees() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                URL_API,
                null,
                response -> {
                    try {
                        listeFailles.clear();
                        for (int i = 0; i < response.length(); i++) {
                            // Dans la boucle for du onResponse
                            JSONObject obj = response.getJSONObject(i);
                            Faille f = new Faille(
                                    obj.getString("id"),
                                    obj.getString("ip"),
                                    obj.getString("port"),
                                    obj.getString("severity"),
                                    obj.getString("description"),
                                    obj.getString("scan_date")
                            );
                            listeFailles.add(f);
                        }
                        adapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this, "Données actualisées", Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Erreur de lecture JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(MainActivity.this, "Erreur de connexion au serveur", Toast.LENGTH_LONG).show();
                }
        );

        queue.add(jsonArrayRequest);
    }
}