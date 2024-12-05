package com.example.mad_project.controller;

import android.content.Context;
import android.util.Log;
import com.example.mad_project.data.AppDatabase;
import com.example.mad_project.data.Bus;
import com.example.mad_project.data.Ticket;
import com.example.mad_project.data.TicketDao;
import com.example.mad_project.utils.NotificationHandler;

import androidx.room.Room;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class TicketController {

    private final AppDatabase db;
    private final TicketDao ticketDao;
    private final ExecutorService executorService;
    private final Context context;

    public TicketController(Context context) {
        db = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "mad_project_db").build();
        ticketDao = db.ticketDao();
        executorService = Executors.newSingleThreadExecutor();
        this.context = context;
    }

    public void createTicket(Ticket ticket, Consumer<Boolean> callback) {
        executorService.execute(() -> {
            try {
                ticketDao.insert(ticket);
                callback.accept(true);
            } catch (Exception e) {
                Log.e("TicketController", "Error creating ticket", e);
                callback.accept(false);
            }
        });
    }

    public void getAllTickets(Consumer<List<Ticket>> callback) {
        executorService.execute(() -> {
            try {
                List<Ticket> tickets = ticketDao.getAllActiveTickets();
                callback.accept(tickets);
            } catch (Exception e) {
                Log.e("TicketController", "Error getting tickets", e);
                callback.accept(Collections.emptyList());
            }
        });
    }

    public void getUpcomingTickets(Consumer<List<Ticket>> callback) {
        executorService.execute(() -> {
            try {
                long currentTime = System.currentTimeMillis();
                List<Ticket> tickets = ticketDao.getUpcomingTickets(currentTime);
                callback.accept(tickets);
            } catch (Exception e) {
                Log.e("TicketController", "Error getting upcoming tickets", e);
                callback.accept(Collections.emptyList());
            }
        });
    }

    public void updateTicketStatus(int ticketId, String status, Consumer<Boolean> callback) {
        executorService.execute(() -> {
            try {
                ticketDao.updateTicketStatus(ticketId, status, System.currentTimeMillis());
                callback.accept(true);
            } catch (Exception e) {
                Log.e("TicketController", "Error updating ticket status", e);
                callback.accept(false);
            }
        });
    }

    public void swapSeats(int ticket1Id, int ticket2Id, SwapCallback callback) {
        executorService.execute(() -> {
            try {
                // Create final holder objects for the data
                final class DataHolder {
                    Ticket ticket1;
                    Ticket ticket2;
                    Bus bus;
                }
                DataHolder holder = new DataHolder();
                
                db.runInTransaction(() -> {
                    // Get tickets
                    holder.ticket1 = db.ticketDao().getTicketById(ticket1Id);
                    holder.ticket2 = db.ticketDao().getTicketById(ticket2Id);
                    
                    // Validate tickets
                    if (holder.ticket1 == null || holder.ticket2 == null) {
                        throw new IllegalStateException("One or both tickets not found");
                    }
                    
                    holder.bus = db.busDao().getBusById(holder.ticket1.getBusId());
                    
                    if (!holder.ticket1.getStatus().equals("booked") || !holder.ticket2.getStatus().equals("booked")) {
                        throw new IllegalStateException("One or both tickets are not in valid state for swapping");
                    }
                    
                    // Store original seat numbers
                    int seat1 = holder.ticket1.getSeatNumber();
                    int seat2 = holder.ticket2.getSeatNumber();
                    
                    // Update seat numbers
                    holder.ticket1.setSeatNumber(seat2);
                    holder.ticket2.setSeatNumber(seat1);
                    
                    // Update timestamps
                    long currentTime = System.currentTimeMillis();
                    holder.ticket1.setUpdatedAt(currentTime);
                    holder.ticket2.setUpdatedAt(currentTime);
                    
                    // Update both tickets in the database
                    db.ticketDao().update(holder.ticket1);
                    db.ticketDao().update(holder.ticket2);
                    
                    // Log the swap for debugging
                    Log.d("TicketController", String.format(
                        "Swapped seats - Ticket %d: %d → %d, Ticket %d: %d → %d",
                        holder.ticket1.getId(), seat1, seat2,
                        holder.ticket2.getId(), seat2, seat1
                    ));
                });

                // Send confirmation emails after successful swap
                NotificationHandler notificationHandler = new NotificationHandler(context);
                notificationHandler.sendSwapConfirmationEmails(holder.ticket1, holder.ticket2, holder.bus);
                
                callback.onSuccess();
            } catch (Exception e) {
                Log.e("TicketController", "Error swapping seats", e);
                callback.onError(e.getMessage());
            }
        });
    }

    public interface SwapCallback {
        void onSuccess();
        void onError(String message);
    }
}