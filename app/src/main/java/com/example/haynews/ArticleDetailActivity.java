package com.example.haynews;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.haynews.database.NewsDatabase;
import com.example.haynews.database.NewsDao;
import com.example.haynews.service.NewsService;
import com.squareup.picasso.Picasso;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ArticleDetailActivity extends AppCompatActivity {

    ImageView imgArticle, btnLike, btnBookmark, btnShare, btnDownload;
    TextView textTitle, textMeta, textContent, textCredibility, textSource, textAuthor, textReadFull;
    
    private NewsItem currentArticle;
    private NewsService newsService;
    private NewsDao newsDao;
    private ExecutorService executorService;
    private boolean liked = false;
    private boolean bookmarked = false;
    private boolean downloaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        newsService = new NewsService(this);
        newsDao = NewsDatabase.getInstance(this).newsDao();
        executorService = Executors.newSingleThreadExecutor();

        initViews();
        loadArticleData();
        checkBookmarkStatus();
        setupButtons();
    }

    private void initViews() {
        imgArticle = findViewById(R.id.imgArticle);
        textTitle = findViewById(R.id.textTitle);
        textMeta = findViewById(R.id.textMeta);
        textContent = findViewById(R.id.textContent);
        textCredibility = findViewById(R.id.textCredibility);
        textSource = findViewById(R.id.textSource);
        textAuthor = findViewById(R.id.textAuthor);
        textReadFull = findViewById(R.id.textReadFull);

        btnLike = findViewById(R.id.btnLike);
        btnBookmark = findViewById(R.id.btnBookmark);
        btnShare = findViewById(R.id.btnShare);
        btnDownload = findViewById(R.id.btnDownload);
    }

    private void loadArticleData() {
        // Create NewsItem from intent data
        currentArticle = new NewsItem();
        currentArticle.title = getIntent().getStringExtra("title");
        currentArticle.subtitle = getIntent().getStringExtra("meta");
        currentArticle.imageUrl = getIntent().getStringExtra("image");
        currentArticle.content = getIntent().getStringExtra("content");
        currentArticle.description = getIntent().getStringExtra("description");
        currentArticle.url = getIntent().getStringExtra("url");
        currentArticle.source = getIntent().getStringExtra("source");
        currentArticle.author = getIntent().getStringExtra("author");
        
        String credibilityStr = getIntent().getStringExtra("credibility");
        try {
            currentArticle.credibilityScore = Integer.parseInt(credibilityStr);
        } catch (NumberFormatException e) {
            currentArticle.credibilityScore = 50;
        }

        textTitle.setText(currentArticle.title);
        textMeta.setText(currentArticle.subtitle);

        String descriptionText = currentArticle.description != null ? currentArticle.description.trim() : "";
        String contentText = currentArticle.content != null ? currentArticle.content.trim() : "";

        StringBuilder fullTextBuilder = new StringBuilder();
        if (!descriptionText.isEmpty()) {
            fullTextBuilder.append(descriptionText);
        }
        if (!contentText.isEmpty() && !contentText.equals(descriptionText)) {
            if (fullTextBuilder.length() > 0) {
                fullTextBuilder.append("\n\n");
            }
            fullTextBuilder.append(contentText);
        }

        String fullText = fullTextBuilder.length() > 0
                ? fullTextBuilder.toString()
                : "Content not available";
        textContent.setText(fullText);
        
        if (textCredibility != null) {
            textCredibility.setText("Credibility: " + currentArticle.credibilityScore + "%");
        }
        
        if (textSource != null && currentArticle.source != null) {
            textSource.setText("Source: " + currentArticle.source);
        }
        
        if (textAuthor != null && currentArticle.author != null) {
            textAuthor.setText("Author: " + currentArticle.author);
        }

        if (currentArticle.imageUrl != null && !currentArticle.imageUrl.isEmpty()) {
            Picasso.get().load(currentArticle.imageUrl).into(imgArticle);
        }
    }

    private void checkBookmarkStatus() {
        if (currentArticle == null || currentArticle.url == null) return;
        
        executorService.execute(() -> {
            com.example.haynews.database.NewsEntity entity = newsDao.getNewsByUrl(currentArticle.url);
            if (entity != null) {
                bookmarked = entity.isBookmarked;
                downloaded = entity.isDownloaded;
                runOnUiThread(() -> updateButtonStates());
            }
        });
    }

    private void updateButtonStates() {

    }

    private void setupButtons() {
        btnLike.setOnClickListener(v -> {
            liked = !liked;
            Toast.makeText(this, liked ? "Liked!" : "Unliked", Toast.LENGTH_SHORT).show();
            if (currentArticle != null) {
                newsService.recordArticleInteraction(currentArticle, 
                        currentArticle.category != null ? currentArticle.category : "general");
            }
        });

        btnBookmark.setOnClickListener(v -> {
            bookmarked = !bookmarked;
            if (currentArticle != null) {
                newsService.bookmarkArticle(currentArticle, bookmarked);
            }
            Toast.makeText(this, bookmarked ? "Bookmarked!" : "Removed Bookmark", 
                    Toast.LENGTH_SHORT).show();
        });

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareText = currentArticle.title;
            if (currentArticle.url != null) {
                shareText += "\n\n" + currentArticle.url;
            }
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Share Article"));
        });

        btnDownload.setOnClickListener(v -> {
            downloaded = true;
            if (currentArticle != null) {
                newsService.downloadArticle(currentArticle);
            }
            Toast.makeText(this, "Saved for offline reading", Toast.LENGTH_SHORT).show();
        });

        if (textReadFull != null) {
            textReadFull.setOnClickListener(v -> {
                if (currentArticle != null && currentArticle.url != null && !currentArticle.url.isEmpty()) {
                    android.content.Intent intent = new android.content.Intent(
                            ArticleDetailActivity.this, ArticleWebViewActivity.class);
                    intent.putExtra("url", currentArticle.url);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Full article link not available", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
