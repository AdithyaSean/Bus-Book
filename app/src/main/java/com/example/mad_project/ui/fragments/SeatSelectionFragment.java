package com.example.mad_project.ui.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mad_project.R;
import com.example.mad_project.adapter.SeatAdapter;
import com.example.mad_project.controller.SeatBookingController;
import com.example.mad_project.data.AppDatabase;
import com.example.mad_project.data.Bus;
import com.example.mad_project.data.Ticket;
import com.example.mad_project.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.example.mad_project.controller.NotificationController;

public class SeatSelectionFragment extends Fragment implements SeatAdapter.OnSeatClickListener {
    private GridLayout leftSeatGrid;
    private GridLayout rightSeatGrid;
    private SeatAdapter seatAdapter;
    private SeatBookingController bookingController;
    private Bus selectedBus;
    private boolean isSwapRequest;
    private int currentSeat;
    private List<Integer> selectedSeats = new ArrayList<>();
    private List<Integer> bookedSeats = new ArrayList<>();
    private List<Integer> myBookedSeats = new ArrayList<>();
    private Executor executor = Executors.newSingleThreadExecutor();
    private AppDatabase db;
    private TextView routeText;
    private TextView selectedSeatsText;
    private TextView fareText;
    private MaterialButton confirmButton;
    private OnSeatSelectionListener selectionListener;
    private int currentTicketId;

