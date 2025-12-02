package com.example.finalapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class  RegisterActivity extends AppCompatActivity {

    private FirebaseAuth refAuth;
    private TextInputEditText etEmail, etUsername, etPassword, etConfirmPassword;
    private TextView tvMsg;
    private MaterialButton btnRegister;
    private CheckBox cbStayConnect;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase
        refAuth = FirebaseAuth.getInstance();

        // SharedPreferences
        sharedPref = getSharedPreferences("MyPref", MODE_PRIVATE);

        // חיבור אל רכיבי ה־XML
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        cbStayConnect = findViewById(R.id.cbStayConnect);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink); // כאן

        // TextView להצגת הודעות
        tvMsg = new TextView(this);
        tvMsg.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        ((androidx.cardview.widget.CardView)findViewById(R.id.cardRegister)).addView(tvMsg);

        // כפתור הרשמה
        btnRegister.setOnClickListener(v -> createUser());

        // כפתור למעבר למסך Login
        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // סוגר את עמוד ההרשמה
        });
    }

    private void createUser() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            tvMsg.setText("Please fill all fields");
            return;
        }

        if (!pass.equals(confirmPass)) {
            tvMsg.setText("Passwords do not match");
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Connecting");
        pd.setMessage("Creating user...");
        pd.show();

        refAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        pd.dismiss();
                        if (task.isSuccessful()) {
                            Log.i("Register", "createUserWithEmailAndPassword:success");
                            FirebaseUser user = refAuth.getCurrentUser();
                            tvMsg.setText("User created successfully\nUid: " + user.getUid());
                            Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                            // שמור מצב stay connected
                            boolean stayConnected = cbStayConnect.isChecked();
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean("stayConnect", stayConnected);
                            editor.apply();

                            // המשך ל-MainActivity
                            Intent si = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(si);
                            finish();
                        } else {
                            Exception exp = task.getException();
                            if (exp instanceof FirebaseAuthInvalidUserException) {
                                tvMsg.setText("Invalid email address.");
                            } else if (exp instanceof FirebaseAuthWeakPasswordException) {
                                tvMsg.setText("Password too weak.");
                            } else if (exp instanceof FirebaseAuthUserCollisionException) {
                                tvMsg.setText("User already exists.");
                            } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
                                tvMsg.setText("Invalid credentials.");
                            } else if (exp instanceof FirebaseNetworkException) {
                                tvMsg.setText("Network error. Please check your connection.");
                            } else {
                                tvMsg.setText("An error occurred. Please try again later.");
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
            Intent si = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(si);
            finish();
        }
    }
}
