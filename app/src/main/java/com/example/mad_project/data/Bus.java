package com.example.mad_project.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "buses",
        foreignKeys = {
                @ForeignKey(entity = BusOwner.class,
                        parentColumns = "id",
                        childColumns = "owner_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = BusDriver.class,
                        parentColumns = "id",
                        childColumns = "driver_id",
                        onDelete = ForeignKey.SET_NULL)
        },
        indices = {
                @Index(value = "owner_id"),
                @Index(value = "driver_id")
        })
public class Bus {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "owner_id")
    private int ownerId;

    @ColumnInfo(name = "driver_id", defaultValue = "NULL")
    private Integer driverId;

    @ColumnInfo(name = "registration_number")
    private String registrationNumber;

    @ColumnInfo(name = "model")
    private String model;

    @ColumnInfo(name = "total_seats")
    private int totalSeats;

    @ColumnInfo(name = "amenities")
    private String amenities;

    @ColumnInfo(name = "is_active")
    private boolean isActive;

    @ColumnInfo(name = "route_from")
    private String routeFrom;

    @ColumnInfo(name = "route_to")
    private String routeTo;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    @ColumnInfo(name = "latitude")
    private double latitude;

    @ColumnInfo(name = "longitude")
    private double longitude;

    @ColumnInfo(name = "departure_time")
    private long departureTime;

    @ColumnInfo(name = "arrival_time")
    private long arrivalTime;

    @ColumnInfo(name = "base_points")
    private int basePoints;

    @ColumnInfo(name = "rating")
    private float rating = 0.0f;

    @ColumnInfo(name = "rating_count")
    private int ratingCount = 0;

    public Bus(int ownerId, Integer driverId, String registrationNumber, String model, int totalSeats, String amenities, boolean isActive, String routeFrom, String routeTo, double latitude, double longitude, long departureTime, long arrivalTime, int basePoints) {
        this.ownerId = ownerId;
        this.driverId = driverId;
        this.registrationNumber = registrationNumber;
        this.model = model;
        this.totalSeats = totalSeats;
        this.amenities = amenities;
        this.isActive = isActive;
        this.routeFrom = routeFrom;
        this.routeTo = routeTo;
        this.latitude = latitude;
        this.longitude = longitude;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.basePoints = basePoints;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public Integer getDriverId() {
        return driverId;
    }

    public void setDriverId(Integer driverId) {
        this.driverId = driverId;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public String getAmenities() {
        return amenities;
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getRouteFrom() {
        return routeFrom;
    }

    public void setRouteFrom(String routeFrom) {
        this.routeFrom = routeFrom;
    }

    public String getRouteTo() {
        return routeTo;
    }

    public void setRouteTo(String routeTo) {
        this.routeTo = routeTo;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getCapacity() {
        return totalSeats;
    }

    public void setCapacity(int capacity) {
        this.totalSeats = capacity;
    }

    public long getDepartureTime() {
        return departureTime;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public String getFormattedDepartureTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(departureTime));
    }

    public String getFormattedArrivalTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(arrivalTime));
    }

    public void setDepartureTime(long departureTime) {
        this.departureTime = departureTime;
    }

    public void setArrivalTime(long arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getBasePoints() {
        return basePoints;
    }

    public void setBasePoints(int basePoints) {
        this.basePoints = basePoints;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int count) {
        this.ratingCount = count;
    }
}