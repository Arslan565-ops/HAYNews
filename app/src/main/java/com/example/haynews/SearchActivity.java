package com.example.haynews;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.haynews.service.NewsService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private EditText editSearch;
    private Spinner spinnerCategory;
    private Spinner spinnerRegion;
    private RecyclerView recyclerView;
    private NewsAdapter adapter;
    private NewsService newsService;
    private TextView textEmpty;

    private String selectedCategory = "all";
    private String selectedRegion = "us";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Check authentication
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initializeViews();
        setupRecyclerView();
        setupSpinners();
        setupSearch();
    }

    private void initializeViews() {
        editSearch = findViewById(R.id.editSearch);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerRegion = findViewById(R.id.spinnerRegion);
        recyclerView = findViewById(R.id.recyclerView);
        textEmpty = findViewById(R.id.textEmpty);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        newsService = new NewsService(this);
    }

    private void setupRecyclerView() {
        adapter = new NewsAdapter(new ArrayList<>());
        adapter.setOnItemClickListener(this::openArticleDetail);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSpinners() {
        // Category spinner
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this, R.array.categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] categories = getResources().getStringArray(R.array.categories);
                selectedCategory = categories[position].toLowerCase();
                if (selectedCategory.equals("all")) {
                    performSearch();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Region spinner
        ArrayAdapter<CharSequence> regionAdapter = ArrayAdapter.createFromResource(
                this, R.array.regions, android.R.layout.simple_spinner_item);
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRegion.setAdapter(regionAdapter);
        spinnerRegion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] regions = getResources().getStringArray(R.array.regions);
                String[] regionCodes = getResources().getStringArray(R.array.region_codes);
                if (position < regionCodes.length) {
                    selectedRegion = regionCodes[position];
                    performSearch();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSearch() {
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 2) {
                    performSearch();
                } else if (s.length() == 0) {
                    adapter.updateData(new ArrayList<>());
                    textEmpty.setVisibility(View.GONE);
                }
            }
        });
    }

    private void performSearch() {
        String query = editSearch.getText().toString().trim();
        
        if (query.isEmpty() && selectedCategory.equals("all")) {
            adapter.updateData(new ArrayList<>());
            textEmpty.setVisibility(View.GONE);
            return;
        }

        textEmpty.setVisibility(View.GONE);
        String searchQuery = query.isEmpty() ? selectedCategory : query;

        newsService.searchNews(searchQuery, "relevancy", 20, new NewsService.NewsCallback() {
            @Override
            public void onSuccess(List<NewsItem> articles) {
                runOnUiThread(() -> {
                    if (articles.isEmpty()) {
                        textEmpty.setVisibility(View.VISIBLE);
                        textEmpty.setText("No articles found");
                    } else {
                        textEmpty.setVisibility(View.GONE);
                    }
                    adapter.updateData(articles);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(SearchActivity.this, error, Toast.LENGTH_SHORT).show();
                    textEmpty.setVisibility(View.VISIBLE);
                    textEmpty.setText("Error: " + error);
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

