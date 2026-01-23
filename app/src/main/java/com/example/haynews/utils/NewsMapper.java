package com.example.haynews.utils;

import com.example.haynews.NewsItem;
import com.example.haynews.api.NewsApiResponse;
import com.example.haynews.database.NewsEntity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewsMapper {
    public static List<NewsItem> mapApiResponseToNewsItems(NewsApiResponse response) {
        if (response == null || response.articles == null) {
            return new ArrayList<>();
        }
        return mapApiResponseToNewsItems(response.articles);
    }

    public static List<NewsItem> mapApiResponseToNewsItems(List<NewsApiResponse.Article> articles) {
        List<NewsItem> newsItems = new ArrayList<>();
        
        for (NewsApiResponse.Article article : articles) {
            NewsItem item = new NewsItem();
            item.title = article.title != null ? article.title : "No Title";
            item.description = article.description != null ? article.description : "";
            item.content = article.content != null ? article.content : item.description;
            item.imageUrl = article.urlToImage != null ? article.urlToImage : "";
            item.source = article.source != null && article.source.name != null ? article.source.name : "Unknown Source";
            item.author = article.author != null ? article.author : "Unknown Author";
            item.url = article.url != null ? article.url : "";
            item.publishedAt = formatDate(article.publishedAt);
            item.subtitle = item.source + " • " + item.publishedAt;
            item.category = extractCategory(item.title, item.description);
            
            // Calculate credibility score (simplified - can be enhanced with ML)
            item.credibilityScore = calculateCredibilityScore(item.source, item.author);
            
            newsItems.add(item);
        }
        
        return newsItems;
    }

    private static String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "Unknown date";
        }
        
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return dateString;
    }

    private static String extractCategory(String title, String description) {
        String text = (title + " " + description).toLowerCase();
        
        if (text.contains("sport") || text.contains("football") || text.contains("cricket") || text.contains("basketball")) {
            return "Sports";
        } else if (text.contains("tech") || text.contains("technology") || text.contains("ai") || text.contains("software")) {
            return "Technology";
        } else if (text.contains("health") || text.contains("medical") || text.contains("disease")) {
            return "Health";
        } else if (text.contains("business") || text.contains("economy") || text.contains("stock") || text.contains("market")) {
            return "Business";
        } else if (text.contains("politics") || text.contains("government") || text.contains("election")) {
            return "Politics";
        } else if (text.contains("entertainment") || text.contains("movie") || text.contains("celebrity")) {
            return "Entertainment";
        }
        
        return "General";
    }

    private static int calculateCredibilityScore(String source, String author) {
        int score = 50; // Base score
        
        // Known credible sources get higher scores
        String[] credibleSources = {"BBC", "Reuters", "Associated Press", "The Guardian", 
                                    "The New York Times", "CNN", "Al Jazeera", "Bloomberg"};
        for (String credible : credibleSources) {
            if (source != null && source.toLowerCase().contains(credible.toLowerCase())) {
                score += 30;
                break;
            }
        }
        
        // Author presence adds credibility
        if (author != null && !author.isEmpty() && !author.equals("Unknown Author")) {
            score += 10;
        }
        
        // Ensure score is between 0 and 100
        return Math.min(100, Math.max(0, score));
    }

    public static NewsEntity newsItemToEntity(NewsItem item) {
        NewsEntity entity = new NewsEntity();
        entity.title = item.title;
        entity.subtitle = item.subtitle;
        entity.imageUrl = item.imageUrl;
        entity.content = item.content;
        entity.source = item.source;
        entity.author = item.author;
        entity.url = item.url;
        entity.category = item.category;
        entity.publishedAt = item.publishedAt;
        entity.credibilityScore = item.credibilityScore;
        entity.isBookmarked = item.isBookmarked;
        entity.isDownloaded = item.isDownloaded;
        entity.description = item.description;
        return entity;
    }

    public static List<NewsItem> entitiesToNewsItems(List<NewsEntity> entities) {
        List<NewsItem> items = new ArrayList<>();
        for (NewsEntity entity : entities) {
            NewsItem item = new NewsItem();
            item.title = entity.title;
            item.subtitle = entity.subtitle;
            item.imageUrl = entity.imageUrl;
            item.content = entity.content;
            item.source = entity.source;
            item.author = entity.author;
            item.url = entity.url;
            item.category = entity.category;
            item.publishedAt = entity.publishedAt;
            item.credibilityScore = entity.credibilityScore;
            item.isBookmarked = entity.isBookmarked;
            item.isDownloaded = entity.isDownloaded;
            item.description = entity.description;
            items.add(item);
        }
        return items;
    }
}

