package com.example.mad_project.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mad_project.MainActivity;
import com.example.mad_project.R;
import com.example.mad_project.controller.UserController;
import com.example.mad_project.utils.SessionManager;
import com.example.mad_project.utils.Validation;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class OwnerRegistrationActivity extends MainActivity {
    private TextInputLayout companyNameLayout;
    private TextInputLayout registrationNumberLayout;
    private TextInputLayout taxNumberLayout;

    private TextInputEditText companyNameInput;
    private TextInputEditText registrationNumberInput;
    private TextInputEditText taxNumberInput;
    private MaterialButton registerButton;

    private UserController userController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_register_owner, contentFrame);
        setupNavigation(true, false, "Owner Registration");

        initializeViews();
        setupListeners();

        userController = new UserController(this);
    }

    private void initializeViews() {
        companyNameLayout = findViewById(R.id.companyNameLayout);
        registrationNumberLayout = findViewById(R.id.registrationNumberLayout);
        taxNumberLayout = findViewById(R.id.taxNumberLayout);

        companyNameInput = findViewById(R.id.companyNameInput);
        registrationNumberInput = findViewById(R.id.registrationNumberInput);
        taxNumberInput = findViewById(R.id.taxNumberInput);
        registerButton = findViewById(R.id.registerButton);
    }

    private void setupListeners() {
        registerButton.setOnClickListener(v -> attemptRegistration());
    }

    private void attemptRegistration() {
        String companyName = companyNameInput.getText().toString().trim();
        String registrationNumber = registrationNumberInput.getText().toString().trim();
        String taxId = taxNumberInput.getText().toString().trim();

        // Reset errors
        companyNameLayout.setError(null);
        registrationNumberLayout.setError(null);
        taxNumberLayout.setError(null);

        // Validate inputs
        if (companyName.isEmpty()) {
            companyNameLayout.setError("Company name is required");
            return;
        }

        if (!Validation.isValidBusinessRegistration(registrationNumber)) {
            registrationNumberLayout.setError("Please enter a valid registration number");
            return;
        }

        if (!Validation.isValidTaxId(taxId)) {
            taxNumberLayout.setError("Please enter a valid tax ID");
            return;
        }

        // Show loading state
        registerButton.setEnabled(false);
        registerButton.setText("Registering...");

        userController.upgradeToOwner(
            companyName, registrationNumber, taxId,
            success -> runOnUiThread(() -> {
                registerButton.setEnabled(true);
                registerButton.setText("Register");

                SessionManager sessionManager = new SessionManager(this);

                if (success) {
                    Toast.makeText(this, "Successfully registered as an owner", 
                        Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    sessionManager.updateRole("owner");
                    finishAffinity();
                } else {
                    Toast.makeText(this, "Registration failed. Business details may already be registered.", 
                        Toast.LENGTH_LONG).show();
                }
            })
        );
    }
}