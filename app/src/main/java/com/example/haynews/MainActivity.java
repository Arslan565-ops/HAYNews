package com.example.haynews;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.haynews.model.UserBehavior;
import com.example.haynews.model.UserPreferences;
import com.example.haynews.service.NewsService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView textWelcome;
    private RecyclerView recyclerRecommended, recyclerTrending, recyclerLocal;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NewsAdapter recommendedAdapter, trendingAdapter, localAdapter;
    private NewsService newsService;
    private UserPreferences userPreferences;
    private UserBehavior userBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check login
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        initializeViews();
        loadUserPreferences();
        loadRecommendedNews();
        loadTrendingNews();
        loadLocalNews();
    }

    private void initializeViews() {
        textWelcome = findViewById(R.id.textWelcome);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);

        recyclerRecommended = findViewById(R.id.recyclerRecommended);
        recyclerTrending = findViewById(R.id.recyclerTrending);
        recyclerLocal = findViewById(R.id.recyclerLocal);

        recyclerRecommended.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerTrending.setLayoutManager(new LinearLayoutManager(this));
        recyclerLocal.setLayoutManager(new LinearLayoutManager(this));

        recommendedAdapter = new NewsAdapter(new ArrayList<>());
        recommendedAdapter.setOnItemClickListener(this::openArticleDetail);
        trendingAdapter = new NewsAdapter(new ArrayList<>());
        trendingAdapter.setOnItemClickListener(this::openArticleDetail);
        localAdapter = new NewsAdapter(new ArrayList<>());
        localAdapter.setOnItemClickListener(this::openArticleDetail);

        recyclerRecommended.setAdapter(recommendedAdapter);
        recyclerTrending.setAdapter(trendingAdapter);
        recyclerLocal.setAdapter(localAdapter);

        textWelcome.setText("Welcome to AI-News");
        
        ImageButton buttonProfile = findViewById(R.id.buttonProfile);
        buttonProfile.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

        ImageButton buttonSearch = findViewById(R.id.buttonSearch);
        if (buttonSearch != null) {
            buttonSearch.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
            });
        }

        ImageButton buttonBookmarks = findViewById(R.id.buttonBookmarks);
        if (buttonBookmarks != null) {
            buttonBookmarks.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, BookmarksActivity.class));
            });
        }

        ImageButton buttonTrending = findViewById(R.id.buttonTrending);
        if (buttonTrending != null) {
            buttonTrending.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, TrendingLocalActivity.class));
            });
        }

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                loadRecommendedNews();
                loadTrendingNews();
                loadLocalNews();
            });
        }

        newsService = new NewsService(this);
    }

    private void loadUserPreferences() {
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        userPreferences = new UserPreferences();
        userBehavior = new UserBehavior();

        // Load selected categories
        if (prefs.getBoolean("topic_Technology", false)) {
            userPreferences.addCategory("technology");
        }
        if (prefs.getBoolean("topic_Sports", false)) {
            userPreferences.addCategory("sports");
        }
        if (prefs.getBoolean("topic_Politics", false)) {
            userPreferences.addCategory("politics");
        }
        if (prefs.getBoolean("topic_Health", false)) {
            userPreferences.addCategory("health");
        }
        if (prefs.getBoolean("topic_Business", false)) {
            userPreferences.addCategory("business");
        }
        if (prefs.getBoolean("topic_Entertainment", false)) {
            userPreferences.addCategory("entertainment");
        }

        // Default region set to Pakistan ("pk") so local news is Pakistani by default
        userPreferences.region = prefs.getString("user_region", "pk");
        newsService.setUserPreferences(userPreferences);
        newsService.setUserBehavior(userBehavior);
    }

    private void loadRecommendedNews() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        // Fetch personalized news based on user preferences using the search endpoint
        String category = userPreferences.selectedCategories.isEmpty()
                ? null
                : userPreferences.selectedCategories.get(0);

        String query = category != null ? category : "Pakistan";

        newsService.searchNews(query, "publishedAt", 20, new NewsService.NewsCallback() {
            @Override
            public void onSuccess(List<NewsItem> articles) {
                runOnUiThread(() -> {
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    // Show top 5 recommended articles
                    List<NewsItem> recommended = articles.size() > 5 
                            ? articles.subList(0, 5) 
                            : articles;
                    recommendedAdapter.updateData(recommended);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    Toast.makeText(MainActivity.this, "Error loading recommended news: " + error, 
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadTrendingNews() {
        // Use search endpoint for trending news (recent popular Pakistan news)
        newsService.searchNews("Pakistan", "publishedAt", 10, new NewsService.NewsCallback() {
            @Override
            public void onSuccess(List<NewsItem> articles) {
                runOnUiThread(() -> {
                    trendingAdapter.updateData(articles);
                });
            }

            @Override
            public void onError(String error) {
                // Silent fail for trending - don't show error
            }
        });
    }

    private void loadLocalNews() {
        // Use search endpoint for local news; if region is pk, favor Pakistan
        String query = "Pakistan";
        newsService.searchNews(query, "publishedAt", 10, new NewsService.NewsCallback() {
            @Override
            public void onSuccess(List<NewsItem> articles) {
                runOnUiThread(() -> {
                    localAdapter.updateData(articles);
                });
            }

            @Override
            public void onError(String error) {
                // Silent fail for local - don't show error
            }
        });
    }

    private void openArticleDetail(NewsItem article) {
        // Record user interaction
        newsService.recordArticleInteraction(article, article.category != null ? article.category : "general");

        Intent intent = new Intent(this, ArticleDetailActivity.class);
        intent.putExtra("title", article.title);
        intent.putExtra("meta", article.subtitle);
        intent.putExtra("image", article.imageUrl);
        intent.putExtra("content", article.content);
        intent.putExtra("description", article.description);
        intent.putExtra("credibility", String.valueOf(article.credibilityScore));
        intent.putExtra("url", article.url);
        intent.putExtra("source", article.source);
        intent.putExtra("author", article.author);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to activity
        loadRecommendedNews();
    }
}
