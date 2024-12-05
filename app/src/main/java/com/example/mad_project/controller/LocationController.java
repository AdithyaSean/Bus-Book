package com.example.mad_project.controller;

import android.content.Context;
import com.example.mad_project.data.AppDatabase;
import com.example.mad_project.data.Location;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LocationController {
    private AppDatabase db;
    private Executor executor;

    public LocationController(Context context) {
        this.db = AppDatabase.getDatabase(context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void searchLocations(String query, LocationCallback callback) {
        executor.execute(() -> {
            List<Location> locations = db.locationDao().searchLocations(query);
            callback.onLocationsLoaded(locations);
        });
    }

    public void getAllLocations(LocationCallback callback) {
        executor.execute(() -> {
            List<Location> locations = db.locationDao().getAllLocations();
            callback.onLocationsLoaded(locations);
        });
    }

    public void getLocationByName(String name, SingleLocationCallback callback) {
        executor.execute(() -> {
            Location location = db.locationDao().getLocationByName(name);
            callback.onLocationLoaded(location);
        });
    }

    public interface LocationCallback {
        void onLocationsLoaded(List<Location> locations);
    }

    public interface SingleLocationCallback {
        void onLocationLoaded(Location location);
    }
}
