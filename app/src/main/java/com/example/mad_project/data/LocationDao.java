package com.example.mad_project.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface LocationDao {
    @Insert
    long insert(Location location);

    @Update
    void update(Location location);

    @Query("SELECT * FROM locations WHERE name LIKE '%' || :query || '%'")
    List<Location> searchLocations(String query);

    @Query("SELECT * FROM locations")
    List<Location> getAllLocations();

    @Query("SELECT * FROM locations WHERE id = :locationId")
    Location getLocationById(int locationId);

    @Query("SELECT * FROM locations WHERE name = :name")
    Location getLocationByName(String name);

    @Query("DELETE FROM locations")
    void deleteAllLocations();
}
