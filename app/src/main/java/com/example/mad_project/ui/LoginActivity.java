package com.example.mad_project.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.mad_project.MainActivity;
import com.example.mad_project.R;
import com.example.mad_project.controller.UserController;
import com.example.mad_project.utils.Validation;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends MainActivity {

    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
    private MaterialButton registerButton;
    private UserController userController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_login, contentFrame);
        setupNavigation(false, false, "Login");
        
        // Check if we're switching users
        boolean isSwitchingUser = getIntent().getBooleanExtra("switching_user", false);
        
        // Only check logged in status if not switching users
        if (!isSwitchingUser && sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }

        initializeViews();
        userController = new UserController(this);
        setupListeners();
    }

    @Override
    protected void setupNavigation(boolean showBackButton, boolean showBottomNav, String title) {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        if (topAppBar != null) {
            topAppBar.setTitle(title);
            
            if (showBackButton) {
                topAppBar.setNavigationIcon(R.drawable.baseline_arrow_back_24);
                topAppBar.setNavigationOnClickListener(v -> onBackPressed());
            }
        }

        if (bottomNav != null) {
            bottomNav.setVisibility(showBottomNav ? View.VISIBLE : View.GONE);
            if (showBottomNav) {
                setupBottomNavigation();
            }
        }
    }

    private void initializeViews() {
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailInput = findViewById(R.id.editTextEmail);
        passwordInput = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.btnLogin);
        registerButton = findViewById(R.id.btnRegister);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());
        
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void attemptLogin() {
        // Reset errors
        emailLayout.setError(null);
        passwordLayout.setError(null);

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate input
        if (!Validation.isValidEmail(email)) {
            emailLayout.setError("Please enter a valid email address");
            return;
        }

        if (!Validation.isValidPassword(password)) {
            passwordLayout.setError("Password cannot be empty");
            return;
        }

        // Show loading state
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        userController.login(email, password, user -> {
            runOnUiThread(() -> {
                loginButton.setEnabled(true);
                loginButton.setText("Login");

                if (user != null) {
                    // Navigate to dashboard
                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this,
                        "Invalid email or password",
                        Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}