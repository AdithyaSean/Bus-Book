package com.example.mad_project.utils;

import android.content.Context;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.example.mad_project.data.AppDatabase;
import com.example.mad_project.data.Bus;
import com.example.mad_project.data.Payment;
import com.example.mad_project.data.Ticket;
import com.example.mad_project.data.User;

public class TicketManager {
    private final AppDatabase db;
    private final Context context;
    private final Executor executor;

    public TicketManager(Context context) {
        this.context = context;
        this.db = AppDatabase.getDatabase(context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void cancelTicket(Ticket ticket, CancellationCallback callback) {
        executor.execute(() -> {
            try {
                db.runInTransaction(() -> {
                    // Get bus details for fare calculation
                    Bus bus = db.busDao().getBusById(ticket.getBusId());
                    if (bus == null) {
                        throw new IllegalStateException("Bus not found");
                    }

                    // Calculate refund amount using FareCalculator
                    FareCalculator.PointsBreakdown breakdown = FareCalculator.calculatePoints(bus);
                    int refundPoints = breakdown.totalPoints; // Full refund

                    // Refund points to user
                    db.userDao().addPoints(ticket.getUserId(), refundPoints);
                    
                    // Update ticket status to cancelled
                    ticket.setStatus("cancelled");
                    ticket.setUpdatedAt(System.currentTimeMillis());
                    db.ticketDao().update(ticket);
                });
                
                // Send cancellation email
                User user = db.userDao().getUserById(ticket.getUserId());
                Bus bus = db.busDao().getBusById(ticket.getBusId());
                if (user != null && bus != null) {
                    sendCancellationEmail(user, ticket, bus);
                }
                
                callback.onCancellationSuccess();
            } catch (Exception e) {
                Log.e("TicketManager", "Error cancelling ticket", e);
                callback.onCancellationFailure(e.getMessage());
            }
        });
    }

    private void sendCancellationEmail(User user, Ticket ticket, Bus bus) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        String date = dateFormat.format(new Date(ticket.getJourneyDate()));
        
        EmailSender.sendTicketCancellation(
            user.getEmail(),
            user.getName(),
            ticket.getId(),
            bus.getRegistrationNumber(),
            ticket.getSource(),
            ticket.getDestination(),
            date
        );
    }

    public interface CancellationCallback {
        void onCancellationSuccess();
        void onCancellationFailure(String error);
    }
} 