package com.example.mad_project.ui.fragments;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.mad_project.R;
import com.example.mad_project.controller.LocationController;
import com.example.mad_project.data.*;
import com.example.mad_project.ui.ManageBusesActivity;
import com.example.mad_project.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddBusFragment extends Fragment {
    private TextInputEditText registrationInput;
    private TextInputEditText modelInput;
    private TextInputEditText seatsInput;
    private AutoCompleteTextView driverInput;
    private AutoCompleteTextView fromLocationInput;
    private AutoCompleteTextView toLocationInput;
    private TextInputEditText basePointsInput;
    private MaterialButton addBusButton;
    private AppDatabase db;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private List<BusDriver> availableDrivers = new ArrayList<>();
    private TextInputEditText departureTimeInput;
    private TextInputEditText arrivalTimeInput;
    private long selectedDepartureTime;
    private long selectedArrivalTime;
    private boolean isEditMode = false;
    private int busId = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isEditMode = getArguments().getBoolean("EDIT_MODE", false);
            busId = getArguments().getInt("BUS_ID", -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_bus, container, false);
        
        db = AppDatabase.getDatabase(requireContext());
        initializeViews(view);
        setupTimeInputs();
        loadDrivers();
        setupLocationAdapters();
        setupAddBusButton();
        
        if (isEditMode && busId != -1) {
            loadExistingBusData();
        } else {
            setupDefaultTimes();
        }
        
        return view;
    }

    private void initializeViews(View view) {
        registrationInput = view.findViewById(R.id.registrationInput);
        modelInput = view.findViewById(R.id.modelInput);
        seatsInput = view.findViewById(R.id.seatsInput);
        driverInput = view.findViewById(R.id.driverInput);
        fromLocationInput = view.findViewById(R.id.fromLocationInput);
        toLocationInput = view.findViewById(R.id.toLocationInput);
        basePointsInput = view.findViewById(R.id.basePointsInput);
        addBusButton = view.findViewById(R.id.addBusButton);
        departureTimeInput = view.findViewById(R.id.departureTimeInput);
        arrivalTimeInput = view.findViewById(R.id.arrivalTimeInput);
    }

    private void loadDrivers() {
        executor.execute(() -> {
            List<BusDriver> drivers = db.busDriverDao().getAllActiveDrivers();
            availableDrivers.clear();
            availableDrivers.addAll(drivers);

            List<String> driverNames = new ArrayList<>();
            for (BusDriver driver : drivers) {
                User user = db.userDao().getUserById(driver.getUserId());
                if (user != null) {
                    driverNames.add(user.getName() + " (" + driver.getLicenseNumber() + ")");
                }
            }

            requireActivity().runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(), 
                    android.R.layout.simple_dropdown_item_1line, 
                    driverNames
                );
                driverInput.setAdapter(adapter);
            });
        });
    }

    private void setupLocationAdapters() {
        LocationController locationController = new LocationController(requireContext());
        locationController.getAllLocations(locations -> {
            requireActivity().runOnUiThread(() -> {
                ArrayAdapter<Location> adapter = new ArrayAdapter<>(
                    requireContext(), 
                    android.R.layout.simple_dropdown_item_1line, 
                    locations
                );
                fromLocationInput.setAdapter(adapter);
                toLocationInput.setAdapter(adapter);
            });
        });
    }

    private boolean validateInputs() {
        String model = modelInput.getText().toString().trim();
        String seats = seatsInput.getText().toString().trim();
        String driver = driverInput.getText().toString().trim();
        String from = fromLocationInput.getText().toString().trim();
        String to = toLocationInput.getText().toString().trim();
        String basePoints = basePointsInput.getText().toString().trim();
        
        // Only check registration in add mode
        if (!isEditMode) {
            String registration = registrationInput.getText().toString().trim();
            if (registration.isEmpty()) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Registration number is required", Toast.LENGTH_SHORT).show();
                });
                return false;
            }
        }

        if (model.isEmpty() || seats.isEmpty() || driver.isEmpty() || 
            from.isEmpty() || to.isEmpty() || basePoints.isEmpty()) {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            });
            return false;
        }

        try {
            Integer.parseInt(seats);
            Integer.parseInt(basePoints);
        } catch (NumberFormatException e) {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
            });
            return false;
        }

        return true;
    }

    private int getSelectedDriverPosition() {
        String selectedText = driverInput.getText().toString();
        for (int i = 0; i < availableDrivers.size(); i++) {
            BusDriver driver = availableDrivers.get(i);
            User user = db.userDao().getUserById(driver.getUserId());
            String driverText = user.getName() + " (" + driver.getLicenseNumber() + ")";
            if (driverText.equals(selectedText)) {
                return i;
            }
        }
        return -1;
    }

    private void saveBus() {
        if (!validateInputs()) return;

        SessionManager sessionManager = new SessionManager(requireContext());
        int userId = sessionManager.getUserId();

        // Get selected driver
        int selectedPosition = getSelectedDriverPosition();
        if (selectedPosition == -1) {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Please select a driver", Toast.LENGTH_SHORT).show();
            });
            return;
        }

        BusDriver selectedDriver = availableDrivers.get(selectedPosition);

        executor.execute(() -> {
            try {
                BusOwner busOwner = db.busOwnerDao().getBusOwnerByUserId(userId);
                
                if (busOwner == null) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "You are not registered as a bus owner", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                Location fromLocation = db.locationDao().getLocationByName(
                    fromLocationInput.getText().toString().trim()
                );
                Location toLocation = db.locationDao().getLocationByName(
                    toLocationInput.getText().toString().trim()
                );

                if (fromLocation == null || toLocation == null) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Invalid locations selected", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                Bus bus;
                if (isEditMode) {
                    // Update existing bus
                    bus = db.busDao().getBusById(busId);
                    bus.setModel(modelInput.getText().toString().trim());
                    bus.setTotalSeats(Integer.parseInt(seatsInput.getText().toString().trim()));
                    bus.setDriverId(selectedDriver.getId());
                    bus.setRouteFrom(fromLocation.getName());
                    bus.setRouteTo(toLocation.getName());
                    bus.setLatitude(fromLocation.getLatitude());
                    bus.setLongitude(fromLocation.getLongitude());
                    bus.setDepartureTime(selectedDepartureTime);
                    bus.setArrivalTime(selectedArrivalTime);
                    bus.setBasePoints(Integer.parseInt(basePointsInput.getText().toString().trim()));
                    
                    db.runInTransaction(() -> {
                        db.busDao().update(bus);
                        createDriverNotification(selectedDriver, bus, busOwner);
                    });
                } else {
                    // Create new bus
                    bus = new Bus(
                        busOwner.getId(),
                        selectedDriver.getId(),
                        registrationInput.getText().toString().trim(),
                        modelInput.getText().toString().trim(),
                        Integer.parseInt(seatsInput.getText().toString().trim()),
                        "WiFi, AC",
                        true,
                        fromLocation.getName(),
                        toLocation.getName(),
                        fromLocation.getLatitude(),
                        fromLocation.getLongitude(),
                        selectedDepartureTime,
                        selectedArrivalTime,
                        Integer.parseInt(basePointsInput.getText().toString().trim())
                    );

                    db.runInTransaction(() -> {
                        long busId = db.busDao().insert(bus);
                        createDriverNotification(selectedDriver, bus, busOwner);
                        createOwnerNotification(busOwner.getUserId(), selectedDriver, bus);
                    });
                }

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), 
                        isEditMode ? "Bus updated successfully" : "Bus assignment request sent to driver", 
                        Toast.LENGTH_SHORT).show();
                    requireActivity().finish();
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), 
                        "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void createDriverNotification(BusDriver driver, Bus bus, BusOwner owner) {
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

    private void createOwnerNotification(int ownerId, BusDriver driver, Bus bus) {
        Notification notification = new Notification(
            ownerId,
            "BUS_ASSIGNMENT_PENDING",
            "Pending Driver Acceptance",
            String.format("Waiting for driver %s to accept assignment to bus %s",
                driver.getLicenseNumber(),
                bus.getRegistrationNumber())
        );
        notification.setAdditionalData(bus.getRegistrationNumber());
        notification.setStatus("PENDING");
        db.notificationDao().insert(notification);
    }

    private void clearInputs() {
        registrationInput.setText("");
        modelInput.setText("");
        seatsInput.setText("");
        driverInput.setText("");
        fromLocationInput.setText("");
        toLocationInput.setText("");
        basePointsInput.setText("");
    }

    private void setupAddBusButton() {
        addBusButton.setOnClickListener(v -> {
            executor.execute(() -> {
                if (validateInputs()) {
                    saveBus();
                }
            });
        });
    }

    public void onBookClick(Bus bus) {
        // Not needed for owner view
    }

    private void setupTimeInputs() {
        departureTimeInput.setOnClickListener(v -> showTimePickerDialog(true));
        arrivalTimeInput.setOnClickListener(v -> showTimePickerDialog(false));
    }

    private void showTimePickerDialog(boolean isDeparture) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
            requireContext(),
            (view, hourOfDay, selectedMinute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, selectedMinute);
                
                long selectedTime = calendar.getTimeInMillis();
                
                if (isDeparture) {
                    selectedDepartureTime = selectedTime;
                    departureTimeInput.setText(formatTime(selectedTime));
                } else {
                    selectedArrivalTime = selectedTime;
                    arrivalTimeInput.setText(formatTime(selectedTime));
                }
            },
            hour,
            minute,
            true
        );
        
        timePickerDialog.show();
    }

    private String formatTime(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timeInMillis));
    }

    private void setupDefaultTimes() {
        Calendar calendar = Calendar.getInstance();
        selectedDepartureTime = calendar.getTimeInMillis();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        selectedArrivalTime = calendar.getTimeInMillis();
        
        departureTimeInput.setText(formatTime(selectedDepartureTime));
        arrivalTimeInput.setText(formatTime(selectedArrivalTime));
    }

    private void loadExistingBusData() {
        executor.execute(() -> {
            Bus bus = db.busDao().getBusById(busId);
            if (bus != null) {
                requireActivity().runOnUiThread(() -> {
                    registrationInput.setVisibility(View.GONE);
                    modelInput.setText(bus.getModel());
                    seatsInput.setText(String.valueOf(bus.getTotalSeats()));
                    fromLocationInput.setText(bus.getRouteFrom());
                    toLocationInput.setText(bus.getRouteTo());
                    basePointsInput.setText(String.valueOf(bus.getBasePoints()));
                    selectedDepartureTime = bus.getDepartureTime();
                    selectedArrivalTime = bus.getArrivalTime();
                    
                    departureTimeInput.setText(formatTime(selectedDepartureTime));
                    arrivalTimeInput.setText(formatTime(selectedArrivalTime));
                    
                    addBusButton.setText("Update Bus");
                });
            }
        });
    }
} 