package com.example.haynews.api;

import com.example.haynews.NewsItem;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import java.util.List;

public interface NewsApiService {
    @GET("v2/top-headlines")
    Call<NewsApiResponse> getTopHeadlines(
            @Query("apiKey") String apiKey,
            @Query("country") String country,
            @Query("category") String category,
            @Query("pageSize") int pageSize
    );

    @GET("v2/everything")
    Call<NewsApiResponse> searchNews(
            @Query("apiKey") String apiKey,
            @Query("q") String query,
            @Query("sortBy") String sortBy,
            @Query("pageSize") int pageSize,
            @Query("language") String language
    );

    @GET("v2/everything")
    Call<NewsApiResponse> getNewsByCategory(
            @Query("apiKey") String apiKey,
            @Query("q") String category,
            @Query("sortBy") String sortBy,
            @Query("pageSize") int pageSize
    );
}

