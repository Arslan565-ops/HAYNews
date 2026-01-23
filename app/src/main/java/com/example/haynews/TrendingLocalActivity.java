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

import com.example.haynews.service.NewsService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class TrendingLocalActivity extends AppCompatActivity {
    private RecyclerView recyclerTrending, recyclerLocal;
    private NewsAdapter trendingAdapter, localAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView textTrendingEmpty, textLocalEmpty;
    private NewsService newsService;
    private FusedLocationProviderClient locationClient;
    private String userRegion = "us";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trending_local);

        // Check authentication
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initializeViews();
        setupRecyclerViews();
        loadUserRegion();
        loadTrendingNews();
        loadLocalNews();
    }

    private void initializeViews() {
        recyclerTrending = findViewById(R.id.recyclerTrending);
        recyclerLocal = findViewById(R.id.recyclerLocal);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        textTrendingEmpty = findViewById(R.id.textTrendingEmpty);
        textLocalEmpty = findViewById(R.id.textLocalEmpty);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        newsService = new NewsService(this);
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadTrendingNews();
            loadLocalNews();
        });
    }

    private void setupRecyclerViews() {
        trendingAdapter = new NewsAdapter(new ArrayList<>());
        trendingAdapter.setOnItemClickListener(this::openArticleDetail);
        recyclerTrending.setLayoutManager(new LinearLayoutManager(this));
        recyclerTrending.setAdapter(trendingAdapter);

        localAdapter = new NewsAdapter(new ArrayList<>());
        localAdapter.setOnItemClickListener(this::openArticleDetail);
        recyclerLocal.setLayoutManager(new LinearLayoutManager(this));
        recyclerLocal.setAdapter(localAdapter);
    }

    private void loadUserRegion() {
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        userRegion = prefs.getString("user_region", "us");
        
        // Try to get location if permission granted
        // For now, use saved preference or default
    }

    private void loadTrendingNews() {
        textTrendingEmpty.setVisibility(View.GONE);
        
        // Fetch trending news (top headlines sorted by popularity)
        newsService.fetchTopHeadlines("us", null, 20, new NewsService.NewsCallback() {
            @Override
            public void onSuccess(List<NewsItem> articles) {
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    if (articles.isEmpty()) {
                        textTrendingEmpty.setVisibility(View.VISIBLE);
                        textTrendingEmpty.setText("No trending news available");
                    } else {
                        textTrendingEmpty.setVisibility(View.GONE);
                        // Take top 10 as trending
                        List<NewsItem> trending = articles.size() > 10 
                                ? articles.subList(0, 10) 
                                : articles;
                        trendingAdapter.updateData(trending);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(TrendingLocalActivity.this, 
                            "Error loading trending: " + error, Toast.LENGTH_SHORT).show();
                    textTrendingEmpty.setVisibility(View.VISIBLE);
                    textTrendingEmpty.setText("Unable to load trending news");
                });
            }
        });
    }

    private void loadLocalNews() {
        textLocalEmpty.setVisibility(View.GONE);
        
        // Fetch local news based on user region
        newsService.fetchTopHeadlines(userRegion, null, 15, new NewsService.NewsCallback() {
            @Override
            public void onSuccess(List<NewsItem> articles) {
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    if (articles.isEmpty()) {
                        textLocalEmpty.setVisibility(View.VISIBLE);
                        textLocalEmpty.setText("No local news available");
                    } else {
                        textLocalEmpty.setVisibility(View.GONE);
                        localAdapter.updateData(articles);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(TrendingLocalActivity.this, 
                            "Error loading local news: " + error, Toast.LENGTH_SHORT).show();
                    textLocalEmpty.setVisibility(View.VISIBLE);
                    textLocalEmpty.setText("Unable to load local news");
                });
            }
        });
    }

    private void openArticleDetail(NewsItem article) {
        Intent intent = new Intent(this, ArticleDetailActivity.class);
        intent.putExtra("title", article.title);
        intent.putExtra("meta", article.subtitle);
        intent.putExtra("image", article.imageUrl);
        intent.putExtra("content", article.content != null ? article.content : article.description);
        intent.putExtra("credibility", String.valueOf(article.credibilityScore));
        intent.putExtra("url", article.url);
        intent.putExtra("source", article.source);
        intent.putExtra("author", article.author);
        startActivity(intent);
    }
}

