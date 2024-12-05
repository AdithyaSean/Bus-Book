package com.example.mad_project.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.MainActivity;
import com.example.mad_project.R;
import com.example.mad_project.adapter.BusAdapter;
import com.example.mad_project.data.AppDatabase;
import com.example.mad_project.data.Bus;
import com.example.mad_project.utils.SessionManager;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriverBusesActivity extends MainActivity {
    private RecyclerView busRecyclerView;
    private TextView emptyView;
    private AppDatabase db;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_driver_buses, contentFrame);
        setupNavigation(true, false, "My Assigned Buses");

        initializeViews();
        db = AppDatabase.getDatabase(this);
        loadAssignedBuses();
    }

    private void initializeViews() {
        busRecyclerView = findViewById(R.id.busRecyclerView);
        emptyView = findViewById(R.id.emptyView);
        busRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadAssignedBuses() {
        SessionManager sessionManager = new SessionManager(this);
        int userId = sessionManager.getUserId();

        executor.execute(() -> {
            // Get driver ID from user ID
            int driverId = db.busDriverDao().getBusDriverByUserId(userId).getId();
            // Get assigned buses
            List<Bus> assignedBuses = db.busDao().getBusesByDriverId(driverId);
            
            runOnUiThread(() -> {
                if (assignedBuses.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    busRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    busRecyclerView.setVisibility(View.VISIBLE);
                    
                    BusAdapter adapter = new BusAdapter(assignedBuses, 
                        new BusAdapter.OnBusClickListener() {
                            @Override
                            public void onBusClick(Bus bus) {
                                // Launch SeatBookActivity in view-only mode for drivers
                                Intent intent = new Intent(DriverBusesActivity.this, SeatBookActivity.class);
                                intent.putExtra("bus_id", bus.getId());
                                intent.putExtra("is_driver_view", true);  // Add this flag
                                startActivity(intent);
                            }

                            @Override
                            public void onBookClick(Bus bus) {
                                // Not needed for driver view
                            }
                        }, false, true);  // Add isDriverView parameter
                    busRecyclerView.setAdapter(adapter);
                }
            });
        });
    }

    private void showBusDetails(Bus bus) {
        // Show bus details in a dialog or new activity
        // You can implement this based on your requirements
    }
}