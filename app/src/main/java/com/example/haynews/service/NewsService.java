package com.example.haynews.service;

import android.content.Context;
import android.util.Log;

import com.example.haynews.NewsItem;
import com.example.haynews.api.GNewsApiClient;
import com.example.haynews.api.GNewsApiService;
import com.example.haynews.api.NewsApiClient;
import com.example.haynews.api.NewsApiResponse;
import com.example.haynews.api.NewsApiService;
import com.example.haynews.api.RecommendationApiClient;
import com.example.haynews.api.RecommendationResponse;
import com.example.haynews.api.RecommendationApiService;
import com.example.haynews.utils.RecommendationMapper;
import com.example.haynews.utils.GNewsMapper;
import com.example.haynews.controller.CredibilityVerifier;
import com.example.haynews.controller.RecommendationEngine;
import com.example.haynews.database.NewsDatabase;
import com.example.haynews.database.NewsDao;
import com.example.haynews.database.NewsEntity;
import com.example.haynews.model.UserBehavior;
import com.example.haynews.model.UserPreferences;
import com.example.haynews.utils.NewsMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsService {
    private static final String TAG = "NewsService";
    private NewsApiService apiService;
    private GNewsApiService gNewsApiService;
    private NewsDao newsDao;
    private CredibilityVerifier credibilityVerifier;
    private RecommendationEngine recommendationEngine;
    private UserPreferences userPreferences;
    private UserBehavior userBehavior;
    private ExecutorService executorService;
    private boolean useGNewsAsBackup = true;

    public NewsService(Context context) {
        this.apiService = NewsApiClient.getInstance().getApiService();
        this.gNewsApiService = GNewsApiClient.getInstance().getApiService();
        this.newsDao = NewsDatabase.getInstance(context).newsDao();
        this.credibilityVerifier = new CredibilityVerifier();
        this.userPreferences = new UserPreferences();
        this.userBehavior = new UserBehavior();
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public void setUserPreferences(UserPreferences preferences) {
        this.userPreferences = preferences;
        this.recommendationEngine = new RecommendationEngine(preferences, userBehavior);
    }

    public void setUserBehavior(UserBehavior behavior) {
        this.userBehavior = behavior;
        this.recommendationEngine = new RecommendationEngine(userPreferences, behavior);
    }

    public interface NewsCallback {
        void onSuccess(List<NewsItem> articles);
        void onError(String error);
    }
    public void fetchTopHeadlines(String country, String category, int pageSize, NewsCallback callback) {
        String apiKey = com.example.haynews.BuildConfig.NEWS_API_KEY;

        final String resolvedCountry = (country == null || country.isEmpty()) ? "pk" : country;
        final String resolvedCategory = (category == null || category.isEmpty()) ? "general" : category;

        Call<NewsApiResponse> call = apiService.getTopHeadlines(apiKey, resolvedCountry, resolvedCategory, pageSize);
        call.enqueue(new Callback<NewsApiResponse>() {
            @Override
            public void onResponse(Call<NewsApiResponse> call, Response<NewsApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<NewsItem> articles = NewsMapper.mapApiResponseToNewsItems(response.body());
                    
                    // Verify credibility and personalize
                    executorService.execute(() -> {
                        List<NewsItem> processedArticles = processArticles(articles);
                        callback.onSuccess(processedArticles);
                    });
                } else {
                    String errorMsg = "Failed to fetch news: code " + response.code() + " - " + response.message();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<NewsApiResponse> call, Throwable t) {
                Log.e(TAG, "Error fetching news from NewsAPI", t);
                // Try GNews as backup
                if (useGNewsAsBackup) {
                    fetchFromGNews(resolvedCountry, resolvedCategory, pageSize, callback);
                } else {
                    loadCachedNews(callback);
                }
            }
        });
    }
    public void searchNews(String query, String sortBy, int pageSize, NewsCallback callback) {
        String apiKey = com.example.haynews.BuildConfig.NEWS_API_KEY;
        
        Call<NewsApiResponse> call = apiService.searchNews(apiKey, query, sortBy, pageSize, "en");
        call.enqueue(new Callback<NewsApiResponse>() {
            @Override
            public void onResponse(Call<NewsApiResponse> call, Response<NewsApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<NewsItem> articles = NewsMapper.mapApiResponseToNewsItems(response.body());
                    
                    executorService.execute(() -> {
                        List<NewsItem> processedArticles = processArticles(articles);
                        callback.onSuccess(processedArticles);
                    });
                } else {
                    String errorMsg = "Search failed: code " + response.code() + " - " + response.message();
                    Log.e(TAG, errorMsg);

                    // If NewsAPI search is rate-limited or fails, try GNews search as backup
                    if (useGNewsAsBackup) {
                        fetchSearchFromGNews(query, pageSize, callback);
                    } else {
                        callback.onError(errorMsg);
                    }
                }
            }

            @Override
            public void onFailure(Call<NewsApiResponse> call, Throwable t) {
                Log.e(TAG, "Error searching news", t);

                if (useGNewsAsBackup) {
                    fetchSearchFromGNews(query, pageSize, callback);
                } else {
                    callback.onError("Network error: " + t.getMessage());
                }
            }
        });
    }
    private List<NewsItem> processArticles(List<NewsItem> articles) {
        List<NewsItem> processed = new ArrayList<>();
        
        for (NewsItem article : articles) {
            // Filter out unwanted sources (e.g., Indian sites that may be blocked locally)
            if (isBlockedSource(article)) {
                continue;
            }

            // Verify credibility
            int credibilityScore = credibilityVerifier.verifyCredibility(article, articles);
            article.credibilityScore = credibilityScore;
            
            // Save to database for offline access
            saveArticleToDatabase(article);
            
            processed.add(article);
        }
        
        // Personalize if recommendation engine is available
        if (recommendationEngine != null) {
            return recommendationEngine.personalizeNews(processed);
        }
        
        return processed;
    }

    private boolean isBlockedSource(NewsItem article) {
        if (article == null) return true;

        String url = article.url != null ? article.url.toLowerCase() : "";
        String sourceName = article.source != null ? article.source.toLowerCase() : "";

        // Skip common Indian news domains
        String[] blockedDomains = {
                "https://timesofindia.indiatimes.com/",
                "https://www.ndtv.com/",
                "https://www.indiatoday.in/",
                "https://www.hindustantimes.com/"
        };

        for (String domain : blockedDomains) {
            if (!domain.isEmpty() && url.contains(domain)) {
                return true;
            }
        }

        // Also skip if source name clearly mentions "india" or "indian"
        if (sourceName.contains("india") || sourceName.contains("indian")) {
            return true;
        }

        return false;
    }
    private void saveArticleToDatabase(NewsItem article) {
        executorService.execute(() -> {
            NewsEntity entity = NewsMapper.newsItemToEntity(article);
            NewsEntity existing = newsDao.getNewsByUrl(article.url);
            
            if (existing == null) {
                newsDao.insert(entity);
            } else {
                entity.id = existing.id;
                newsDao.update(entity);
            }
        });
    }
    private void fetchFromGNews(String country, String category, int pageSize, NewsCallback callback) {
        String apiKey = "d082a3be6eb9bfbea088b5638fb88607";
        
        if (apiKey.isEmpty()) {
            loadCachedNews(callback);
            return;
        }

        Call<GNewsApiService.GNewsResponse> call = gNewsApiService.getTopHeadlines(
                apiKey, country, category != null ? category : "general", pageSize);
        
        call.enqueue(new retrofit2.Callback<GNewsApiService.GNewsResponse>() {
            @Override
            public void onResponse(Call<GNewsApiService.GNewsResponse> call, 
                    Response<GNewsApiService.GNewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<NewsItem> articles = GNewsMapper.mapGNewsResponseToNewsItems(response.body());
                    executorService.execute(() -> {
                        List<NewsItem> processedArticles = processArticles(articles);
                        callback.onSuccess(processedArticles);
                    });
                } else {
                    loadCachedNews(callback);
                }
            }

            @Override
            public void onFailure(Call<GNewsApiService.GNewsResponse> call, Throwable t) {
                Log.e(TAG, "Error fetching from GNews", t);
                loadCachedNews(callback);
            }
        });
    }

    private void fetchSearchFromGNews(String query, int pageSize, NewsCallback callback) {
        String apiKey = "d082a3be6eb9bfbea088b5638fb88607";

        if (apiKey.isEmpty()) {
            loadCachedNews(callback);
            return;
        }

        Call<GNewsApiService.GNewsResponse> call = gNewsApiService.searchNews(
                apiKey, query, pageSize, "en");

        call.enqueue(new retrofit2.Callback<GNewsApiService.GNewsResponse>() {
            @Override
            public void onResponse(Call<GNewsApiService.GNewsResponse> call,
                                   Response<GNewsApiService.GNewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<NewsItem> articles = GNewsMapper.mapGNewsResponseToNewsItems(response.body());
                    executorService.execute(() -> {
                        List<NewsItem> processedArticles = processArticles(articles);
                        callback.onSuccess(processedArticles);
                    });
                } else {
                    String errorMsg = "GNews search failed: code " + response.code() + " - " + response.message();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<GNewsApiService.GNewsResponse> call, Throwable t) {
                Log.e(TAG, "Error searching news from GNews", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    private void loadCachedNews(NewsCallback callback) {
        executorService.execute(() -> {
            List<NewsEntity> entities = newsDao.getAllDownloaded();
            List<NewsItem> articles = NewsMapper.entitiesToNewsItems(entities);
            callback.onSuccess(articles);
        });
    }
    public void bookmarkArticle(NewsItem article, boolean isBookmarked) {
        executorService.execute(() -> {
            newsDao.updateBookmarkStatus(article.url, isBookmarked);
            if (isBookmarked) {
                userPreferences.addBookmarkedArticle(article.url);
            }
        });
    }
    public void downloadArticle(NewsItem article) {
        executorService.execute(() -> {
            newsDao.updateDownloadStatus(article.url, true);
            saveArticleToDatabase(article);
        });
    }
    public void recordArticleInteraction(NewsItem article, String category) {
        executorService.execute(() -> {
            userBehavior.recordCategoryView(category);
            userBehavior.recordSourceView(article.source);
            userBehavior.recordInteraction(article.url);
            userPreferences.addToHistory(article.url);
        });
    }

    /**
     * Fetch personalized recommendations from the backend API (based on user's reading genres).
     * On failure or if backend is unavailable, callback receives onError and caller can fall back to local logic.
     */
    public void fetchRecommendationsFromBackend(String userId, int limit, NewsCallback callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onError("No user ID");
            return;
        }
        RecommendationApiService api = RecommendationApiClient.getInstance().getApiService();
        api.getRecommendations(userId, limit).enqueue(new retrofit2.Callback<RecommendationResponse>() {
            @Override
            public void onResponse(retrofit2.Call<RecommendationResponse> call, retrofit2.Response<RecommendationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<NewsItem> articles = RecommendationMapper.mapToNewsItems(response.body());
                    callback.onSuccess(articles);
                } else {
                    callback.onError("Backend returned " + response.code());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<RecommendationResponse> call, Throwable t) {
                Log.e(TAG, "Backend recommendations failed", t);
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Sync user preferences (genres/categories and region) to the backend so recommendations use them.
     */
    public void syncPreferencesToBackend(String userId, UserPreferences preferences, final Runnable onSuccess, final Runnable onError) {
        if (userId == null || userId.isEmpty()) {
            if (onError != null) onError.run();
            return;
        }
        RecommendationApiService api = RecommendationApiClient.getInstance().getApiService();
        RecommendationApiService.UserPreferencesBody body = new RecommendationApiService.UserPreferencesBody(
                preferences.selectedCategories != null ? preferences.selectedCategories : new ArrayList<>(),
                preferences.region != null ? preferences.region : "pk"
        );
        api.putPreferences(userId, body).enqueue(new retrofit2.Callback<RecommendationApiService.PreferencesUpdateResponse>() {
            @Override
            public void onResponse(retrofit2.Call<RecommendationApiService.PreferencesUpdateResponse> call,
                                  retrofit2.Response<RecommendationApiService.PreferencesUpdateResponse> response) {
                if (response.isSuccessful() && onSuccess != null) {
                    onSuccess.run();
                } else if (onError != null) {
                    onError.run();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<RecommendationApiService.PreferencesUpdateResponse> call, Throwable t) {
                Log.e(TAG, "Sync preferences to backend failed", t);
                if (onError != null) onError.run();
            }
        });
    }
}

