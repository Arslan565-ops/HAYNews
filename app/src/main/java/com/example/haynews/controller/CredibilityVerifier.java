package com.example.haynews.controller;

import com.example.haynews.NewsItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CredibilityVerifier {
    // Trusted news sources (can be expanded)
    private static final List<String> TRUSTED_SOURCES = List.of(
            "BBC News", "Reuters", "Associated Press", "The Guardian",
            "CNN", "The New York Times", "Washington Post", "NPR",
            "Al Jazeera", "Bloomberg", "Financial Times", "The Wall Street Journal"
    );

    // Suspicious keywords that might indicate fake news
    private static final List<String> SUSPICIOUS_KEYWORDS = List.of(
            "BREAKING: SHOCKING", "YOU WON'T BELIEVE", "DOCTORS HATE",
            "INSTANTLY", "GUARANTEED", "MIRACLE", "SECRET"
    );

    /**
     * Verifies article credibility based on multiple factors
     */
    public int verifyCredibility(NewsItem article, List<NewsItem> similarArticles) {
        int score = 50; // Base score

        // Source credibility (30 points)
        if (article.source != null) {
            if (TRUSTED_SOURCES.contains(article.source)) {
                score += 30;
            } else {
                // Check if source is in trusted list (case-insensitive)
                boolean isTrusted = TRUSTED_SOURCES.stream()
                        .anyMatch(trusted -> trusted.equalsIgnoreCase(article.source));
                if (isTrusted) {
                    score += 30;
                } else {
                    score += 10; // Unknown source gets partial credit
                }
            }
        }

        // Cross-source verification (20 points)
        if (similarArticles != null && !similarArticles.isEmpty()) {
            int matchingCount = countMatchingArticles(article, similarArticles);
            if (matchingCount >= 3) {
                score += 20; // Multiple sources confirm
            } else if (matchingCount >= 1) {
                score += 10; // At least one source confirms
            }
        }

        // Content quality checks (20 points)
        if (article.title != null && article.content != null) {
            // Check for suspicious keywords
            String titleLower = article.title.toLowerCase();
            boolean hasSuspiciousKeywords = SUSPICIOUS_KEYWORDS.stream()
                    .anyMatch(keyword -> titleLower.contains(keyword.toLowerCase()));
            
            if (hasSuspiciousKeywords) {
                score -= 15; // Penalize suspicious content
            } else {
                score += 10;
            }

            // Check content length (longer articles are generally more credible)
            if (article.content.length() > 500) {
                score += 10;
            } else if (article.content.length() < 100) {
                score -= 5;
            }
        }

        // Author verification (10 points)
        if (article.author != null && !article.author.isEmpty()) {
            score += 10;
        }

        // URL validity (10 points)
        if (article.url != null && article.url.startsWith("http")) {
            score += 10;
        }

        // Ensure score is between 0 and 100
        return Math.max(0, Math.min(100, score));
    }

    private int countMatchingArticles(NewsItem article, List<NewsItem> similarArticles) {
        int count = 0;
        String articleTitle = normalizeTitle(article.title);

        for (NewsItem similar : similarArticles) {
            if (similar.url != null && !similar.url.equals(article.url)) {
                String similarTitle = normalizeTitle(similar.title);
                // Simple similarity check - can be enhanced with NLP
                if (calculateSimilarity(articleTitle, similarTitle) > 0.6) {
                    count++;
                }
            }
        }

        return count;
    }

    private String normalizeTitle(String title) {
        if (title == null) return "";
        return title.toLowerCase().replaceAll("[^a-z0-9\\s]", "");
    }

    private double calculateSimilarity(String title1, String title2) {
        if (title1 == null || title2 == null) return 0.0;
        
        String[] words1 = title1.split("\\s+");
        String[] words2 = title2.split("\\s+");
        
        int commonWords = 0;
        for (String word : words1) {
            if (word.length() > 3) { // Ignore short words
                for (String word2 : words2) {
                    if (word.equals(word2)) {
                        commonWords++;
                        break;
                    }
                }
            }
        }
        
        int totalWords = Math.max(words1.length, words2.length);
        return totalWords > 0 ? (double) commonWords / totalWords : 0.0;
    }

    /**
     * Tags article with credibility status
     */
    public String getCredibilityStatus(int score) {
        if (score >= 80) {
            return "Highly Credible";
        } else if (score >= 60) {
            return "Credible";
        } else if (score >= 40) {
            return "Moderate";
        } else {
            return "Unverified";
        }
    }
}

