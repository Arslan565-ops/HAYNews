package com.example.haynews.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NewsApiResponse {
    @SerializedName("status")
    public String status;

    @SerializedName("totalResults")
    public int totalResults;

    @SerializedName("articles")
    public List<Article> articles;

    public static class Article {
        @SerializedName("source")
        public Source source;

        @SerializedName("author")
        public String author;

        @SerializedName("title")
        public String title;

        @SerializedName("description")
        public String description;

        @SerializedName("url")
        public String url;

        @SerializedName("urlToImage")
        public String urlToImage;

        @SerializedName("publishedAt")
        public String publishedAt;

        @SerializedName("content")
        public String content;

        public static class Source {
            @SerializedName("id")
            public String id;

            @SerializedName("name")
            public String name;
        }
    }
}

