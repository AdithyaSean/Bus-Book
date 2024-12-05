package com.example.mad_project.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.example.mad_project.data.AppDatabase;
import com.example.mad_project.data.Bus;
import com.example.mad_project.data.Notification;
import com.example.mad_project.data.Ticket;
import com.example.mad_project.utils.BookingManager;

public class SeatBookingController {
    private AppDatabase db;
    private Context context;
    private Executor executor;
    private BookingManager bookingManager;
    private Handler mainHandler;

    public interface BusLoadCallback {
        void onBusLoaded(Bus bus);
        void onLoadFailure(String error);
    }

    public interface SwapCallback {
        void onSwapSuccess();
        void onSwapFailure(String error);
    }

    public SeatBookingController(Context context) {
        this.context = context;
        this.db = AppDatabase.getDatabase(context);
        this.executor = Executors.newSingleThreadExecutor();
        this.bookingManager = new BookingManager(context);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void loadBusDetails(int busId, BusLoadCallback callback) {
        executor.execute(() -> {
            try {
                Bus bus = db.busDao().getBusById(busId);
                if (bus != null) {
                    mainHandler.post(() -> callback.onBusLoaded(bus));
                } else {
                    mainHandler.post(() -> callback.onLoadFailure("Bus not found"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onLoadFailure(e.getMessage()));
            }
        });
    }

    public void handleSeatSwap(String currentSeat, int ticketId, Bus bus, SwapCallback callback) {
        executor.execute(() -> {
            try {
                // Get current ticket
                Ticket currentTicket = db.ticketDao().getTicketById(ticketId);
                if (currentTicket == null) {
                    mainHandler.post(() -> callback.onSwapFailure("Current ticket not found"));
                    return;
                }

                // Create swap request notification
                Notification notification = new Notification(
                    currentTicket.getUserId(),
                    "SEAT_SWAP",
                    String.format("Seat swap request for %s → %s", currentSeat, currentTicket.getSeatNumber()),
                    String.format("%d,%d", ticketId, currentTicket.getBusId())
                );
                notification.setStatus("PENDING");
                
                db.notificationDao().insert(notification);
                mainHandler.post(callback::onSwapSuccess);
            } catch (Exception e) {
                mainHandler.post(() -> callback.onSwapFailure(e.getMessage()));
            }
        });
    }

    public void proceedToPayment() {
        // Reference to BookingManager's processBooking method
        // See lines 37-60 in BookingManager.java
    }

    public void processBooking(int userId, Bus selectedBus, List<String> selectedSeats,
                              BookingManager.BookingCallback callback) {
        bookingManager.processBooking(userId, selectedBus, selectedSeats, callback);
    }

    private void postToMainThread(Runnable runnable) {
        mainHandler.post(runnable);
    }
} 