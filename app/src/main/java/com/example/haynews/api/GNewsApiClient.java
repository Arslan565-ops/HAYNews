package com.example.haynews.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class GNewsApiClient {
    private static final String BASE_URL = "https://gnews.io/api/";
    private static GNewsApiClient instance;
    private GNewsApiService apiService;

    private GNewsApiClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(GNewsApiService.class);
    }

    public static GNewsApiClient getInstance() {
        if (instance == null) {
            instance = new GNewsApiClient();
        }
        return instance;
    }

    public GNewsApiService getApiService() {
        return apiService;
    }
}

