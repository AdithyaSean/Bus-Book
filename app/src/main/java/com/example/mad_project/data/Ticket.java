package com.example.mad_project.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "tickets",
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Bus.class,
                        parentColumns = "id",
                        childColumns = "bus_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Payment.class,
                        parentColumns = "id",
                        childColumns = "payment_id",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index(value = "user_id"),
                @Index(value = "bus_id"),
                @Index(value = "payment_id")
        })
public class Ticket {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "bus_id")
    private int busId;

    @ColumnInfo(name = "payment_id")
    private Integer paymentId;

    @ColumnInfo(name = "seat_number")
    private int seatNumber;

    @ColumnInfo(name = "journey_date")
    private long journeyDate;

    @ColumnInfo(name = "source")
    private String source;

    @ColumnInfo(name = "destination")
    private String destination;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "is_active")
    private boolean isActive;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    @ColumnInfo(name = "is_rated")
    private boolean isRated = false;

    public Ticket(int userId, int busId, int seatNumber, 
                 long journeyDate, String source, String destination, String status) {
        this.userId = userId;
        this.busId = busId;
        this.paymentId = null;
        this.seatNumber = seatNumber;
        this.journeyDate = journeyDate;
        this.source = source;
        this.destination = destination;
        this.status = status;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBusId() {
        return busId;
    }

    public void setBusId(int busId) {
        this.busId = busId;
    }

    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public long getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(long journeyDate) {
        this.journeyDate = journeyDate;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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

    public boolean isRated() {
        return isRated;
    }

    public void setRated(boolean rated) {
        isRated = rated;
    }
}