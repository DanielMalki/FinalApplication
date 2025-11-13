package com.example.finalapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private TextView tVMsg;
    private FirebaseAuth refAuth;
    private CheckBox cbStayConnect;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // איתחול Firebase
        refAuth = FirebaseAuth.getInstance();

        // איתחול SharedPreferences
        sharedPref = getSharedPreferences("MyPref", MODE_PRIVATE);

        // מציאת רכיבים מה־XML
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbStayConnect = findViewById(R.id.cbStayConnect);
        tVMsg = findViewById(R.id.tvForgotPassword); // אפשר לשנות ל־tvMsg אם תוסיף טקסט ייעודי
        MaterialButton btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(this::loginUser);

        TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // מעבר ל-RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish(); // סוגר את עמוד ההתחברות
            }
        });

    }

    public void loginUser(View view) {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            tVMsg.setText("Please fill all fields");
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Connecting");
        pd.setMessage("Logging in user...");
        pd.show();

        refAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        pd.dismiss();
                        if (task.isSuccessful()) {
                            Log.i("LoginActivity", "signInWithEmailAndPassword:success");
                            FirebaseUser user = refAuth.getCurrentUser();

                            // שמור את מצב ה-checkbox
                            boolean stayConnected = cbStayConnect.isChecked();
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean("stayConnect", stayConnected);
                            editor.apply();

                            // המשך ל-MainActivity
                            Intent si = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(si);
                            finish();

                        } else {
                            Exception exp = task.getException();
                            if (exp instanceof FirebaseAuthInvalidUserException) {
                                tVMsg.setText("Invalid email address.");
                            } else if (exp instanceof FirebaseAuthWeakPasswordException) {
                                tVMsg.setText("Password too weak.");
                            } else if (exp instanceof FirebaseAuthUserCollisionException) {
                                tVMsg.setText("User already exists.");
                            } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
                                tVMsg.setText("Invalid credentials.");
                            } else if (exp instanceof FirebaseNetworkException) {
                                tVMsg.setText("Network error. Please check your connection.");
                            } else {
                                tVMsg.setText("An error occurred. Please try again later.");
                            }
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Boolean isChecked = sharedPref.getBoolean("stayConnect", false);
        if (refAuth.getCurrentUser() != null && isChecked) {
            FirebaseUser user = refAuth.getCurrentUser();
            Intent si = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(si);
            finish();
        }
    }
}
