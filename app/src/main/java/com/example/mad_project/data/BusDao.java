package com.example.mad_project.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BusDao {
    @Insert
    long insert(Bus bus);

    @Update
    void update(Bus bus);

    @Query("SELECT * FROM buses WHERE is_active = 1")
    List<Bus> getAllBuses();

    @Query("SELECT * FROM buses WHERE " +
           "LOWER(route_from) LIKE LOWER(:from) || '%' AND " +
           "LOWER(route_to) LIKE LOWER(:to) || '%' AND " +
           "is_active = 1")
    List<Bus> getBusesByRoute(String from, String to);

    @Query("SELECT DISTINCT route_from FROM buses WHERE route_from LIKE :query")
    List<String> getFromLocations(String query);

    @Query("SELECT DISTINCT route_to FROM buses WHERE route_to LIKE :query")
    List<String> getToLocations(String query);

    @Query("SELECT * FROM buses WHERE id = :busId")
    Bus getBusById(int busId);

    @Query("SELECT * FROM buses WHERE registration_number = :regNumber AND is_active = 1")
    Bus getBusByRegistration(String regNumber);

    @Query("SELECT * FROM buses WHERE owner_id = :ownerId AND is_active = 1")
    List<Bus> getBusesByOwnerId(int ownerId);

    @Query("UPDATE buses SET driver_id = :driverId, updated_at = :timestamp WHERE id = :busId")
    void assignDriver(int busId, Integer driverId, long timestamp);

    @Query("SELECT * FROM buses WHERE driver_id = :driverId AND is_active = 1")
    List<Bus> getBusesByDriverId(int driverId);

    @Query("SELECT * FROM buses WHERE driver_id IS NULL AND is_active = 1")
    List<Bus> getBusesWithoutDriver();

    @Query("UPDATE buses SET is_active = :isActive, updated_at = :timestamp WHERE id = :busId")
    void updateBusActiveStatus(int busId, boolean isActive, long timestamp);

    @Query("DELETE FROM buses WHERE id = :busId")
    void deleteById(int busId);
}
