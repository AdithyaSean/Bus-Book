package com.example.mad_project.ui;

import android.os.Bundle;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mad_project.ui.fragments.MyBusesFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.example.mad_project.MainActivity;
import com.example.mad_project.R;
import com.example.mad_project.adapter.BusManagementPagerAdapter;

public class ManageBusesActivity extends MainActivity {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_manage_buses, contentFrame);
        setupNavigation(true, false, "Manage Buses");

        initializeViews();
        setupViewPager();
    }

    private void initializeViews() {
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
    }

    private void setupViewPager() {
        BusManagementPagerAdapter pagerAdapter = new BusManagementPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Add Bus" : "My Buses");
        }).attach();
    }

    public void refreshMyBuses() {
        MyBusesFragment fragment = (MyBusesFragment)
            getSupportFragmentManager().findFragmentByTag("f" + 1);
        if (fragment != null) {
            fragment.loadOwnerBuses();
        }
    }
} 