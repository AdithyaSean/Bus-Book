package com.example.mad_project.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mad_project.MainActivity;
import com.example.mad_project.R;
import com.example.mad_project.controller.UserController;
import com.example.mad_project.utils.Validation;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.button.MaterialButton;

import java.util.Objects;

public class RegisterActivity extends MainActivity {

    private TextInputLayout nameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout phoneLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPasswordLayout;
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private MaterialButton registerButton;
    private TextView loginButton;
    private UserController userController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_register, contentFrame);

        initializeViews();
        setupClickListeners();
        setupNavigation(false, false, "Register");

        userController = new UserController(this);
    }

    private void initializeViews() {
        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        phoneLayout = findViewById(R.id.phoneLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);

        registerButton = findViewById(R.id.registerButton);
        loginButton = findViewById(R.id.loginButton);
    }

    private void setupClickListeners() {
        registerButton.setOnClickListener(v -> handleRegistration());
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean validateInputs() {
        String name = Objects.requireNonNull(nameInput.getText()).toString().trim();
        String email = Objects.requireNonNull(emailInput.getText()).toString().trim();
        String phone = Objects.requireNonNull(phoneInput.getText()).toString().trim();
        String password = Objects.requireNonNull(passwordInput.getText()).toString();
        String confirmPassword = Objects.requireNonNull(confirmPasswordInput.getText()).toString();

        boolean isValid = true;

        if (!Validation.isValidName(name)) {
            nameLayout.setError("Name is required");
            nameLayout.setBoxStrokeColor(Color.RED);
            isValid = false;
        } else {
            nameLayout.setError(null);
            nameLayout.setBoxStrokeColor(Color.WHITE);
        }

        if (!Validation.isValidEmail(email)) {
            emailLayout.setError("Invalid email format");
            emailLayout.setBoxStrokeColor(Color.RED);
            isValid = false;
        } else {
            emailLayout.setError(null);
            emailLayout.setBoxStrokeColor(Color.WHITE);
        }

        if (!Validation.isValidPhone(phone)) {
            phoneLayout.setError("Phone number must be 10 digits");
            phoneLayout.setBoxStrokeColor(Color.RED);
            isValid = false;
        } else {
            phoneLayout.setError(null);
            phoneLayout.setBoxStrokeColor(Color.WHITE);
        }

        if (!Validation.isValidPassword(password)) {
            passwordLayout.setError("Password must be at least 6 characters");
            passwordLayout.setBoxStrokeColor(Color.RED);
            isValid = false;
        } else {
            passwordLayout.setError(null);
            passwordLayout.setBoxStrokeColor(Color.WHITE);
        }

        if (!Validation.doPasswordsMatch(password, confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            confirmPasswordLayout.setBoxStrokeColor(Color.RED);
            isValid = false;
        } else {
            confirmPasswordLayout.setError(null);
            confirmPasswordLayout.setBoxStrokeColor(Color.WHITE);
        }

        if (!isValid) {
            Toast.makeText(this, "Please fix the errors before registering", Toast.LENGTH_SHORT).show();
        }

        return isValid;
    }

    private void handleRegistration() {
        if (validateInputs()) {
            String name = Objects.requireNonNull(nameInput.getText()).toString().trim();
            String email = Objects.requireNonNull(emailInput.getText()).toString().trim();
            String phone = Objects.requireNonNull(phoneInput.getText()).toString().trim();
            String password = Objects.requireNonNull(passwordInput.getText()).toString().trim();

            Toast.makeText(this, "Processing registration...", Toast.LENGTH_SHORT).show();

            userController.register(name, email, phone, password, "user", success -> runOnUiThread(() -> {
                if (success != null) {
                    Toast.makeText(RegisterActivity.this, "Registration successful! Please login.", Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed. Email might already be registered.", Toast.LENGTH_SHORT).show();
                }
            }));
        }
    }

    protected void redirectToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
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
}