    public static SeatSelectionFragment newInstance(int busId, boolean isSwapRequest, int currentSeat) {
        SeatSelectionFragment fragment = new SeatSelectionFragment();
        Bundle args = new Bundle();
        args.putInt("bus_id", busId);
        args.putBoolean("is_swap", isSwapRequest);
        args.putInt("current_seat", currentSeat);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = AppDatabase.getDatabase(requireContext());
        if (getArguments() != null) {
            int busId = getArguments().getInt("bus_id");
            isSwapRequest = getArguments().getBoolean("is_swap", false);
            currentSeat = getArguments().getInt("current_seat", -1);
            currentTicketId = getActivity().getIntent().getIntExtra("ticket_id", -1);
            loadBusDetails(busId, isSwapRequest);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            selectionListener = (OnSeatSelectionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnSeatSelectionListener");
        }
    }

    private void loadBusDetails(int busId, boolean isSwap) {
        executor.execute(() -> {
            try {
                selectedBus = db.busDao().getBusById(busId);
                List<Ticket> tickets = db.ticketDao().getTicketsByBusId(busId);
                bookedSeats.clear();
                myBookedSeats.clear();
                
                SessionManager sessionManager = new SessionManager(requireContext());
                int currentUserId = sessionManager.getUserId();
                long currentTime = System.currentTimeMillis();
                
                for (Ticket ticket : tickets) {
                    if (ticket.getStatus().equalsIgnoreCase("booked")) {
                        bookedSeats.add(ticket.getSeatNumber());
                        if (ticket.getUserId() == currentUserId) {
                            myBookedSeats.add(ticket.getSeatNumber());
                        }
                    }
                }
                
                requireActivity().runOnUiThread(() -> {
                    if (selectedBus != null) {
                        setupSeatAdapter();
                        initializeSeatStates();
                        updateBottomSheet();
                    } else {
                        Toast.makeText(requireContext(), "Failed to load bus details", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Error loading bus details: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seat_selection, container, false);
        initializeViews(view);
        setupController();
        return view;
    }

    private void initializeViews(View view) {
        leftSeatGrid = view.findViewById(R.id.leftSeatGrid);
        rightSeatGrid = view.findViewById(R.id.rightSeatGrid);
        
        // Initialize bottom sheet views
        View bottomSheet = requireActivity().findViewById(R.id.bottomSheet);
        routeText = bottomSheet.findViewById(R.id.routeText);
        selectedSeatsText = bottomSheet.findViewById(R.id.selectedSeatsText);
        fareText = bottomSheet.findViewById(R.id.fareText);
        confirmButton = bottomSheet.findViewById(R.id.confirmButton);
        
        // Initially disable confirm button
        confirmButton.setEnabled(false);
    }

    private void setupController() {
        bookingController = new SeatBookingController(requireContext());
        db = AppDatabase.getDatabase(requireContext());
    }

    private void setupSeatAdapter() {
        if (selectedBus == null) {
            return;
        }
        
        // Clear existing views
        leftSeatGrid.removeAllViews();
        rightSeatGrid.removeAllViews();
        
        // Set grid properties for vertical layout - 2 columns each side
        leftSeatGrid.setColumnCount(2);
        rightSeatGrid.setColumnCount(2);
        
        int totalSeats = selectedBus.getTotalSeats();
        
        // Create all seats
        for (int seatIndex = 0; seatIndex < totalSeats; seatIndex++) {
            String seatNumber = String.valueOf(seatIndex + 1);
            LinearLayout seatContainer = createSeatButton(seatNumber);
            
            // Even numbered seats go to left, odd to right
            if (seatIndex % 2 == 0) {
                leftSeatGrid.addView(seatContainer);
            } else {
                rightSeatGrid.addView(seatContainer);
            }
        }
    }

    private LinearLayout createSeatButton(String seatNumber) {
        LinearLayout seatContainer = new LinearLayout(requireContext());
        seatContainer.setOrientation(LinearLayout.VERTICAL);
        seatContainer.setGravity(Gravity.CENTER);
        
        // Create seat button with number inside
        MaterialButton button = new MaterialButton(requireContext());
        
        // Calculate button size based on screen width
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int buttonSize = (screenWidth - 200) / 6; // Account for padding and margins
        
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(buttonSize, buttonSize);
        button.setLayoutParams(buttonParams);
        button.setText(seatNumber);
        button.setTextSize(12); // Reduced text size
        button.setTextColor(Color.WHITE);
        
        // Remove all padding and insets
        button.setPadding(0, 0, 0, 0);
        button.setInsetTop(0);
        button.setInsetBottom(0);
        button.setIconPadding(0);
        button.setMinHeight(0);
        button.setMinWidth(0);
        button.setCornerRadius(16);
        
        // Prevent text wrapping
        button.setSingleLine(true);
        button.setMaxLines(1);
        button.setGravity(Gravity.CENTER);
        
        // Set initial color based on booking status
        int seatNum = Integer.parseInt(seatNumber);
        if (myBookedSeats.contains(seatNum)) {
            button.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.yellow_light)));
        } else if (bookedSeats.contains(seatNum)) {
            button.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.red)));
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.green_light)));
        }
        
        button.setOnClickListener(v -> handleSeatClick((MaterialButton) v, seatNumber));
        
        // Set container layout params for GridLayout
        GridLayout.LayoutParams containerParams = new GridLayout.LayoutParams();
        containerParams.width = GridLayout.LayoutParams.WRAP_CONTENT;
        containerParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
        containerParams.setMargins(12, 12, 12, 12);
        seatContainer.setLayoutParams(containerParams);
        
        seatContainer.addView(button);
        
        return seatContainer;
    }

    private void handleSeatClick(MaterialButton button, String seatNumber) {
        int seatNum = Integer.parseInt(seatNumber);
        
        // First check if seat is booked
        if (bookedSeats.contains(seatNum)) {
            // Check if it's not user's own booked seat
            if (!myBookedSeats.contains(seatNum)) {
                showSwapRequestDialog(seatNum);
            }
            return;  // Exit early for booked seats
        }
        
        // Handle selection for available seats
        if (selectedSeats.contains(seatNum)) {
            selectedSeats.remove(Integer.valueOf(seatNum));
        } else {
            selectedSeats.add(seatNum);
        }
        
        // Notify activity of selection change
        if (selectionListener != null) {
            selectionListener.onSeatsSelected(selectedSeats);
        }
        
        // Update appearance after selection change
        updateSeatAppearance(button, seatNumber);
        updateBottomSheet();
    }

    private void showSwapRequestDialog(int requestedSeat) {
        executor.execute(() -> {
            try {
                Ticket targetTicket = db.ticketDao().getTicketsByBusAndSeat(selectedBus.getId(), requestedSeat)
                    .stream()
                    .filter(t -> t.getStatus().equals("booked"))
                    .findFirst()
                    .orElse(null);

                if (targetTicket == null) {
                    requireActivity().runOnUiThread(() -> 
                        Toast.makeText(requireContext(), "Error: Target seat not found", Toast.LENGTH_SHORT).show());
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Seat Swap Request")
                        .setMessage(String.format("Would you like to request to swap your seat %d with seat %d?", 
                            currentSeat, requestedSeat))
                        .setPositiveButton("Request Swap", (dialog, which) -> {
                            NotificationController notificationController = 
                                new NotificationController(requireContext());
                            
                            SessionManager sessionManager = new SessionManager(requireContext());
                            String userName = sessionManager.getUsername();
                            
                            notificationController.createSeatSwapNotification(
                                targetTicket.getUserId(),
                                userName,
                                String.valueOf(currentSeat),
                                String.valueOf(requestedSeat),
                                currentTicketId,
                                targetTicket.getId()
                            );

                            Toast.makeText(requireContext(), 
                                "Seat swap request sent", Toast.LENGTH_SHORT).show();
                            requireActivity().finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(requireContext(), 
                        "Error processing request: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateSeatAppearance(MaterialButton button, String seatNumber) {
        Context context = requireContext();
        int seatNum = Integer.parseInt(seatNumber);
        
        if (myBookedSeats.contains(seatNum)) {
            button.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.yellow_light)));
        } else if (bookedSeats.contains(seatNum)) {
            // Seat booked by others
            button.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.red)));
        } else if (seatNum == currentSeat) {
            button.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.accent_blue)));
        } else if (selectedSeats.contains(seatNum)) {
            button.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.accent_blue)));
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.green_light)));
        }
    }

    @Override
    public void onSeatSelected(String seatNumber) {
        try {
            int seatNum = Integer.parseInt(seatNumber);
            if (isSwapRequest) {
                // Handle swap selection
                selectedSeats.clear();
                selectedSeats.add(seatNum);
            } else {
                // Handle normal booking selection
                if (selectedSeats.contains(seatNum)) {
                    selectedSeats.remove(Integer.valueOf(seatNum));
                } else {
                    selectedSeats.add(seatNum);
                }
            }
            
            // Notify activity of selection change
            if (selectionListener != null) {
                selectionListener.onSeatsSelected(selectedSeats);
            }
            updateBottomSheet();
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid seat number", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBookedSeatClick(String seatNumber) {
        if (isSwapRequest) {
            try {
                int seatNum = Integer.parseInt(seatNumber);
                showSwapRequestDialog(seatNum);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid seat number", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateBottomSheet() {
        if (selectedBus == null) return;
        
        // Show bus route
        routeText.setText(String.format("%s → %s", 
            selectedBus.getRouteFrom(), 
            selectedBus.getRouteTo()));
        
        // Show base fare
        int basePoints = selectedBus.getBasePoints();
        
        if (selectedSeats.isEmpty()) {
            selectedSeatsText.setText("No seats selected");
            fareText.setText(String.format("Base fare: %d points per seat", basePoints));
            confirmButton.setEnabled(false);
        } else {
            // Show selected seats
            String seatText = selectedSeats.size() == 1 ? "seat" : "seats";
            selectedSeatsText.setText(String.format("Selected %d %s: %s", 
                selectedSeats.size(), 
                seatText,
                selectedSeats.stream()
                    .sorted()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "))
            ));
            
            // Calculate and show total points
            int totalPoints = selectedSeats.size() * basePoints;
            fareText.setText(String.format("Total: %d points (%d points × %d %s)", 
                totalPoints, 
                basePoints,
                selectedSeats.size(),
                seatText));
            
            confirmButton.setEnabled(true);
        }
    }

    private void updateSelection() {
        // ... existing selection update code ...
        selectionListener.onSeatsSelected(selectedSeats);
    }

    public interface OnSeatSelectionListener {
        void onSeatsSelected(List<Integer> seats);
    }

    private void initializeSeatStates() {
        if (selectedBus != null) {
            int totalSeats = selectedBus.getTotalSeats();
            for (int i = 1; i <= totalSeats; i++) {
                if (!bookedSeats.contains(i)) {
                    // Find buttons in both grids
                    MaterialButton button = null;
                    for (int j = 0; j < leftSeatGrid.getChildCount(); j++) {
                        View child = leftSeatGrid.getChildAt(j);
                        if (child instanceof LinearLayout) {
                            View buttonView = ((LinearLayout) child).getChildAt(0);
                            if (buttonView instanceof MaterialButton && 
                                ((MaterialButton) buttonView).getText().toString().equals(String.valueOf(i))) {
                                button = (MaterialButton) buttonView;
                                break;
                            }
                        }
                    }
                    if (button == null) {
                        for (int j = 0; j < rightSeatGrid.getChildCount(); j++) {
                            View child = rightSeatGrid.getChildAt(j);
                            if (child instanceof LinearLayout) {
                                View buttonView = ((LinearLayout) child).getChildAt(0);
                                if (buttonView instanceof MaterialButton && 
                                    ((MaterialButton) buttonView).getText().toString().equals(String.valueOf(i))) {
                                    button = (MaterialButton) buttonView;
                                    break;
                                }
                            }
                        }
                    }
                    if (button != null) {
                        updateSeatAppearance(button, String.valueOf(i));
                    }
                }
            }
        }
    }
} 