package com.example.mad_project.utils;

import android.util.Log;
import android.util.Patterns;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class Validation {
    private static final int SALT_LENGTH = 16;
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String DELIMITER = ":";

    public static String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            digest.reset();
            digest.update(salt);
            byte[] hash = digest.digest(password.getBytes());

            return Base64.getEncoder().encodeToString(salt) +
                    DELIMITER +
                    Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            Log.e("Validation", "Error hashing password", e);
            return null;
        }
    }

    public static boolean verifyPassword(String password, String storedHash) throws NoSuchAlgorithmException {
        String[] parts = storedHash.split(DELIMITER);
        if (parts.length != 2) {
            return false;
        }

        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] hash = Base64.getDecoder().decode(parts[1]);

        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        digest.reset();
        digest.update(salt);
        byte[] newHash = digest.digest(password.getBytes());

        return MessageDigest.isEqual(hash, newHash);
    }

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.length() == 10 && phone.matches("\\d+");
    }

    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }

    public static boolean isValidLicenseNumber(String licenseNumber) {
        return licenseNumber != null && licenseNumber.matches("[A-Z]\\d{7}");
    }

    public static boolean isValidBusinessRegistration(String regNumber) {
        return regNumber != null && regNumber.matches("[A-Z]{2}\\d{5}");
    }

    public static boolean isValidTaxId(String taxId) {
        return taxId != null && taxId.matches("[A-Z]{3}\\d{7}");
    }

    public static boolean isValidExperience(int years) {
        return years >= 0 && years <= 50;
    }
}