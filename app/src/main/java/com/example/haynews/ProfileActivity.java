package com.example.haynews;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.haynews.model.UserPreferences;
import com.example.haynews.service.NewsService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_NOTIFICATIONS = "notifications_enabled";
    private static final String PREFS_SYNC = "sync_preferences_enabled";

    private FirebaseAuth auth;
    private TextView txtEmail;
    private TextView txtAvatar;
    private Button btnLogout;
    private Button btnEditTopics;
    private SwitchCompat switchNotifications;
    private SwitchCompat switchSync;
    private SharedPreferences prefs;
    private NewsService newsService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        newsService = new NewsService(this);

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        bindViews();
        loadUserInfo(user);
        loadSwitchState();
        setupListeners();
    }

    private void bindViews() {
        txtEmail = findViewById(R.id.txtEmail);
        txtAvatar = findViewById(R.id.txtAvatar);
        btnLogout = findViewById(R.id.btnLogout);
        btnEditTopics = findViewById(R.id.btnEditTopics);
        switchNotifications = findViewById(R.id.switchNotifications);
        switchSync = findViewById(R.id.switchSync);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void loadUserInfo(FirebaseUser user) {
        String email = user.getEmail();
        if (email != null && !email.isEmpty()) {
            txtEmail.setText(email);
            txtAvatar.setText(getInitials(email));
        } else {
            txtEmail.setText("Signed in");
            txtAvatar.setText("?");
        }
    }

    private String getInitials(String email) {
        if (email == null || email.isEmpty()) return "?";
        String part = email.split("@")[0].trim();
        if (part.isEmpty()) return "?";
        if (part.length() >= 2) {
            return String.valueOf(part.charAt(0)).toUpperCase() + part.substring(1, 2).toUpperCase();
        }
        return String.valueOf(part.charAt(0)).toUpperCase();
    }

    private void loadSwitchState() {
        switchNotifications.setChecked(prefs.getBoolean(PREFS_NOTIFICATIONS, false));
        switchSync.setChecked(prefs.getBoolean(PREFS_SYNC, true));
    }

    private void saveSwitchState() {
        prefs.edit()
                .putBoolean(PREFS_NOTIFICATIONS, switchNotifications.isChecked())
                .putBoolean(PREFS_SYNC, switchSync.isChecked())
                .apply();
    }

    private void setupListeners() {
        btnEditTopics.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, HomeActivity.class)));

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> saveSwitchState());

        switchSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSwitchState();
            if (isChecked) {
                syncPreferencesToBackend();
            }
        });

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void syncPreferencesToBackend() {
        UserPreferences preferences = buildUserPreferencesFromPrefs();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        newsService.syncPreferencesToBackend(user.getUid(), preferences,
                () -> runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Preferences synced", Toast.LENGTH_SHORT).show()),
                () -> runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Sync failed", Toast.LENGTH_SHORT).show()));
    }

    private UserPreferences buildUserPreferencesFromPrefs() {
        UserPreferences up = new UserPreferences();
        if (prefs.getBoolean("topic_Technology", false)) up.addCategory("technology");
        if (prefs.getBoolean("topic_Sports", false)) up.addCategory("sports");
        if (prefs.getBoolean("topic_Politics", false)) up.addCategory("politics");
        if (prefs.getBoolean("topic_Health", false)) up.addCategory("health");
        if (prefs.getBoolean("topic_Business", false)) up.addCategory("business");
        if (prefs.getBoolean("topic_Entertainment", false)) up.addCategory("entertainment");
        up.region = prefs.getString("user_region", "pk");
        return up;
    }
}
