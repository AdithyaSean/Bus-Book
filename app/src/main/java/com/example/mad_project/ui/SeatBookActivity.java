package com.example.mad_project.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mad_project.MainActivity;
import com.example.mad_project.R;
import com.example.mad_project.controller.SeatBookingController;
import com.example.mad_project.data.Bus;
import com.example.mad_project.utils.BookingManager;
import com.example.mad_project.utils.DialogManager;
import com.example.mad_project.utils.SeatLayoutManager;
import com.example.mad_project.ui.fragments.SeatSelectionFragment;
import com.example.mad_project.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SeatBookActivity extends MainActivity implements SeatSelectionFragment.OnSeatSelectionListener {
    private SeatBookingController bookingController;
    private SeatLayoutManager layoutManager;
    private TextView fareText;
    private Button confirmButton;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private boolean isSwapRequest;
    private String currentSeat;
    private int ticketId;
    private Bus selectedBus;
    private List<Integer> selectedSeats = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_seat_selection, contentFrame);
        
        boolean isDriverView = getIntent().getBooleanExtra("is_driver_view", false);
        if (isDriverView) {
            setupNavigation(true, false, "Bus Seat Layout");
            findViewById(R.id.bottomSheet).setVisibility(View.GONE);
        } else {
            setupNavigation(true, false, "Select Seats");
        }
        
        setupBottomSheet();
        initializeViews();
        setupControllers();
        handleIntentData();
    }
    
    private void setupBottomSheet() {
        View bottomSheet = findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
    
    private void initializeViews() {
        fareText = findViewById(R.id.fareText);
        confirmButton = findViewById(R.id.confirmButton);
        
        confirmButton.setOnClickListener(v -> {
            if (isSwapRequest) {
                bookingController.handleSeatSwap(currentSeat, ticketId, selectedBus,
                    new SeatBookingController.SwapCallback() {
                        @Override
                        public void onSwapSuccess() {
                            finish();
                        }
                        
                        @Override
                        public void onSwapFailure(String error) {
                            Toast.makeText(SeatBookActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    });
            } else {
                handleBookingConfirmation();
            }
        });
    }
    
    private void setupControllers() {
        bookingController = new SeatBookingController(this);
        layoutManager = new SeatLayoutManager(this);
    }
    
    private void handleIntentData() {
        int busId = getIntent().getIntExtra("bus_id", -1);
        isSwapRequest = getIntent().getBooleanExtra("is_swap", false);
        currentSeat = String.valueOf(getIntent().getIntExtra("current_seat", -1));
        ticketId = getIntent().getIntExtra("ticket_id", -1);
        
        if (busId != -1) {
            bookingController.loadBusDetails(busId, new SeatBookingController.BusLoadCallback() {
                @Override
                public void onBusLoaded(Bus bus) {
                    selectedBus = bus;
                    getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.seatSelectionContainer, 
                            SeatSelectionFragment.newInstance(
                                bus.getId(), 
                                isSwapRequest, 
                                Integer.parseInt(currentSeat)))
                        .commit();
                }
                
                @Override
                public void onLoadFailure(String error) {
                    Toast.makeText(SeatBookActivity.this, error, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }
    
    private void handleBookingConfirmation() {
        if (selectedBus == null || selectedSeats.isEmpty()) {
            Toast.makeText(this, "Please select seats to continue", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> seatNumbers = selectedSeats.stream()
            .map(String::valueOf)
            .collect(Collectors.toList());

        bookingController.processBooking(
            new SessionManager(this).getUserId(),
            selectedBus,
            seatNumbers,
            new BookingManager.BookingCallback() {
                @Override
                public void onBookingSuccess(int pointsDeducted) {
                    DialogManager.showBookingSuccess(SeatBookActivity.this, 
                        pointsDeducted,
                        () -> {
                            Intent intent = new Intent(SeatBookActivity.this, TicketsActivity.class);
                            startActivity(intent);
                            finish();
                        });
                }

                @Override
                public void onBookingFailure(String error) {
                    Toast.makeText(SeatBookActivity.this, 
                        "Booking failed: " + error, Toast.LENGTH_LONG).show();
                }
            }
        );
    }
    
    @Override
    public void onSeatsSelected(List<Integer> seats) {
        this.selectedSeats = new ArrayList<>(seats);
        
        // Update bottom sheet UI
        if (selectedSeats.isEmpty()) {
            fareText.setText(String.format("Base fare: %d points per seat", selectedBus.getBasePoints()));
            confirmButton.setEnabled(false);
        } else {
            int totalPoints = selectedSeats.size() * selectedBus.getBasePoints();
            fareText.setText(String.format("Total: %d points", totalPoints));
            confirmButton.setEnabled(true);
        }
    }
} 