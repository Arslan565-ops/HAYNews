package com.example.haynews;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 1800; // 1.8 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Animate logo
        ImageView logo = findViewById(R.id.imgLogo);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logo.startAnimation(fadeIn);

        new Handler().postDelayed(this::routeUser, SPLASH_TIME);
    }

    private void routeUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            startActivity(new Intent(StartActivity.this, LoginActivity.class));
        } else {
            SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
            boolean topicsSelected = prefs.getBoolean("topicsSelected", false);

            if (!topicsSelected) {
                startActivity(new Intent(StartActivity.this, HomeActivity.class));
            } else {
                startActivity(new Intent(StartActivity.this, MainActivity.class));
            }
        }

        finish();
    }
}
