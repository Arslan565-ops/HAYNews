package com.example.haynews.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class NewsApiClient {
    private static final String BASE_URL = "https://newsapi.org/";
    private static NewsApiClient instance;
    private NewsApiService apiService;

    private NewsApiClient() {
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

        apiService = retrofit.create(NewsApiService.class);
    }

    public static NewsApiClient getInstance() {
        if (instance == null) {
            instance = new NewsApiClient();
        }
        return instance;
    }

    public NewsApiService getApiService() {
        return apiService;
    }
}

