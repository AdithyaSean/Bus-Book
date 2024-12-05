package com.example.mad_project.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_ROLE = "role";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_NAME = "name";
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setLogin(boolean isLoggedIn, String name, String email, int userId, String role, String image) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_IMAGE, image);
        editor.apply();
    }

    public String getUsername() {
        return sharedPreferences.getString(KEY_NAME, null);
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    public int getUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    public String getRole() {
        return sharedPreferences.getString(KEY_ROLE, null);
    }

    public String getImage() {
        return sharedPreferences.getString(KEY_IMAGE, null);
    }

    public void updateRole(String role) {
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }

    public void logoutKeepData() {
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_NAME);
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_ROLE);
        editor.remove(KEY_IMAGE);
        editor.apply();
    }

    public boolean isFirstLaunch() {
        return sharedPreferences.getBoolean("is_first_launch", true);
    }

    public void setFirstLaunchComplete() {
        editor.putBoolean("is_first_launch", false);
        editor.apply();
    }
}