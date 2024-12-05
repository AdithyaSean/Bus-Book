package com.example.mad_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mad_project.config.Config;
import com.example.mad_project.ui.BusActivity;
import com.example.mad_project.ui.DashboardActivity;
import com.example.mad_project.ui.LoginActivity;
import com.example.mad_project.ui.NotificationActivity;
import com.example.mad_project.ui.ProfileActivity;
import com.example.mad_project.ui.RegisterActivity;
import com.example.mad_project.ui.TicketsActivity;
import com.example.mad_project.ui.SettingsActivity;
import com.example.mad_project.ui.WelcomeActivity;
import com.example.mad_project.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    protected FrameLayout contentFrame;
    protected BottomNavigationView bottomNav;
    protected SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Config.initialize(this);
        
        sessionManager = new SessionManager(this);
        
        contentFrame = findViewById(R.id.content_frame);
        bottomNav = findViewById(R.id.bottomNavigationView);
        
        // Check if we're switching users
        boolean isSwitchingUser = getIntent().getBooleanExtra("switching_user", false);
        
        // First check if it's first launch
        if (sessionManager.isFirstLaunch()) {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }
        
        // Check if this is LoginActivity or RegisterActivity
        if (this instanceof LoginActivity || this instanceof RegisterActivity) {
            return;
        }
        
        // Then check login status (skip if switching users)
        if (!isSwitchingUser && !sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        // Only redirect to Dashboard if this is the main activity itself
        if (getClass() == MainActivity.class) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        }
    }

    protected void setupNavigation(boolean showBackButton, boolean showBottomNav, String title) {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        if (topAppBar != null) {
            topAppBar.setTitle(title);
            setSupportActionBar(topAppBar);
            
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

    protected void setupBottomNavigation() {
        if (bottomNav != null) {
            // Set the current item based on activity first
            if (this instanceof DashboardActivity) {
                bottomNav.setSelectedItemId(R.id.navigation_home);
            } else if (this instanceof TicketsActivity) {
                bottomNav.setSelectedItemId(R.id.navigation_tickets);
            } else if (this instanceof BusActivity) {
                bottomNav.setSelectedItemId(R.id.navigation_buses);
            } else if (this instanceof ProfileActivity) {
                bottomNav.setSelectedItemId(R.id.navigation_profile);
            }

            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                Intent intent = null;

                if (itemId == R.id.navigation_home && !(this instanceof DashboardActivity)) {
                    intent = new Intent(this, DashboardActivity.class);
                } else if (itemId == R.id.navigation_tickets && !(this instanceof TicketsActivity)) {
                    intent = new Intent(this, TicketsActivity.class);
                } else if (itemId == R.id.navigation_buses && !(this instanceof BusActivity)) {
                    intent = new Intent(this, BusActivity.class);
                } else if (itemId == R.id.navigation_profile && !(this instanceof ProfileActivity)) {
                    intent = new Intent(this, ProfileActivity.class);
                }

                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                }
                return false;
            });
        }
    }

    protected void redirectToLogin() {
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    protected void redirectToDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_notifications) {
            startActivity(new Intent(this, NotificationActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
}