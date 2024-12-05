package com.example.mad_project.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mad_project.MainActivity;
import com.example.mad_project.R;
import com.example.mad_project.controller.UserController;
import com.example.mad_project.utils.ImageUtils;
import com.example.mad_project.utils.Validation;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import android.os.Build;
import androidx.annotation.NonNull;

public class EditProfileActivity extends MainActivity {
    
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneInput;
    private MaterialButton saveButton;
    private MaterialButton changePhotoButton;
    private ShapeableImageView profileImage;
    private String currentPhotoPath;
    private UserController userController;
    
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri selectedImage = result.getData().getData();
                if (selectedImage != null) {
                    handleImageResult(selectedImage);
                }
            }
        }
    );

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Bundle extras = result.getData().getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null) {
                        handleImageResult(ImageUtils.getImageUriFromBitmap(this, imageBitmap));
                    }
                }
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_edit_profile, contentFrame);
        setupNavigation(true, false, "Edit Profile");

        userController = new UserController(this);
        
        initializeViews();
        setupListeners();
        loadUserData();
    }

    private void initializeViews() {
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        saveButton = findViewById(R.id.saveButton);
        changePhotoButton = findViewById(R.id.changePhotoButton);
        profileImage = findViewById(R.id.profileImage);
    }

    private void setupListeners() {
        saveButton.setOnClickListener(v -> saveChanges());
        changePhotoButton.setOnClickListener(v -> showImagePickerDialog());
    }

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        new MaterialAlertDialogBuilder(this)
            .setTitle("Choose Profile Picture")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    checkCameraPermissionAndLaunch();
                } else if (which == 1) {
                    checkGalleryPermissionAndLaunch();
                }
            })
            .show();
    }

    private void checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                100);
        } else {
            launchCamera();
        }
    }

    private void checkGalleryPermissionAndLaunch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 and above
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    101);
            } else {
                launchGallery();
            }
        } else {
            // For Android 12 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    101);
            } else {
                launchGallery();
            }
        }
    }

    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(takePictureIntent);
        }
    }

    private void launchGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void showLoadingState(boolean isLoading) {
        changePhotoButton.setEnabled(!isLoading);
        saveButton.setEnabled(!isLoading);
        if (isLoading) {
            changePhotoButton.setText("Updating...");
        } else {
            changePhotoButton.setText("Change Photo");
        }
    }

    private void handleImageResult(Uri imageUri) {
        if (imageUri == null) return;
        
        showLoadingState(true);
        String savedImagePath = ImageUtils.saveImageToInternalStorage(this, imageUri);
        
        if (savedImagePath == null) {
            showLoadingState(false);
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = sessionManager.getUserId();
        userController.getUserById(userId, user -> {
            if (user != null) {
                user.setImage(savedImagePath);
                userController.update(user, success -> runOnUiThread(() -> {
                    showLoadingState(false);
                    if (success) {
                        sessionManager.setLogin(true, user.getName(), user.getEmail(), user.getId(),
                            user.getRole(), savedImagePath);
                        ImageUtils.loadProfileImage(this, profileImage, savedImagePath);
                        Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to update profile picture", 
                            Toast.LENGTH_SHORT).show();
                    }
                }));
            }
        });
    }

    private void loadUserData() {
        int userId = sessionManager.getUserId();
        if (userId != -1) {
            userController.getUserById(userId, user -> runOnUiThread(() -> {
                if (user != null) {
                    nameInput.setText(user.getName());
                    emailInput.setText(user.getEmail());
                    phoneInput.setText(user.getPhone());
                    ImageUtils.loadProfileImage(this, profileImage, sessionManager.getImage());
                }
            }));
        }
    }

    private void saveChanges() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        if (!validateInputs(name, email, phone)) return;

        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        int userId = sessionManager.getUserId();
        userController.getUserById(userId, user -> {
            if (user != null) {
                user.setName(name);
                user.setEmail(email);
                user.setPhone(phone);
                
                userController.update(user, success -> runOnUiThread(() -> {
                    saveButton.setEnabled(true);
                    saveButton.setText("Save Changes");

                    if (success) {
                        Toast.makeText(this, "Profile updated successfully", 
                            Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update profile. Email might be taken.", 
                            Toast.LENGTH_SHORT).show();
                    }
                }));
            }
        });
    }

    private boolean validateInputs(String name, String email, String phone) {
        if (!Validation.isValidName(name)) {
            nameInput.setError("Name is required");
            return false;
        }
        if (!Validation.isValidEmail(email)) {
            emailInput.setError("Invalid email format");
            return false;
        }
        if (!Validation.isValidPhone(phone)) {
            phoneInput.setError("Invalid phone number");
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {  // Gallery permission
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchGallery();
            } else {
                Toast.makeText(this, "Storage permission is required to select photos", Toast.LENGTH_SHORT).show();
            }
        }
    }
} 