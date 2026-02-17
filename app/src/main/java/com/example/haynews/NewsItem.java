package com.example.haynews;

import java.io.Serializable;

public class NewsItem implements Serializable {
    public String title;
    public String subtitle;
    public String imageUrl;
    public String content;
    public String source;
    public String author;
    public String url;
    public String category;
    public String publishedAt;
    public int credibilityScore;
    public boolean isBookmarked;
    public boolean isDownloaded;
    public String description;

    public NewsItem() {
        this.credibilityScore = 0;
        this.isBookmarked = false;
        this.isDownloaded = false;
    }

    public NewsItem(String title, String subtitle, String imageUrl) {
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
        this.credibilityScore = 0;
        this.isBookmarked = false;
        this.isDownloaded = false;
    }

    public NewsItem(String title, String subtitle, String imageUrl, String content,
                   String source, String author, String url, String category,
                   String publishedAt, String description) {
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
        this.content = content;
        this.source = source;
        this.author = author;
        this.url = url;
        this.category = category;
        this.publishedAt = publishedAt;
        this.description = description;
        this.credibilityScore = 0;
        this.isBookmarked = false;
        this.isDownloaded = false;
    }
}
