package com.example.haynews.model;

import java.util.ArrayList;
import java.util.List;

public class UserPreferences {
    public List<String> selectedCategories;
    public List<String> preferredSources;
    public String region;
    public List<String> readingHistory;
    public List<String> likedArticles;
    public List<String> bookmarkedArticles;
    public long lastUpdated;

    public UserPreferences() {
        this.selectedCategories = new ArrayList<>();
        this.preferredSources = new ArrayList<>();
        this.readingHistory = new ArrayList<>();
        this.likedArticles = new ArrayList<>();
        this.bookmarkedArticles = new ArrayList<>();
        this.region = "us";
        this.lastUpdated = System.currentTimeMillis();
    }

    public void addCategory(String category) {
        if (!selectedCategories.contains(category)) {
            selectedCategories.add(category);
        }
    }

    public void addToHistory(String articleUrl) {
        if (!readingHistory.contains(articleUrl)) {
            readingHistory.add(articleUrl);
            // Keep only last 100 articles
            if (readingHistory.size() > 100) {
                readingHistory.remove(0);
            }
        }
    }

    public void addLikedArticle(String articleUrl) {
        if (!likedArticles.contains(articleUrl)) {
            likedArticles.add(articleUrl);
        }
    }

    public void addBookmarkedArticle(String articleUrl) {
        if (!bookmarkedArticles.contains(articleUrl)) {
            bookmarkedArticles.add(articleUrl);
        }
    }
}

