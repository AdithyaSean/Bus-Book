package com.example.mad_project.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BusDriverDao {
    @Insert
    long insert(BusDriver busDriver);

    @Update
    void update(BusDriver busDriver);

    @Query("SELECT * FROM bus_drivers WHERE user_id = :userId AND is_active = 1")
    BusDriver getDriverByUserId(int userId);

    @Query("SELECT * FROM bus_drivers WHERE license_number = :licenseNumber AND is_active = 1")
    BusDriver getDriverByLicense(String licenseNumber);

    @Query("UPDATE bus_drivers SET is_active = 0 WHERE id = :driverId")
    void deactivateDriver(int driverId);

    @Query("SELECT EXISTS(SELECT 1 FROM bus_drivers WHERE license_number = :licenseNumber)")
    boolean isLicenseExists(String licenseNumber);

    @Query("SELECT * FROM bus_drivers WHERE is_active = 1")
    List<BusDriver> getAllActiveDrivers();

    @Query("SELECT * FROM bus_drivers WHERE user_id = :userId")
    BusDriver getBusDriverByUserId(int userId);
}