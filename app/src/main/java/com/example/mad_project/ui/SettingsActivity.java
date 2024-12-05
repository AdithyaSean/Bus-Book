package com.example.mad_project.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import android.app.ProgressDialog;

import com.example.mad_project.MainActivity;
import com.example.mad_project.R;
import com.example.mad_project.data.AppDatabase;
import com.example.mad_project.data.Location;
import com.example.mad_project.data.User;
import com.example.mad_project.data.BusDriver;
import com.example.mad_project.data.BusOwner;
import com.example.mad_project.utils.Validation;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SettingsActivity extends MainActivity {
    private AppDatabase db;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_settings, contentFrame);
        setupNavigation(true, true, "Settings");

        db = AppDatabase.getDatabase(this);
        initializeViews();
    }

    private void initializeViews() {
        MaterialButton addUsersButton = findViewById(R.id.addUsersButton);
        MaterialButton addLocationsButton = findViewById(R.id.addLocationsButton);

        addUsersButton.setOnClickListener(v -> showUsersConfirmation());
        addLocationsButton.setOnClickListener(v -> showLocationsConfirmation());
    }

    private void showUsersConfirmation() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Add Sample Users")
            .setMessage("This will add sample users to the database. Continue?")
            .setPositiveButton("Add", (dialog, which) -> addSampleUsers())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showLocationsConfirmation() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Add Highway Locations")
            .setMessage("This will add highway locations to the database. Continue?")
            .setPositiveButton("Add", (dialog, which) -> addHighwayLocations())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void addSampleUsers() {
        // Show loading dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding sample users...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        executor.execute(() -> {
            try {
                db.runInTransaction(() -> {
                    // Passengers
                    User passenger1 = new User("Adithya Ekanayaka", "adithyasean@gmail.com", "+94702967909",
                        Validation.hashPassword("aaaaaa"), "user");
                    passenger1.setPoints(50000);
                    db.userDao().insert(passenger1);

                    User passenger2 = new User("Adithya Ekanayaka", "adithyasean@outlook.com", "+94767007909",
                        Validation.hashPassword("aaaaaa"), "user");
                    passenger2.setPoints(30000);
                    db.userDao().insert(passenger2);

                    User passenger3 = new User("Yasodha Heshan", "yasodhaheshan2002@gmail.com", "+94111111111",
                        Validation.hashPassword("aaaaaa"), "user");
                    passenger3.setPoints(45000);
                    db.userDao().insert(passenger3);

                    // Owners
                    User ownerUser1 = new User("Senith Nimsara", "senith2002n@gmail.com", "+94761234567",
                        Validation.hashPassword("aaaaaa"), "owner");
                    ownerUser1.setPoints(10000);
                    long owner1Id = db.userDao().insert(ownerUser1);
                    BusOwner busOwner1 = new BusOwner((int)owner1Id, "Senith Transport", "REG001", "TAX001");
                    db.busOwnerDao().insert(busOwner1);

                    User ownerUser2 = new User("Jalith Chamikara", "premjalith.@gmail.com", "+94762345678",
                        Validation.hashPassword("aaaaaa"), "owner");
                    ownerUser2.setPoints(15000);
                    long owner2Id = db.userDao().insert(ownerUser2);
                    BusOwner busOwner2 = new BusOwner((int)owner2Id, "Jalith Express", "REG002", "TAX002");
                    db.busOwnerDao().insert(busOwner2);

                    User ownerUser3 = new User("Sasmitha", "sasmitha.vishvadinu27@gmail.com", "+94763456789",
                        Validation.hashPassword("aaaaaa"), "owner");
                    ownerUser3.setPoints(20000);
                    long owner3Id = db.userDao().insert(ownerUser3);
                    BusOwner busOwner3 = new BusOwner((int)owner3Id, "Sasmitha Transit", "REG003", "TAX003");
                    db.busOwnerDao().insert(busOwner3);

                    // Drivers
                    User driverUser1 = new User("Dave Brown", "ai@gmail.com", "+94751234567",
                        Validation.hashPassword("aaaaaa"), "driver");
                    driverUser1.setPoints(10000);
                    long driver1Id = db.userDao().insert(driverUser1);
                    BusDriver busDriver1 = new BusDriver((int)driver1Id, "DL001", 
                        System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000), 5);
                    db.busDriverDao().insert(busDriver1);

                    User driverUser2 = new User("John Connor", "a@a.a", "+94752345678",
                        Validation.hashPassword("aaaaaa"), "driver");
                    driverUser2.setPoints(12000);
                    long driver2Id = db.userDao().insert(driverUser2);
                    BusDriver busDriver2 = new BusDriver((int)driver2Id, "DL002", 
                        System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000), 3);
                    db.busDriverDao().insert(busDriver2);
                });

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Sample users added successfully", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void addHighwayLocations() {
        // Show loading dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding highway locations...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        executor.execute(() -> {
            try {
                db.runInTransaction(() -> {
                    Location[] locations = {
                        new Location("Colombo", 6.927079, 79.861243),
                        new Location("Kandy", 7.290572, 80.633728),
                        new Location("Galle", 6.053519, 80.220978),
                        new Location("Hambantota", 6.127064, 81.111197),
                        new Location("Matara", 5.948853, 80.535888),
                        new Location("Kurunegala", 7.486842, 80.362439),
                        new Location("Dambulla", 7.868039, 80.650698),
                        new Location("Kottawa", 6.8427, 79.9731),
                        new Location("Mattala", 6.2921, 81.1240),
                        new Location("Kerawalapitiya", 7.0089, 79.8773),
                        new Location("Kaduwela", 6.9271, 79.9846),
                        new Location("Kadawatha", 7.0025, 79.9495),
                        new Location("Katunayake", 7.1643, 79.8760),
                        new Location("Mirigama", 7.2417, 80.1278),
                        new Location("Andarawewa", 6.1238, 81.1213),
                        new Location("Pelmadulla", 6.6204, 80.5421),
                        new Location("Athurugiriya", 6.8726, 79.9984),
                        new Location("New Kelani Bridge", 6.9537, 79.8765),
                        new Location("Orugodawatta", 6.9428, 79.8775),
                        new Location("Kahathuduwa", 6.7845, 79.9723)
                    };

                    // Clear existing locations
                    db.locationDao().deleteAllLocations();
                    
                    // Insert new locations
                    for (Location location : locations) {
                        db.locationDao().insert(location);
                    }
                });

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Highway locations added successfully", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
} 