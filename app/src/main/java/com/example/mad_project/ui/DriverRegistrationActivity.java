package com.example.mad_project.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mad_project.MainActivity;
import com.example.mad_project.R;
import com.example.mad_project.controller.UserController;
import com.example.mad_project.utils.Validation;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DriverRegistrationActivity extends MainActivity {
    private TextInputLayout licenseNumberLayout;
    private TextInputLayout experienceLayout;
    private TextInputLayout expiryDateLayout;

    private TextInputEditText licenseNumberInput;
    private TextInputEditText experienceInput;
    private TextInputEditText expiryDateInput;
    private MaterialButton registerButton;

    private UserController userController;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_register_driver, contentFrame);
        setupNavigation(true, false, "Driver Registration");

        initializeViews();
        setupDatePicker();
        setupListeners();

        userController = new UserController(this);
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    private void initializeViews() {
        licenseNumberLayout = findViewById(R.id.licenseNumberLayout);
        experienceLayout = findViewById(R.id.experienceLayout);
        expiryDateLayout = findViewById(R.id.expiryDateLayout);

        licenseNumberInput = findViewById(R.id.licenseNumberInput);
        experienceInput = findViewById(R.id.experienceInput);
        expiryDateInput = findViewById(R.id.expiryDateInput);
        registerButton = findViewById(R.id.registerButton);
    }

    private void setupDatePicker() {
        expiryDateInput.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    expiryDateInput.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
    }

    private void setupListeners() {
        registerButton.setOnClickListener(v -> attemptRegistration());
    }

    private void attemptRegistration() {
        String licenseNumber = licenseNumberInput.getText().toString().trim();
        String experience = experienceInput.getText().toString().trim();
        String expiryDate = expiryDateInput.getText().toString().trim();

        // Reset errors
        licenseNumberLayout.setError(null);
        experienceLayout.setError(null);
        expiryDateLayout.setError(null);

        // Validate inputs
        if (!Validation.isValidLicenseNumber(licenseNumber)) {
            licenseNumberLayout.setError("Please enter a valid license number (e.g., B1234567)");
            return;
        }

        if (expiryDate.isEmpty()) {
            expiryDateLayout.setError("License expiry date is required");
            return;
        }

        int yearsExperience;
        try {
            yearsExperience = Integer.parseInt(experience);
            if (!Validation.isValidExperience(yearsExperience)) {
                experienceLayout.setError("Years of experience must be between 0 and 50");
                return;
            }
        } catch (NumberFormatException e) {
            experienceLayout.setError("Please enter a valid number");
            return;
        }

        // Show loading state
        registerButton.setEnabled(false);
        registerButton.setText("Registering...");

        userController.upgradeToDriver(
            licenseNumber, calendar.getTimeInMillis(), yearsExperience,
            success -> runOnUiThread(() -> {
                registerButton.setEnabled(true);
                registerButton.setText("Register");

                if (success) {
                    Toast.makeText(this, "Successfully registered as a driver", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    sessionManager.updateRole("driver");
                    finishAffinity();
                } else {
                    Toast.makeText(this, "Registration failed. License number may already be registered.", 
                        Toast.LENGTH_LONG).show();
                }
            })
        );
    }
}