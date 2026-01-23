package com.example.haynews;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.haynews.database.NewsDatabase;
import com.example.haynews.database.NewsDao;
import com.example.haynews.database.NewsEntity;
import com.example.haynews.utils.NewsMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookmarksActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NewsAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView textEmpty;
    private NewsDao newsDao;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        // Check authentication
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initializeViews();
        setupRecyclerView();
        loadBookmarks();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        textEmpty = findViewById(R.id.textEmpty);
        
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        newsDao = NewsDatabase.getInstance(this).newsDao();
        executorService = Executors.newSingleThreadExecutor();

        swipeRefreshLayout.setOnRefreshListener(this::loadBookmarks);
    }

    private void setupRecyclerView() {
        adapter = new NewsAdapter(new ArrayList<>());
        adapter.setOnItemClickListener(this::openArticleDetail);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadBookmarks() {
        swipeRefreshLayout.setRefreshing(true);
        textEmpty.setVisibility(View.GONE);

        executorService.execute(() -> {
            List<NewsEntity> entities = newsDao.getAllBookmarked();
            List<NewsItem> bookmarks = NewsMapper.entitiesToNewsItems(entities);

            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
                if (bookmarks.isEmpty()) {
                    textEmpty.setVisibility(View.VISIBLE);
                    textEmpty.setText("No bookmarked articles yet");
                } else {
                    textEmpty.setVisibility(View.GONE);
                }
                adapter.updateData(bookmarks);
            });
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

    @Override
    protected void onResume() {
        super.onResume();
        loadBookmarks();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}

