package com.example.haynews;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
    private ImageButton btnSearch;

    private String selectedCategory = "all";
    private String selectedRegionCode = "us";
    private String selectedRegionName = "United States";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

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

        btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> performSearch());

        newsService = new NewsService(this);
    }

    private void setupRecyclerView() {
        adapter = new NewsAdapter(new ArrayList<>());
        adapter.setOnItemClickListener(this::openArticleDetail);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this, R.array.categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] categories = getResources().getStringArray(R.array.categories);
                selectedCategory = categories[position].toLowerCase();

                // If user already typed something, update results when category changes
                if (!editSearch.getText().toString().trim().isEmpty()) {
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
                    selectedRegionCode = regionCodes[position];
                    selectedRegionName = regions[position];
                }

                // If there is already a query or a non-all category selected, re-run search when region changes
                if (!editSearch.getText().toString().trim().isEmpty() || !"all".equals(selectedCategory)) {
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
                if (s.length() == 0) {
                    adapter.updateData(new ArrayList<>());
                    textEmpty.setVisibility(View.VISIBLE);
                    textEmpty.setText("Enter a keyword or choose filters");
                }
            }
        });

        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            && event.getAction() == KeyEvent.ACTION_DOWN)) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void performSearch() {
        String query = editSearch.getText().toString().trim();
        // Normalize category value
        String categoryValue = selectedCategory != null ? selectedCategory.toLowerCase() : "all";

        // If nothing typed and category is "all", show top headlines for the selected region
        if (query.isEmpty() && "all".equals(categoryValue)) {
            textEmpty.setVisibility(View.GONE);
            newsService.fetchTopHeadlines(selectedRegionCode, null, 20, new NewsService.NewsCallback() {
                @Override
                public void onSuccess(List<NewsItem> articles) {
                    runOnUiThread(() -> {
                        if (articles.isEmpty()) {
                            textEmpty.setVisibility(View.VISIBLE);
                            textEmpty.setText("No top headlines found");
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
            return;
        }

        textEmpty.setVisibility(View.GONE);

        // If user didn't type anything but chose a specific category, use top headlines for that region+category
        if (query.isEmpty() && !"all".equals(categoryValue)) {
            String apiCategory = mapToNewsApiCategory(categoryValue);
            newsService.fetchTopHeadlines(selectedRegionCode, apiCategory, 20, new NewsService.NewsCallback() {
                @Override
                public void onSuccess(List<NewsItem> articles) {
                    runOnUiThread(() -> {
                        if (articles.isEmpty()) {
                            textEmpty.setVisibility(View.VISIBLE);
                            textEmpty.setText("No articles found for this filter");
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
            return;
        }

        // Text query present: bias search towards selected region by appending region name
        String baseQuery = query;
        if (selectedRegionName != null && !selectedRegionName.isEmpty()) {
            baseQuery = baseQuery + " " + selectedRegionName;
        }

        newsService.searchNews(baseQuery, "relevancy", 20, new NewsService.NewsCallback() {
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

    /**
     * Map UI category label to a NewsAPI top-headlines category where possible.
     */
    private String mapToNewsApiCategory(String categoryValue) {
        if (categoryValue == null) return null;
        switch (categoryValue) {
            case "technology":
                return "technology";
            case "sports":
                return "sports";
            case "health":
                return "health";
            case "business":
                return "business";
            case "entertainment":
                return "entertainment";
            case "science":
                return "science";
            case "general":
                return "general";
            default:
                // "all" and unsupported ones like "politics" fall back to general
                return "general";
        }
    }

    private void openArticleDetail(NewsItem article) {
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
}

