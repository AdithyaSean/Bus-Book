package com.example.mad_project.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;

import com.example.mad_project.MainActivity;
import com.example.mad_project.R;
import com.example.mad_project.controller.BusController;
import com.example.mad_project.controller.UserController;
import com.example.mad_project.data.AppDatabase;
import com.example.mad_project.data.Bus;
import com.example.mad_project.data.Ticket;
import com.example.mad_project.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DashboardActivity extends MainActivity {
    private AutoCompleteTextView fromLocationInput;
    private AutoCompleteTextView toLocationInput;
    private MaterialButton searchBusButton;
    private BusController busController;
    private ArrayAdapter<String> fromAdapter;
    private ArrayAdapter<String> toAdapter;
    private Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_dashboard, contentFrame);
        setupNavigation(false, true, "Dashboard");

        busController = new BusController(this);
        fromLocationInput = findViewById(R.id.fromLocationInput);
        toLocationInput = findViewById(R.id.toLocationInput);
        searchBusButton = findViewById(R.id.searchBusButton);
        setupListeners();
        setupAdapters();
        setupPointsCard();
        setupUpcomingRideCard();
        setupRoleSpecificButton();
    }

    private void setupAdapters() {
        fromAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        toAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        
        fromLocationInput.setAdapter(fromAdapter);
        toLocationInput.setAdapter(toAdapter);
    }

    private void setupListeners() {
        searchBusButton.setOnClickListener(v -> {
            String from = fromLocationInput.getText().toString().trim();
            String to = toLocationInput.getText().toString().trim();
            
            if (from.isEmpty() || to.isEmpty()) {
                Toast.makeText(this, "Please enter both locations", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, BusActivity.class);
            intent.putExtra("from_location", from);
            intent.putExtra("to_location", to);
            startActivity(intent);
        });

        fromLocationInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    busController.getRouteSuggestions(query, suggestions -> {
                        runOnUiThread(() -> {
                            fromAdapter.clear();
                            fromAdapter.addAll(suggestions);
                            fromAdapter.notifyDataSetChanged();
                        });
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        toLocationInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    busController.getRouteSuggestions(query, suggestions -> {
                        runOnUiThread(() -> {
                            toAdapter.clear();
                            toAdapter.addAll(suggestions);
                            toAdapter.notifyDataSetChanged();
                        });
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupPointsCard() {
        View pointsCard = findViewById(R.id.points_card);
        TextView pointsBalanceText = pointsCard.findViewById(R.id.pointsBalanceText);
        TextView pointsValueText = pointsCard.findViewById(R.id.pointsValueText);
        
        SessionManager sessionManager = new SessionManager(this);
        int userId = sessionManager.getUserId();
        
        UserController userController = new UserController(this);
        userController.getUserPoints(userId, points -> {
            runOnUiThread(() -> {
                pointsBalanceText.setText(String.format("%d", points));
                pointsValueText.setText(String.format("Value: %s", 
                    NumberFormat.getCurrencyInstance(new Locale("en", "LK"))
                        .format(points)));
            });
        });
    }

    private void setupUpcomingRideCard() {
        SessionManager sessionManager = new SessionManager(this);
        int userId = sessionManager.getUserId();
        
        View upcomingRideCard = findViewById(R.id.upcoming_ride_card);
        TextView upcomingRouteText = upcomingRideCard.findViewById(R.id.upcomingRouteText);
        TextView upcomingDateTimeText = upcomingRideCard.findViewById(R.id.upcomingDateTimeText);
        TextView upcomingSeatText = upcomingRideCard.findViewById(R.id.upcomingSeatText);
        TextView noUpcomingRideText = upcomingRideCard.findViewById(R.id.noUpcomingRideText);
        View upcomingRideContent = upcomingRideCard.findViewById(R.id.upcomingRideContent);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        // Get database instance outside of executor
        AppDatabase db = AppDatabase.getDatabase(this);

        executor.execute(() -> {
            try {
                // Get upcoming tickets for user
                List<Ticket> tickets = db.ticketDao().getUpcomingTickets(System.currentTimeMillis());
                List<Ticket> filteredTickets = tickets.stream()
                    .filter(t -> t.getUserId() == userId && t.getStatus().equalsIgnoreCase("booked"))
                    .sorted((t1, t2) -> Long.compare(t1.getJourneyDate(), t2.getJourneyDate()))
                    .collect(Collectors.toList());

                // Get bus details inside the executor thread
                Bus bus = null;
                if (!filteredTickets.isEmpty()) {
                    bus = db.busDao().getBusById(filteredTickets.get(0).getBusId());
                }

                final Bus finalBus = bus;
                final List<Ticket> finalTickets = filteredTickets;

                runOnUiThread(() -> {
                    if (!finalTickets.isEmpty() && finalBus != null) {
                        Ticket nextTicket = finalTickets.get(0);

                        upcomingRouteText.setText(String.format("%s → %s", 
                            nextTicket.getSource(), nextTicket.getDestination()));
                        upcomingDateTimeText.setText(String.format("%s at %s", 
                            dateFormat.format(new Date(nextTicket.getJourneyDate())),
                            timeFormat.format(new Date(finalBus.getDepartureTime()))));
                        upcomingSeatText.setText("Seat: " + nextTicket.getSeatNumber());

                        noUpcomingRideText.setVisibility(View.GONE);
                        upcomingRideContent.setVisibility(View.VISIBLE);

                        upcomingRideCard.setOnClickListener(v -> {
                            Intent intent = new Intent(DashboardActivity.this, TicketsActivity.class);
                            startActivity(intent);
                        });
                    } else {
                        noUpcomingRideText.setVisibility(View.VISIBLE);
                        upcomingRideContent.setVisibility(View.GONE);
                    }
                });
            } catch (Exception e) {
                Log.e("DashboardActivity", "Error loading upcoming ride", e);
                runOnUiThread(() -> {
                    noUpcomingRideText.setVisibility(View.VISIBLE);
                    upcomingRideContent.setVisibility(View.GONE);
                });
            }
        });
    }

    private void setupRoleSpecificButton() {
        SessionManager sessionManager = new SessionManager(this);
        String userRole = sessionManager.getRole();

        MaterialButton roleSpecificButton = findViewById(R.id.roleSpecificButton);
        
        if (userRole == null) {
            roleSpecificButton.setVisibility(View.GONE);
            return;
        }

        if (userRole.equalsIgnoreCase("owner")) {
            roleSpecificButton.setText("Manage Buses");
            roleSpecificButton.setVisibility(View.VISIBLE);
            roleSpecificButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, ManageBusesActivity.class);
                startActivity(intent);
            });
        } else if (userRole.equalsIgnoreCase("driver")) {
            roleSpecificButton.setText("My Assignments");
            roleSpecificButton.setVisibility(View.VISIBLE);
            roleSpecificButton.setOnClickListener(v -> {
                startActivity(new Intent(this, DriverBusesActivity.class));
            });
        } else {
            roleSpecificButton.setVisibility(View.GONE);
        }
    }
}
