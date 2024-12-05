package com.example.mad_project.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DialogManager {
    public static void showBookingConfirmation(Context context, int points, Runnable onConfirm) {
        new AlertDialog.Builder(context)
            .setTitle("Confirm Booking")
            .setMessage(String.format("Total points to be deducted: %d\nDo you want to proceed?", points))
            .setPositiveButton("Confirm", (dialog, which) -> onConfirm.run())
            .setNegativeButton("Cancel", null)
            .show();
    }

    public static void showBookingSuccess(Context context, int pointsDeducted, Runnable onDismiss) {
        new MaterialAlertDialogBuilder(context)
            .setTitle("Booking Successful!")
            .setMessage(String.format("Your booking is confirmed!\n\n%d points have been deducted from your account.\n\nA confirmation email has been sent to your registered email address.", pointsDeducted))
            .setPositiveButton("View Ticket", (dialog, which) -> {
                dialog.dismiss();
                onDismiss.run();
            })
            .setCancelable(false)
            .show();
    }

    public static ProgressDialog showLoadingDialog(Context context, String message) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    public static void showSwapConfirmationDialog(Context context, String title, String message, Runnable onConfirm) {
        new AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Confirm", (dialog, which) -> onConfirm.run())
            .setNegativeButton("Cancel", null)
            .show();
    }
}