package com.example.mad_project.ui;

import android.os.Bundle;

import com.example.mad_project.MainActivity;
import com.example.mad_project.R;
import com.example.mad_project.ui.fragments.AddBusFragment;

public class ManageBusActivity extends MainActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_manage_bus, contentFrame);
        setupNavigation(true, false, "Manage Buses");
        
        boolean isEditMode = getIntent().getBooleanExtra("EDIT_MODE", false);
        setTitle(isEditMode ? "Edit Bus" : "Add New Bus");
        
        AddBusFragment fragment = new AddBusFragment();
        fragment.setArguments(getIntent().getExtras());
        
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit();
    }
} 