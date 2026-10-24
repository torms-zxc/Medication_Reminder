package com.example;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.database.UserDatabaseHelper;
import com.example.model.UserModel;
import com.example.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private boolean isLoginMode = true;

    private MaterialButtonToggleGroup toggleAuthMode;
    private TextView tvFormTitle, tvFormSubtitle;
    private TextInputLayout tilFullName, tilUserId, tilPassword, tilConfirmPassword;
    private TextInputEditText etFullName, etUserId, etPassword, etConfirmPassword;
    private MaterialButton btnSubmitAuth;

    private UserDatabaseHelper userDbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userDbHelper = new UserDatabaseHelper(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        toggleAuthMode = findViewById(R.id.toggleAuthMode);
        tvFormTitle = findViewById(R.id.tvFormTitle);
        tvFormSubtitle = findViewById(R.id.tvFormSubtitle);

        tilFullName = findViewById(R.id.tilFullName);
        tilUserId = findViewById(R.id.tilUserId);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etFullName = findViewById(R.id.etFullName);
        etUserId = findViewById(R.id.etUserId);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnSubmitAuth = findViewById(R.id.btnSubmitAuth);
    }

    private void setupListeners() {
        toggleAuthMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnTabLogin) {
                    switchToLoginMode();
                } else if (checkedId == R.id.btnTabRegister) {
                    switchToRegisterMode();
                }
            }
        });

        btnSubmitAuth.setOnClickListener(v -> {
            if (isLoginMode) {
                handleLogin();
            } else {
                handleRegister();
            }
        });
    }

    private void switchToLoginMode() {
        isLoginMode = true;
        tvFormTitle.setText(R.string.login_title);
        tvFormSubtitle.setText(R.string.login_subtitle);
        tilFullName.setVisibility(View.GONE);
        tilConfirmPassword.setVisibility(View.GONE);
        btnSubmitAuth.setText(R.string.sign_in);
        clearErrors();
    }

    private void switchToRegisterMode() {
        isLoginMode = false;
        tvFormTitle.setText(R.string.register_title);
        tvFormSubtitle.setText(R.string.register_subtitle);
        tilFullName.setVisibility(View.VISIBLE);
        tilConfirmPassword.setVisibility(View.VISIBLE);
        btnSubmitAuth.setText(R.string.create_account);
        clearErrors();
    }

    private void clearErrors() {
        tilFullName.setError(null);
        tilUserId.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    private void handleLogin() {
        clearErrors();
        String userId = etUserId.getText() != null ? etUserId.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        if (userId.isEmpty()) {
            tilUserId.setError("Please enter your User ID or Username");
            etUserId.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            tilPassword.setError("Please enter your password");
            etPassword.requestFocus();
            return;
        }

        // Check SQLite Database
        boolean isValid = userDbHelper.checkUserCredentials(userId, password);
        if (isValid) {
            UserModel user = userDbHelper.getUser(userId);
            String displayName = (user != null && user.getFullName() != null) ? user.getFullName() : userId;
            sessionManager.createLoginSession(userId.toLowerCase(), displayName);

            Toast.makeText(this, "Welcome back, " + displayName + "!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Check if user exists to give helpful guidance
            if (!userDbHelper.isUserExists(userId)) {
                tilUserId.setError("User ID not found in database. Tap Register to create an account.");
            } else {
                tilPassword.setError("Incorrect password. Please try again.");
            }
        }
    }

    private void handleRegister() {
        clearErrors();
        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String userId = etUserId.getText() != null ? etUserId.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        if (fullName.isEmpty()) {
            tilFullName.setError("Please enter your full name");
            etFullName.requestFocus();
            return;
        }

        if (userId.isEmpty()) {
            tilUserId.setError("Please choose a User ID or Username");
            etUserId.requestFocus();
            return;
        }

        if (userId.length() < 3) {
            tilUserId.setError("User ID must be at least 3 characters");
            etUserId.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            tilPassword.setError("Please enter a password");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 4) {
            tilPassword.setError("Password must be at least 4 characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Check if already exists in SQLite
        if (userDbHelper.isUserExists(userId)) {
            tilUserId.setError("User ID is already registered. Please choose another or Sign In.");
            return;
        }

        // Save to SQLite
        boolean registered = userDbHelper.registerUser(userId, password, fullName);
        if (registered) {
            Toast.makeText(this, "Account registered successfully! Logging you in...", Toast.LENGTH_SHORT).show();
            sessionManager.createLoginSession(userId.toLowerCase(), fullName);

            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Please check your inputs.", Toast.LENGTH_LONG).show();
        }
    }
}
