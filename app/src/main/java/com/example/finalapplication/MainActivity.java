package com.example.finalapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth refAuth;
    private TextView tvGreeting;
    private Button btnLogout, registerBTN, loginBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // שמירה על ההתנהגות של ה־EdgeToEdge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // קישור לרכיבים
        tvGreeting = findViewById(R.id.tvGreeting);
        btnLogout = findViewById(R.id.btnLogout);
        registerBTN = findViewById(R.id.register_BTN);
        loginBTN = findViewById(R.id.login_BTN);

        // Firebase Auth
        refAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = refAuth.getCurrentUser();

        if (currentUser != null) {
            // המשתמש מחובר
            tvGreeting.setText("Hello, " + (currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User"));
            tvGreeting.setVisibility(View.VISIBLE);

            btnLogout.setVisibility(View.VISIBLE);

            registerBTN.setVisibility(View.GONE);
            loginBTN.setVisibility(View.GONE);
        } else {
            // המשתמש לא מחובר
            tvGreeting.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);

            registerBTN.setVisibility(View.VISIBLE);
            loginBTN.setVisibility(View.VISIBLE);
        }

        // לחצן Logout
        btnLogout.setOnClickListener(v -> {
            refAuth.signOut();
            // רענון הפעילות לאחר Logout
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        });

        // מעבר לעמוד הרישום
        registerBTN.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // מעבר לעמוד ההתחברות
        loginBTN.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
