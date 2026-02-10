package com.example.haynews.api;

import com.google.gson.annotations.SerializedName;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import java.util.List;

public interface GNewsApiService {
    @GET("v4/top-headlines")
    Call<GNewsResponse> getTopHeadlines(
            @Query("apikey") String apiKey,
            @Query("country") String country,
            @Query("category") String category,
            @Query("max") int max
    );

    @GET("v4/search")
    Call<GNewsResponse> searchNews(
            @Query("apikey") String apiKey,
            @Query("q") String query,
            @Query("max") int max,
            @Query("lang") String lang
    );

    class GNewsResponse {
        @SerializedName("articles")
        public List<GNewsArticle> articles;

        @SerializedName("totalArticles")
        public int totalArticles;
    }

    class GNewsArticle {
        @SerializedName("title")
        public String title;

        @SerializedName("description")
        public String description;

        @SerializedName("content")
        public String content;

        @SerializedName("url")
        public String url;

        @SerializedName("image")
        public String image;

        @SerializedName("publishedAt")
        public String publishedAt;

        @SerializedName("source")
        public GNewsSource source;

        public class GNewsSource {
            @SerializedName("name")
            public String name;

            @SerializedName("url")
            public String url;
        }
    }
}

