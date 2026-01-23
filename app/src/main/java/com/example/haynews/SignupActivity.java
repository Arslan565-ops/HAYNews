package com.example.haynews;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    private EditText emailEditText, passEditText, confirmPassEditText;
    private Button signupBtn;
    private TextView loginLink;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.editTextEmail);
        passEditText = findViewById(R.id.editTextPassword);
        confirmPassEditText = findViewById(R.id.editTextConfirmPassword);
        signupBtn = findViewById(R.id.btnSignup);
        loginLink = findViewById(R.id.textLogin);

        signupBtn.setOnClickListener(v -> createAccount());

        loginLink.setOnClickListener(v -> finish());
    }

    private void createAccount() {
        String email = emailEditText.getText().toString().trim();
        String pass = passEditText.getText().toString().trim();
        String confirmPass = confirmPassEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Enter email");
            return;
        }

        if (TextUtils.isEmpty(pass)) {
            passEditText.setError("Enter password");
            return;
        }

        if (!pass.equals(confirmPass)) {
            confirmPassEditText.setError("Passwords do not match");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "Account Created", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
