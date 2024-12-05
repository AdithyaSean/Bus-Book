package com.example.mad_project.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;

@Entity(tableName = "notifications")
public class Notification {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int userId;
    private String type;  // "BUS_ASSIGNMENT" or "SEAT_SWAP"
    private String title;
    private String message;
    private long timestamp;
    private boolean isRead;
    private String status;  // "PENDING", "ACCEPTED", "DECLINED"
    private String additionalData;  // JSON string for type-specific data

    // Constructor
    public Notification(int userId, String type, String title, String message) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
        this.status = "PENDING";
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getAdditionalData() { return additionalData; }
    public void setAdditionalData(String additionalData) { this.additionalData = additionalData; }
} 