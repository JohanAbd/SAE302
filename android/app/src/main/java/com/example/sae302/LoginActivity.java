package com.example.sae302;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText user = findViewById(R.id.editUser);
        EditText pass = findViewById(R.id.editPass);
        Button btn = findViewById(R.id.btnLogin);

        btn.setOnClickListener(v -> {
            // Test simple (remplace par une requête API si nécessaire)
            if(user.getText().toString().equals("admin") && pass.getText().toString().equals("admin123")) {
                startActivity(new Intent(this, MainActivity.class));
                finish(); // Empêche de revenir au login avec le bouton retour
            } else {
                Toast.makeText(this, "Identifiants incorrects", Toast.LENGTH_SHORT).show();
            }
        });
    }
}