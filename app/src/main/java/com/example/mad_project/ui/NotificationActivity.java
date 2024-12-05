package com.example.mad_project.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mad_project.MainActivity;
import com.example.mad_project.R;
import com.example.mad_project.controller.NotificationController;
import com.example.mad_project.controller.TicketController;
import com.example.mad_project.data.AppDatabase;
import com.example.mad_project.data.BusDriver;
import com.example.mad_project.data.Notification;
import com.example.mad_project.data.NotificationType;
import com.example.mad_project.data.User;
import com.example.mad_project.utils.NotificationHandler;
import com.example.mad_project.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NotificationActivity extends MainActivity {
    private LinearLayout notificationsContainer;
    private TextView emptyNotificationsText;
    private AppDatabase db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_notifications, contentFrame);
        setupNavigation(true, true, "Notifications");

        sessionManager = new SessionManager(this);
        db = AppDatabase.getDatabase(this);
        
        initializeViews();
        loadNotifications();
    }

    private void initializeViews() {
        notificationsContainer = findViewById(R.id.notificationsContainer);
        emptyNotificationsText = findViewById(R.id.emptyNotificationsText);
        
        if (notificationsContainer == null) {
            throw new RuntimeException("notificationsContainer not found");
        }
        if (emptyNotificationsText == null) {
            throw new RuntimeException("emptyNotificationsText not found");
        }
    }

    private void loadNotifications() {
        SessionManager sessionManager = new SessionManager(this);
        int userId = sessionManager.getUserId();

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Notification> notifications = db.notificationDao()
                .getNotificationsByUserId(userId);
            
            runOnUiThread(() -> {
                notificationsContainer.removeAllViews();
                if (notifications.isEmpty()) {
                    emptyNotificationsText.setVisibility(View.VISIBLE);
                } else {
                    emptyNotificationsText.setVisibility(View.GONE);
                    for (Notification notification : notifications) {
                        displayNotification(notification);
                    }
                }
            });
        });
    }

    private void displayNotification(Notification notification) {
        View notificationView = getLayoutInflater().inflate(R.layout.notification_card, 
            notificationsContainer, false);
        
        TextView titleView = notificationView.findViewById(R.id.notificationTitle);
        TextView messageView = notificationView.findViewById(R.id.notificationMessage);
        MaterialButton acceptButton = notificationView.findViewById(R.id.acceptButton);
        MaterialButton declineButton = notificationView.findViewById(R.id.declineButton);
        
        titleView.setText(notification.getTitle());
        messageView.setText(notification.getMessage());

        // Handle different notification types
        switch (notification.getType()) {
            case NotificationType.SEAT_SWAP:
                handleSeatSwapNotificationView(notification, notificationView);
                break;
            case NotificationType.BUS_ASSIGNMENT:
                handleBusAssignmentNotificationView(notification, notificationView);
                break;
        }
        
        notificationsContainer.addView(notificationView);
    }

    private void handleSeatSwapNotificationView(Notification notification, View notificationView) {
        MaterialButton acceptButton = notificationView.findViewById(R.id.acceptButton);
        MaterialButton declineButton = notificationView.findViewById(R.id.declineButton);
        
        // Show/hide buttons based on notification status
        if ("PENDING".equals(notification.getStatus())) {
            acceptButton.setVisibility(View.VISIBLE);
            declineButton.setVisibility(View.VISIBLE);
            
            acceptButton.setOnClickListener(v -> 
                handleSeatSwapResponse(true, notification.getAdditionalData(), notification.getId()));
            declineButton.setOnClickListener(v -> 
                handleSeatSwapResponse(false, notification.getAdditionalData(), notification.getId()));
        } else {
            acceptButton.setVisibility(View.GONE);
            declineButton.setVisibility(View.GONE);
        }
    }

    private void handleBusAssignmentNotificationView(Notification notification, View notificationView) {
        // Implement bus assignment notification view handling
    }

    private void handleSeatSwapResponse(boolean accepted, String additionalData, int notificationId) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Notification notification = db.notificationDao().getNotificationById(notificationId);
                if (notification != null) {
                    NotificationHandler handler = new NotificationHandler(this);
                    handler.handleSeatSwapResponse(notification, accepted, new TicketController.SwapCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(NotificationActivity.this, 
                                    "Seat swap completed successfully", Toast.LENGTH_SHORT).show();
                                notificationsContainer.removeAllViews();
                                loadNotifications();
                            });
                        }

                        @Override
                        public void onError(String errorMessage) {
                            runOnUiThread(() -> {
                                Toast.makeText(NotificationActivity.this, 
                                    "Failed to swap seats: " + errorMessage, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("NotificationActivity", "Error handling seat swap response", e);
            }
        });
    }

    private void checkPendingNotifications() {
        AppDatabase db = AppDatabase.getDatabase(this);
        SessionManager sessionManager = new SessionManager(this);
        int userId = sessionManager.getUserId();

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Notification> pendingNotifications = db.notificationDao().getPendingForUser(userId);
            runOnUiThread(() -> {
                // Update notification badge in menu or toolbar
                invalidateOptionsMenu();
            });
        });
    }

    private void handleBusAssignmentOwnerActions(Notification notification, View notificationView) {
        View actionsContainer = notificationView.findViewById(R.id.ownerActionsContainer);
        
        // Only show actions for pending assignments
        if ("PENDING".equals(notification.getStatus())) {
            actionsContainer.setVisibility(View.VISIBLE);
            
            MaterialButton cancelButton = notificationView.findViewById(R.id.cancelButton);
            MaterialButton changeDriverButton = notificationView.findViewById(R.id.changeDriverButton);
            
            cancelButton.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(this)
                    .setTitle("Cancel Assignment")
                    .setMessage("Are you sure you want to cancel this bus assignment?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        NotificationHandler handler = new NotificationHandler(this);
                        handler.handleBusAssignmentCancellation(notification);
                        loadNotifications(); // Refresh the list
                    })
                    .setNegativeButton("No", null)
                    .show();
            });
            
            changeDriverButton.setOnClickListener(v -> {
                showDriverSelectionDialog(notification);
            });
        } else {
            actionsContainer.setVisibility(View.GONE);
        }
    }

    private void showDriverSelectionDialog(Notification notification) {
        // Show dialog with available drivers
        AppDatabase db = AppDatabase.getDatabase(this);
        List<BusDriver> availableDrivers = db.busDriverDao().getAllActiveDrivers();
        
        String[] driverNames = new String[availableDrivers.size()];
        for (int i = 0; i < availableDrivers.size(); i++) {
            BusDriver driver = availableDrivers.get(i);
            User user = db.userDao().getUserById(driver.getUserId());
            driverNames[i] = user.getName() + " (" + driver.getLicenseNumber() + ")";
        }
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Select New Driver")
            .setItems(driverNames, (dialog, which) -> {
                BusDriver newDriver = availableDrivers.get(which);
                NotificationHandler handler = new NotificationHandler(this);
                handler.handleBusDriverReassignment(notification, newDriver);
                loadNotifications(); // Refresh the list
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void addOwnerBusAssignmentNotification(Notification notification) {
        View notificationView = getLayoutInflater().inflate(R.layout.item_notification_bus_assignment, 
            notificationsContainer, false);
        
        TextView titleView = notificationView.findViewById(R.id.notificationTitle);
        TextView messageView = notificationView.findViewById(R.id.notificationMessage);
        View actionsContainer = notificationView.findViewById(R.id.ownerActionsContainer);
        
        titleView.setText(notification.getTitle());
        messageView.setText(notification.getMessage());
        
        // Show actions only for pending assignments
        if ("PENDING".equals(notification.getStatus())) {
            actionsContainer.setVisibility(View.VISIBLE);
            handleBusAssignmentOwnerActions(notification, notificationView);
        } else {
            actionsContainer.setVisibility(View.GONE);
        }
        
        notificationsContainer.addView(notificationView);
    }
}
