package com.example.mad_project.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.R;
import com.example.mad_project.MainActivity;
import com.example.mad_project.adapter.TicketAdapter;
import com.example.mad_project.controller.BusController;
import com.example.mad_project.data.AppDatabase;
import com.example.mad_project.data.Ticket;
import com.example.mad_project.data.Bus;
import com.example.mad_project.data.Payment;
import com.example.mad_project.utils.DialogManager;
import com.example.mad_project.utils.TicketManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.text.NumberFormat;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.widget.Toast;

import androidx.core.content.ContextCompat;

public class TicketsActivity extends MainActivity {
    private RecyclerView ticketsRecyclerView;
    private TextView emptyStateText;
    private AppDatabase db;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "LK"));
    private ProgressDialog loadingDialog;
    private BusController busController;
    private Bus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_tickets, contentFrame);
        setupNavigation(true, true, "My Tickets");

        // Initialize database
        db = AppDatabase.getDatabase(this);
        busController = new BusController(this);
        initializeViews();
        loadTickets();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTickets(); // Refresh the tickets list
    }

    private void initializeViews() {
        ticketsRecyclerView = findViewById(R.id.ticketsRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
        
        ticketsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadTickets() {
        executor.execute(() -> {
            try {
                List<Ticket> tickets = db.ticketDao().getTicketsByUserId(sessionManager.getUserId());
                
                if (tickets.isEmpty()) {
                    showEmptyState();
                } else {
                    runOnUiThread(() -> displayTickets(tickets));
                }
            } catch (Exception e) {
                Log.e("TicketsActivity", "Failed to load tickets", e);
            }
        });
    }

    private void showEmptyState() {
        ticketsRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
    }

    private void displayTickets(List<Ticket> tickets) {
        ticketsRecyclerView.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);
        
        // Update adapter to show ticket status
        TicketAdapter adapter = new TicketAdapter(tickets, this::showTicketDetails, db.paymentDao());
        ticketsRecyclerView.setAdapter(adapter);
    }

    private void showTicketDetails(Ticket ticket) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.ticket_details_sheet, null);
        
        // Find views in the bottom sheet
        TextView ticketNumberText = bottomSheetView.findViewById(R.id.ticketNumberText);
        TextView startLocationText = bottomSheetView.findViewById(R.id.startLocationText);
        TextView endLocationText = bottomSheetView.findViewById(R.id.endLocationText);
        TextView journeyDateText = bottomSheetView.findViewById(R.id.journeyDateText);
        TextView seatNumberText = bottomSheetView.findViewById(R.id.seatNumberText);
        TextView busDetailsText = bottomSheetView.findViewById(R.id.busDetailsText);
        Button swapSeatButton = bottomSheetView.findViewById(R.id.swapSeatButton);
        Button cancelTicketButton = bottomSheetView.findViewById(R.id.cancelTicketButton);
        Button markCompletedButton = bottomSheetView.findViewById(R.id.markCompletedButton);
        
        // Format date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(ticket.getJourneyDate()));
        
        // Set ticket details
        ticketNumberText.setText("Ticket #" + ticket.getId());
        startLocationText.setText(ticket.getSource());
        endLocationText.setText(ticket.getDestination());
        journeyDateText.setText(formattedDate);
        seatNumberText.setText("Seat: " + ticket.getSeatNumber());
        
        // Show/hide action buttons based on ticket status
        boolean isBooked = ticket.getStatus().equalsIgnoreCase("booked");
        boolean isCompleted = ticket.getStatus().equalsIgnoreCase("completed");
        
        swapSeatButton.setVisibility(isBooked ? View.VISIBLE : View.GONE);
        cancelTicketButton.setVisibility(isBooked ? View.VISIBLE : View.GONE);
        markCompletedButton.setVisibility(isBooked ? View.VISIBLE : View.GONE);
        
        // Add status indicator
        TextView statusText = bottomSheetView.findViewById(R.id.statusText);
        statusText.setText("Status: " + ticket.getStatus().toUpperCase());
        int statusColor = ticket.getStatus().equalsIgnoreCase("booked") ? 
            ContextCompat.getColor(this, R.color.green_light) : 
            ContextCompat.getColor(this, R.color.red);
        statusText.setTextColor(statusColor);
        
        // Load bus details and payment info asynchronously
        executor.execute(() -> {
            final Bus loadedBus = db.busDao().getBusById(ticket.getBusId());
            Payment payment = ticket.getPaymentId() != null ? 
                db.paymentDao().getPaymentById(ticket.getPaymentId()) : null;
            
            runOnUiThread(() -> {
                if (loadedBus != null) {
                    busDetailsText.setText(String.format("%s (%s)", 
                        loadedBus.getRegistrationNumber(),
                        loadedBus.getModel()));

                    if (isCompleted && !ticket.isRated()) {
                        showRatingDialog(loadedBus, ticket);
                    }
                }
            });

            // Handle mark as completed button
            markCompletedButton.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(this)
                    .setTitle("Mark Journey Complete")
                    .setMessage("Are you sure you want to mark this journey as completed?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        executor.execute(() -> {
                            ticket.setStatus("completed");
                            db.ticketDao().update(ticket);
                            
                            runOnUiThread(() -> {
                                bottomSheet.dismiss();
                                loadTickets();
                                showRatingDialog(loadedBus, ticket);
                            });
                        });
                    })
                    .setNegativeButton("No", null)
                    .show();
            });
        });
        
        // Handle swap seat button
        swapSeatButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SeatBookActivity.class);
            intent.putExtra("bus_id", ticket.getBusId());
            intent.putExtra("is_swap", true);
            intent.putExtra("current_seat", ticket.getSeatNumber());
            intent.putExtra("ticket_id", ticket.getId());
            startActivity(intent);
            bottomSheet.dismiss();
        });
        
        // Handle cancel ticket button
        cancelTicketButton.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                .setTitle("Cancel Ticket")
                .setMessage("Are you sure you want to cancel this ticket? Your points will be refunded.")
                .setPositiveButton("Cancel Ticket", (dialog, which) -> {
                    TicketManager ticketManager = new TicketManager(this);
                    loadingDialog = DialogManager.showLoadingDialog(this, "Cancelling ticket...");
                    
                    ticketManager.cancelTicket(ticket, new TicketManager.CancellationCallback() {
                        @Override
                        public void onCancellationSuccess() {
                            runOnUiThread(() -> {
                                if (loadingDialog != null) {
                                    loadingDialog.dismiss();
                                }
                                Toast.makeText(TicketsActivity.this,
                                    "Ticket cancelled successfully", Toast.LENGTH_SHORT).show();
                                bottomSheet.dismiss();
                                loadTickets(); // Refresh ticket list
                            });
                        }

                        @Override
                        public void onCancellationFailure(String error) {
                            runOnUiThread(() -> {
                                if (loadingDialog != null) {
                                    loadingDialog.dismiss();
                                }
                                Toast.makeText(TicketsActivity.this,
                                    "Failed to cancel ticket: " + error, Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Keep Ticket", null)
                .show();
        });
        
        bottomSheet.setContentView(bottomSheetView);
        bottomSheet.show();
    }

    public void showRatingDialog(Bus bus, Ticket ticket) {
        View ratingView = getLayoutInflater().inflate(R.layout.dialog_rate_bus, null);
        RatingBar ratingBar = ratingView.findViewById(R.id.ratingBar);
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Rate Your Journey")
            .setView(ratingView)
            .setPositiveButton("Submit", (dialog, which) -> {
                float newRating = ratingBar.getRating();
                if (newRating > 0) {
                    busController.updateBusRating(bus, newRating);
                    // Mark ticket as rated only after successful rating
                    executor.execute(() -> {
                        ticket.setRated(true);
                        db.ticketDao().update(ticket);
                    });
                } else {
                    Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .setCancelable(false)  // Force user to rate or cancel
            .show();
    }
}
