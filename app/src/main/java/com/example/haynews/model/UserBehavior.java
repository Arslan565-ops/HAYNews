package com.example.haynews.model;

import java.util.HashMap;
import java.util.Map;

public class UserBehavior {
    public Map<String, Integer> categoryViews; // category -> view count
    public Map<String, Integer> sourceViews; // source -> view count
    public Map<String, Long> articleReadTime; // articleUrl -> read time in ms
    public Map<String, Integer> articleInteractions; // articleUrl -> interaction count (likes, shares, etc.)
    public long totalReadingTime;
    public int totalArticlesRead;

    public UserBehavior() {
        this.categoryViews = new HashMap<>();
        this.sourceViews = new HashMap<>();
        this.articleReadTime = new HashMap<>();
        this.articleInteractions = new HashMap<>();
        this.totalReadingTime = 0;
        this.totalArticlesRead = 0;
    }

    public void recordCategoryView(String category) {
        categoryViews.put(category, categoryViews.getOrDefault(category, 0) + 1);
    }

    public void recordSourceView(String source) {
        sourceViews.put(source, sourceViews.getOrDefault(source, 0) + 1);
    }

    public void recordArticleRead(String articleUrl, long readTimeMs) {
        articleReadTime.put(articleUrl, readTimeMs);
        totalReadingTime += readTimeMs;
        totalArticlesRead++;
    }

    public void recordInteraction(String articleUrl) {
        articleInteractions.put(articleUrl, articleInteractions.getOrDefault(articleUrl, 0) + 1);
    }

    public String getTopCategory() {
        return categoryViews.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("general");
    }

    public String getTopSource() {
        return sourceViews.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");
    }
}

