package com.example.haynews.controller;

import com.example.haynews.NewsItem;
import com.example.haynews.model.UserPreferences;
import com.example.haynews.model.UserBehavior;
import java.util.ArrayList;
import java.util.List;

public class RecommendationEngine {
    private UserPreferences preferences;
    private UserBehavior behavior;

    public RecommendationEngine(UserPreferences preferences, UserBehavior behavior) {
        this.preferences = preferences;
        this.behavior = behavior;
    }

    public List<NewsItem> personalizeNews(List<NewsItem> articles) {
        if (articles == null || articles.isEmpty()) {
            return new ArrayList<>();
        }

        List<ScoredArticle> scoredArticles = new ArrayList<>();

        for (NewsItem article : articles) {
            double score = calculateRelevanceScore(article);
            scoredArticles.add(new ScoredArticle(article, score));
        }

        // Sort by score (descending)
        scoredArticles.sort((a, b) -> Double.compare(b.score, a.score));

        // Return top articles
        List<NewsItem> recommended = new ArrayList<>();
        for (ScoredArticle scored : scoredArticles) {
            recommended.add(scored.article);
        }

        return recommended;
    }

    private double calculateRelevanceScore(NewsItem article) {
        double score = 0.0;

        // Category match (40% weight)
        if (article.category != null && preferences.selectedCategories.contains(article.category.toLowerCase())) {
            score += 0.4;
        }

        // Source preference (20% weight)
        if (article.source != null && preferences.preferredSources.contains(article.source)) {
            score += 0.2;
        } else if (behavior.getTopSource().equals(article.source)) {
            score += 0.15;
        }

        // Category popularity from behavior (20% weight)
        if (article.category != null) {
            int categoryViews = behavior.categoryViews.getOrDefault(article.category.toLowerCase(), 0);
            score += 0.2 * Math.min(categoryViews / 10.0, 1.0); // Normalize to 0-1
        }

        // Recency (10% weight) - newer articles get higher score
        if (article.publishedAt != null) {
            long hoursAgo = getHoursSincePublication(article.publishedAt);
            score += 0.1 * Math.max(0, 1.0 - (hoursAgo / 48.0)); // Favor articles < 48 hours old
        }

        // Credibility (10% weight)
        score += 0.1 * (article.credibilityScore / 100.0);

        return score;
    }

    private long getHoursSincePublication(String publishedAt) {
        try {
            // Simple parsing - assuming ISO 8601 format
            // For production, use proper date parsing
            return 0; // Placeholder
        } catch (Exception e) {
            return 24; // Default to 24 hours if parsing fails
        }
    }

    private static class ScoredArticle {
        NewsItem article;
        double score;

        ScoredArticle(NewsItem article, double score) {
            this.article = article;
            this.score = score;
        }
    }
}

