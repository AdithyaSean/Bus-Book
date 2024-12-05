package com.example.mad_project.controller;

import android.content.Context;
import com.example.mad_project.data.AppDatabase;
import com.example.mad_project.data.Bus;
import com.example.mad_project.data.BusDao;
import androidx.room.Room;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class BusController {
    private final AppDatabase db;
    private final BusDao busDao;
    private final ExecutorService executorService;

    public BusController(Context context) {
        db = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "mad_project_db")
                .fallbackToDestructiveMigration()
                .build();
        busDao = db.busDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void getRouteSuggestions(String query, Consumer<List<String>> callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<String> suggestions = new ArrayList<>();
            
            // Get unique 'from' locations
            List<String> fromLocations = busDao.getFromLocations("%" + query + "%");
            // Get unique 'to' locations
            List<String> toLocations = busDao.getToLocations("%" + query + "%");
            
            // Combine and remove duplicates
            suggestions.addAll(fromLocations);
            suggestions.addAll(toLocations);
            suggestions = new ArrayList<>(new LinkedHashSet<>(suggestions));
            
            callback.accept(suggestions);
        });
        executor.shutdown();
    }

    public void searchBuses(String fromLocation, String toLocation, Consumer<List<Bus>> callback) {
        executorService.execute(() -> {
            List<Bus> buses;
            if (hasActiveFilters(fromLocation, toLocation)) {
                buses = busDao.getBusesByRoute(fromLocation, toLocation);
            } else {
                buses = busDao.getAllBuses();
            }
            callback.accept(buses);
        });
    }

    public boolean hasActiveFilters(String fromLocation, String toLocation) {
        return fromLocation != null && !fromLocation.isEmpty() 
            && toLocation != null && !toLocation.isEmpty();
    }

    public void updateBusRating(Bus bus, float newRating) {
        executorService.execute(() -> {
            float currentRating = bus.getRating();
            int currentCount = bus.getRatingCount();
            
            // Calculate new average rating
            float updatedRating = ((currentRating * currentCount) + newRating) / (currentCount + 1);
            
            bus.setRating(updatedRating);
            bus.setRatingCount(currentCount + 1);
            
            db.busDao().update(bus);
        });
    }
}