package com.example.mad_project.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.MainActivity;
import com.example.mad_project.R;
import com.example.mad_project.adapter.BusAdapter;
import com.example.mad_project.controller.BusController;
import com.example.mad_project.data.AppDatabase;
import com.example.mad_project.data.Bus;
import com.example.mad_project.utils.DirectionsHandler;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import android.widget.TextView;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BusActivity extends MainActivity implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap mMap;
    private RecyclerView busRecyclerView;
    private BusController busController;
    private BottomSheetBehavior<NestedScrollView> bottomSheetBehavior;
    private String fromLocation;
    private String toLocation;
    private MaterialButton clearFiltersButton;
    private DirectionsHandler directionsHandler;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_bus, contentFrame);
        setupNavigation(true, true, "Bus Details");

        // Initialize database
        db = AppDatabase.getDatabase(this);
        
        // Initialize views and map
        mapView = findViewById(R.id.map);
        busRecyclerView = findViewById(R.id.busRecyclerView);
        busRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Initialize bottom sheet
        NestedScrollView bottomSheet = findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        
        // Get route filters from intent
        fromLocation = getIntent().getStringExtra("from_location");
        toLocation = getIntent().getStringExtra("to_location");
        
        // Setup map
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        
        busController = new BusController(this);
        clearFiltersButton = findViewById(R.id.clearFiltersButton);
        clearFiltersButton.setOnClickListener(v -> clearFilters());
        
        // Update clear filters button visibility based on intent extras
        updateClearFiltersVisibility();
        
        // Load buses
        setupBusList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private void setupBusList() {
        busController.searchBuses(fromLocation, toLocation, buses -> {
            runOnUiThread(() -> {
                BusAdapter adapter = new BusAdapter(buses, new BusAdapter.OnBusClickListener() {
                    @Override
                    public void onBusClick(Bus bus) {
                        // Collapse bottom sheet to peek height
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        
                        // Clear previous routes
                        mMap.clear();
                        
                        // Add bus marker and display route
                        LatLng busLocation = new LatLng(bus.getLatitude(), bus.getLongitude());
                        mMap.addMarker(new MarkerOptions()
                                .position(busLocation)
                                .title(bus.getRegistrationNumber())
                                .snippet(String.format("%s to %s", bus.getRouteFrom(), bus.getRouteTo()))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        
                        directionsHandler.displayRoute(bus.getRouteFrom(), bus.getRouteTo());
                        
                        try {
                            LatLngBounds routeBounds = directionsHandler.getRouteBounds();
                            if (routeBounds != null) {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(routeBounds, 100));
                            } else {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(busLocation, 12f));
                            }
                        } catch (Exception e) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(busLocation, 12f));
                        }
                    }

                    @Override
                    public void onBookClick(Bus bus) {
                        startBookingProcess(bus);
                    }
                }, false, false);  // false for passenger view, false for driver view
                busRecyclerView.setAdapter(adapter);
                
                // Update title
                TextView titleText = findViewById(R.id.availableBusesTitle);
                if (busController.hasActiveFilters(fromLocation, toLocation)) {
                    titleText.setText(String.format("Buses from %s to %s (%d available)", 
                        fromLocation, toLocation, buses.size()));
                } else {
                    titleText.setText(String.format("Available Buses (%d)", buses.size()));
                }
                clearFiltersButton.setVisibility(
                    busController.hasActiveFilters(fromLocation, toLocation) ? View.VISIBLE : View.GONE
                );
                
                // Update map markers
                if (mMap != null) {
                    updateMapMarkers(buses);
                }
            });
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Initial map setup
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        
        // Initialize DirectionsHandler
        directionsHandler = new DirectionsHandler(mMap, this, getString(R.string.MAPS_API_KEY));
        
        // Load buses again to add markers
        setupBusList();
    }

    private void updateMapMarkers(List<Bus> buses) {
        mMap.clear();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        
        // Add bus markers
        for (Bus bus : buses) {
            LatLng busLocation = new LatLng(bus.getLatitude(), bus.getLongitude());
            builder.include(busLocation);
            
            float markerColor;
            switch (bus.getOwnerId()) {
                case 1:
                    markerColor = BitmapDescriptorFactory.HUE_RED;
                    break;
                case 2:
                    markerColor = BitmapDescriptorFactory.HUE_BLUE;
                    break;
                case 3:
                    markerColor = BitmapDescriptorFactory.HUE_GREEN;
                    break;
                default:
                    markerColor = BitmapDescriptorFactory.HUE_YELLOW;
            }
            
            mMap.addMarker(new MarkerOptions()
                    .position(busLocation)
                    .title(bus.getRegistrationNumber())
                    .snippet(String.format("%s to %s", bus.getRouteFrom(), bus.getRouteTo()))
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
        }

        // Draw route if filters are applied
        if (fromLocation != null && toLocation != null && !buses.isEmpty()) {
            directionsHandler.displayRoute(fromLocation, toLocation);
        }

        // Zoom to show all markers
        if (!buses.isEmpty()) {
            LatLngBounds bounds = builder.build();
            int padding = 100;
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cu);
        }
    }

    private void startBookingProcess(Bus bus) {
        Intent intent = new Intent(this, SeatBookActivity.class);
        intent.putExtra("bus_id", bus.getId());
        startActivity(intent);
    }

    private void clearFilters() {
        fromLocation = null;
        toLocation = null;
        
        // Reload buses without filters
        setupBusList();
        
        // Clear any existing routes on the map
        if (mMap != null) {
            mMap.clear();
        }
    }

    private void updateClearFiltersVisibility() {
        clearFiltersButton.setVisibility(
            busController.hasActiveFilters(fromLocation, toLocation) ? 
            View.VISIBLE : View.GONE
        );
    }
}