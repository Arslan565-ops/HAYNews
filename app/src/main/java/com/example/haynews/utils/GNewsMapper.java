package com.example.haynews.utils;

import com.example.haynews.NewsItem;
import com.example.haynews.api.GNewsApiService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GNewsMapper {
    public static List<NewsItem> mapGNewsResponseToNewsItems(GNewsApiService.GNewsResponse response) {
        if (response == null || response.articles == null) {
            return new ArrayList<>();
        }

        List<NewsItem> newsItems = new ArrayList<>();

        for (GNewsApiService.GNewsArticle article : response.articles) {
            NewsItem item = new NewsItem();
            item.title = article.title != null ? article.title : "No Title";
            item.description = article.description != null ? article.description : "";
            item.content = article.content != null ? article.content : item.description;
            item.imageUrl = article.image != null ? article.image : "";
            item.source = (article.source != null && article.source.name != null) 
                    ? article.source.name : "Unknown Source";
            item.author = "Unknown Author"; // GNews doesn't always provide author
            item.url = article.url != null ? article.url : "";
            item.publishedAt = formatDate(article.publishedAt);
            item.subtitle = item.source + " • " + item.publishedAt;
            item.category = extractCategory(item.title, item.description);
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

        String[] credibleSources = {"BBC", "Reuters", "Associated Press", "The Guardian",
                "The New York Times", "CNN", "Al Jazeera", "Bloomberg"};
        for (String credible : credibleSources) {
            if (source != null && source.toLowerCase().contains(credible.toLowerCase())) {
                score += 30;
                break;
            }
        }

        if (author != null && !author.isEmpty() && !author.equals("Unknown Author")) {
            score += 10;
        }

        return Math.min(100, Math.max(0, score));
    }
}

