package com.example.mad_project.controller;

import android.content.Context;
import com.example.mad_project.data.AppDatabase;
import com.example.mad_project.data.Notification;
import com.example.mad_project.utils.SessionManager;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import android.util.Log;

public class NotificationController {
    private final Context context;
    private final AppDatabase db;
    private final SessionManager sessionManager;
    private final Executor executor;

    public NotificationController(Context context) {
        this.context = context;
        this.db = AppDatabase.getDatabase(context);
        this.sessionManager = new SessionManager(context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void createBusAssignmentNotification(int driverId, String busNumber, String ownerName, String route) {
        executor.execute(() -> {
            String title = "Bus Assignment Request";
            String message = String.format("Owner %s wants to assign you to bus %s on route %s", 
                ownerName, busNumber, route);
            
            Notification notification = new Notification(driverId, "BUS_ASSIGNMENT", title, message);
            notification.setAdditionalData(busNumber);
            db.notificationDao().insert(notification);
        });
    }

    public void createSeatSwapNotification(int userId, String requesterName, String currentSeat, String requestedSeat, int ticket1Id, int ticket2Id) {
        executor.execute(() -> {
            try {
                // Create notification for target user
                String title = "Seat Swap Request";
                String message = String.format("%s would like to swap their seat %s with your seat %s", 
                    requesterName, currentSeat, requestedSeat);
                
                Notification targetNotification = new Notification(userId, "SEAT_SWAP", title, message);
                targetNotification.setAdditionalData(ticket1Id + ":" + ticket2Id);
                targetNotification.setStatus("PENDING");
                long notificationId = db.notificationDao().insert(targetNotification);
                
                // Create notification for requester
                String requesterTitle = "Seat Swap Request Sent";
                String requesterMessage = String.format("You requested to swap your seat %s with seat %s", 
                    currentSeat, requestedSeat);
                
                Notification requesterNotification = new Notification(
                    sessionManager.getUserId(),
                    "SEAT_SWAP",
                    requesterTitle,
                    requesterMessage
                );
                requesterNotification.setAdditionalData(ticket1Id + ":" + ticket2Id);
                requesterNotification.setStatus("PENDING");
                db.notificationDao().insert(requesterNotification);
                
                Log.d("NotificationController", "Created notifications successfully");
            } catch (Exception e) {
                Log.e("NotificationController", "Error creating notifications", e);
            }
        });
    }

    public void updateNotificationStatus(int notificationId, String status) {
        executor.execute(() -> {
            Notification notification = db.notificationDao().getNotificationById(notificationId);
            if (notification != null) {
                notification.setStatus(status);
                db.notificationDao().update(notification);
            }
        });
    }

    public void deleteNotification(int notificationId) {
        executor.execute(() -> {
            db.notificationDao().delete(notificationId);
        });
    }

    public void handleSeatSwapDecision(int notificationId, boolean accepted, String additionalData) {
        executor.execute(() -> {
            try {
                // Update status of the notification
                String status = accepted ? "ACCEPTED" : "DECLINED";
                Notification notification = db.notificationDao().getNotificationById(notificationId);
                if (notification != null) {
                    notification.setStatus(status);
                    db.notificationDao().update(notification);
                    
                    // Delete related notifications
                    String[] ticketIds = additionalData.split(":");
                    List<Notification> relatedNotifications = db.notificationDao()
                        .getNotificationsByTickets(
                            Integer.parseInt(ticketIds[0]), 
                            Integer.parseInt(ticketIds[1])
                        );
                    
                    for (Notification n : relatedNotifications) {
                        if (n.getId() != notificationId) {
                            db.notificationDao().deleteNotification(n.getId());
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("NotificationController", "Error handling seat swap decision", e);
            }
        });
    }
}