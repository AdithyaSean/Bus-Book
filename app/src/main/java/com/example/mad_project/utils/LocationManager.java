package com.example.mad_project.utils;

import com.example.mad_project.data.Location;
import java.util.ArrayList;
import java.util.List;

public class LocationManager {
    private static final List<Location> locations = new ArrayList<>();

    static {
        // Major cities in Sri Lanka with their coordinates
        locations.add(new Location("Colombo", 6.927079, 79.861243));
        locations.add(new Location("Kandy", 7.290572, 80.633728));
        locations.add(new Location("Galle", 6.053519, 80.220978));
        locations.add(new Location("Jaffna", 9.661302, 80.025513));
        locations.add(new Location("Anuradhapura", 8.311338, 80.403656));
        locations.add(new Location("Batticaloa", 7.717935, 81.700088));
        locations.add(new Location("Trincomalee", 8.578132, 81.233040));
        locations.add(new Location("Negombo", 7.189464, 79.858734));
        locations.add(new Location("Matara", 5.948853, 80.535888));
        locations.add(new Location("Kurunegala", 7.486842, 80.362439));
    }

    public static List<Location> getAllLocations() {
        return locations;
    }

    public static Location getLocationByName(String name) {
        return locations.stream()
                .filter(location -> location.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
