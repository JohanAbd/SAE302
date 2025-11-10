package com.example.app;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @GET("api/index.php")
    Call<List<Vulnerability>> getVulnerabilities();


    @Headers("Content-Type: application/json")
    @POST("api/index.php")
    Call<PostResponse> upsertVulnerability(@Body Vulnerability body);
}