package com.example.mad_project.utils;

import android.content.Context;
import android.util.Log;

import com.example.mad_project.controller.TicketController;
import com.example.mad_project.data.*;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationHandler {
    private final Context context;
    private final AppDatabase db;
    private final Executor executor;
    private final SessionManager sessionManager;

    public NotificationHandler(Context context) {
        this.context = context;
        this.db = AppDatabase.getDatabase(context);
        this.executor = Executors.newSingleThreadExecutor();
        this.sessionManager = new SessionManager(context);
    }

    public void handleSeatSwapResponse(Notification notification, boolean accepted, TicketController.SwapCallback callback) {
        executor.execute(() -> {
            try {
                String[] ticketIds = notification.getAdditionalData().split(":");
                int ticket1Id = Integer.parseInt(ticketIds[0]);
                int ticket2Id = Integer.parseInt(ticketIds[1]);

                // Update notification status
                notification.setStatus(accepted ? "ACCEPTED" : "DECLINED");
                db.notificationDao().update(notification);

                // Create response notification for requester
                Ticket ticket1 = db.ticketDao().getTicketById(ticket1Id);
                if (ticket1 != null) {
                    Notification responseNotification = new Notification(
                        ticket1.getUserId(),
                        "SEAT_SWAP",
                        accepted ? "Seat Swap Accepted" : "Seat Swap Declined",
                        String.format("Your seat swap request has been %s", 
                            accepted ? "accepted" : "declined")
                    );
                    responseNotification.setAdditionalData(notification.getAdditionalData());
                    responseNotification.setStatus("UNREAD");
                    db.notificationDao().insert(responseNotification);
                }

                if (accepted) {
                    // Perform the seat swap
                    TicketController ticketController = new TicketController(context);
                    ticketController.swapSeats(ticket1Id, ticket2Id, callback);
                }
            } catch (Exception e) {
                Log.e("NotificationHandler", "Error handling seat swap response", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    public void handleBusAssignment(Notification notification, boolean accepted) {
        db.runInTransaction(() -> {
            Bus bus = db.busDao().getBusByRegistration(notification.getAdditionalData());
            if (bus != null) {
                handleBusDriverResponse(bus, notification, accepted);
                notifyBusOwner(bus, notification, accepted);
            }
        });
    }

    private void handleBusDriverResponse(Bus bus, Notification notification, boolean accepted) {
        if (accepted) {
            BusDriver driver = db.busDriverDao().getBusDriverByUserId(notification.getUserId());
            if (driver != null) {
                bus.setDriverId(driver.getId());
                bus.setActive(true);
            }
        } else {
            bus.setActive(false);
        }
        bus.setUpdatedAt(System.currentTimeMillis());
        db.busDao().update(bus);
    }

    private void notifyBusOwner(Bus bus, Notification driverNotification, boolean accepted) {
        BusOwner owner = db.busOwnerDao().getBusOwnerById(bus.getOwnerId());
        if (owner != null) {
            Notification ownerNotification = new Notification(
                owner.getUserId(),
                "BUS_ASSIGNMENT_RESPONSE",
                accepted ? "Driver Accepted" : "Driver Declined",
                String.format("Driver has %s the bus assignment for %s",
                    accepted ? "accepted" : "declined",
                    bus.getRegistrationNumber())
            );
            ownerNotification.setStatus("UNREAD");
            db.notificationDao().insert(ownerNotification);
        }
    }

    public void handleBusAssignmentCancellation(Notification notification) {
        Bus bus = db.busDao().getBusByRegistration(notification.getAdditionalData());
        if (bus != null) {
            bus.setActive(false);
            bus.setUpdatedAt(System.currentTimeMillis());
            db.busDao().update(bus);
        }
        notification.setStatus("CANCELLED");
        db.notificationDao().update(notification);
    }

    public void handleBusDriverReassignment(Notification notification, BusDriver newDriver) {
        Bus bus = db.busDao().getBusByRegistration(notification.getAdditionalData());
        if (bus != null) {
            bus.setDriverId(newDriver.getId());
            bus.setUpdatedAt(System.currentTimeMillis());
            db.busDao().update(bus);
            
            createBusAssignmentNotification(newDriver, bus, 
                db.busOwnerDao().getBusOwnerById(bus.getOwnerId()));
        }
    }

    public void createBusAssignmentNotification(BusDriver driver, Bus bus, BusOwner owner) {
        Notification notification = new Notification(
            driver.getUserId(),
            "BUS_ASSIGNMENT",
            "Bus Assignment Request",
            String.format("Owner %s wants to assign you to bus %s on route %s to %s", 
                owner.getCompanyName(),
                bus.getRegistrationNumber(),
                bus.getRouteFrom(),
                bus.getRouteTo())
        );
        notification.setAdditionalData(bus.getRegistrationNumber());
        notification.setStatus("PENDING");
        db.notificationDao().insert(notification);
    }

    public void sendSwapConfirmationEmails(Ticket ticket1, Ticket ticket2, Bus bus) {
        try {
            User user1 = db.userDao().getUserById(ticket1.getUserId());
            User user2 = db.userDao().getUserById(ticket2.getUserId());
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            String date = dateFormat.format(new Date(ticket1.getJourneyDate()));
            
            // Send email to first user
            EmailSender.sendSeatSwapConfirmation(
                user1.getEmail(),
                user1.getName(),
                ticket1.getId(),
                bus.getRegistrationNumber(),
                ticket1.getSource(),
                ticket1.getDestination(),
                date,
                String.valueOf(ticket2.getSeatNumber()),  // old seat
                String.valueOf(ticket1.getSeatNumber())   // new seat
            );
            
            // Send email to second user
            EmailSender.sendSeatSwapConfirmation(
                user2.getEmail(),
                user2.getName(),
                ticket2.getId(),
                bus.getRegistrationNumber(),
                ticket2.getSource(),
                ticket2.getDestination(),
                date,
                String.valueOf(ticket1.getSeatNumber()),  // old seat
                String.valueOf(ticket2.getSeatNumber())   // new seat
            );
        } catch (Exception e) {
            Log.e("NotificationHandler", "Error sending swap confirmation emails", e);
        }
    }
}