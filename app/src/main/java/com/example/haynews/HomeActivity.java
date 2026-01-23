package com.example.haynews;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ToggleButton;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {

    ToggleButton btnTech, btnSports, btnPolitics, btnHealth, btnBusiness, btnEntertainment;
    Button btnContinue;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize buttons
        btnTech = findViewById(R.id.btnTech);
        btnSports = findViewById(R.id.btnSports);
        btnPolitics = findViewById(R.id.btnPolitics);
        btnHealth = findViewById(R.id.btnHealth);
        btnBusiness = findViewById(R.id.btnBusiness);
        btnEntertainment = findViewById(R.id.btnEntertainment);
        btnContinue = findViewById(R.id.btnContinue);

        prefs = getSharedPreferences("UserData", MODE_PRIVATE);

        restoreTopicStates();

        btnContinue.setOnClickListener(v -> saveTopics());
    }

    private void restoreTopicStates() {
        btnTech.setChecked(prefs.getBoolean("topic_Technology", false));
        btnSports.setChecked(prefs.getBoolean("topic_Sports", false));
        btnPolitics.setChecked(prefs.getBoolean("topic_Politics", false));
        btnHealth.setChecked(prefs.getBoolean("topic_Health", false));
        btnBusiness.setChecked(prefs.getBoolean("topic_Business", false));
        btnEntertainment.setChecked(prefs.getBoolean("topic_Entertainment", false));
    }

    private void saveTopics() {
        boolean anySelected = false;

        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("topic_Technology", btnTech.isChecked());
        editor.putBoolean("topic_Sports", btnSports.isChecked());
        editor.putBoolean("topic_Politics", btnPolitics.isChecked());
        editor.putBoolean("topic_Health", btnHealth.isChecked());
        editor.putBoolean("topic_Business", btnBusiness.isChecked());
        editor.putBoolean("topic_Entertainment", btnEntertainment.isChecked());

        anySelected = btnTech.isChecked() || btnSports.isChecked() || btnPolitics.isChecked() ||
                btnHealth.isChecked() || btnBusiness.isChecked() || btnEntertainment.isChecked();

        if (!anySelected) {
            Toast.makeText(this, "Select at least one topic", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save that topics are selected
        editor.putBoolean("topicsSelected", true);
        editor.apply();

        Toast.makeText(this, "Topics Saved", Toast.LENGTH_SHORT).show();

        // Go to main news screen
        startActivity(new Intent(HomeActivity.this, MainActivity.class));
        finish();
    }
}
