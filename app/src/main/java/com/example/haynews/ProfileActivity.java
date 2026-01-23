package com.example.haynews;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth auth;
    TextView txtEmail;
    Button btnLogout, btnEditTopics;
    Switch switchNotifications, switchSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();

        txtEmail = findViewById(R.id.txtEmail);
        btnLogout = findViewById(R.id.btnLogout);
        btnEditTopics = findViewById(R.id.btnEditTopics);
        switchNotifications = findViewById(R.id.switchNotifications);
        switchSync = findViewById(R.id.switchSync);

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            txtEmail.setText(user.getEmail());
        }

        btnEditTopics.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, HomeActivity.class))
        );

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
