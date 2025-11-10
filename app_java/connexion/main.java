package com.example.app;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
package com.example.app;


public class MainActivity extends AppCompatActivity {

    private TextView tv;
    private ApiService api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.tv);

        api = ApiClient.get("http://10.0.2.2/");

        api.getVulnerabilities().enqueue(new Callback<List<Vulnerability>>() {
            @Override public void onResponse(Call<List<Vulnerability>> call, Response<List<Vulnerability>> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    tv.setText("Erreur GET: " + resp.code());
                    return;
                }
                StringBuilder sb = new StringBuilder("Liste (GET):\n");
                for (Vulnerability v : resp.body()) {
                    sb.append("- ")
                      .append(v.id != null ? v.id.substring(0, Math.min(8, v.id.length())) : "no-id")
                      .append(" | ").append(v.description)
                      .append(" | sev=").append(v.severity)
                      .append(" | ").append(v.ip != null ? v.ip : "no-ip")
                      .append(":").append(v.port != null ? v.port : 0)
                      .append("\n");
                }
                tv.setText(sb.toString());
            }
            @Override public void onFailure(Call<List<Vulnerability>> call, Throwable t) {
                tv.setText("Échec GET: " + t.getMessage());
            }
        });

        Vulnerability body = new Vulnerability();
        body.description = "Exposition HTTP";
        body.severity = "HIGH";
        body.ip = "192.168.1.10";
        body.port = 80;

        api.upsertVulnerability(body).enqueue(new Callback<PostResponse>() {
            @Override public void onResponse(Call<PostResponse> call, Response<PostResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null && "OK".equals(resp.body().status)) {
                    Vulnerability rec = resp.body().record;
                    Log.i("API", "POST OK, id = " + (rec != null ? rec.id : "null"));
                } else {
                    Log.w("API", "POST status=" + resp.code());
                }
            }
            @Override public void onFailure(Call<PostResponse> call, Throwable t) {
                Log.e("API", "Échec POST: " + t.getMessage());
            }
        });
    }
}