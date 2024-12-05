package com.example.mad_project.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mad_project.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class TopUpDialog {
    public static void show(Context context, int currentPoints, TopUpCallback callback) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_topup, null);
        
        EditText pointsInput = view.findViewById(R.id.pointsInput);
        TextView currentPointsText = view.findViewById(R.id.currentPointsText);
        currentPointsText.setText(String.format("Current Balance: %d points", currentPoints));
        
        builder.setTitle("Top Up Points")
               .setView(view)
               .setPositiveButton("Add Points", (dialog, which) -> {
                    String input = pointsInput.getText().toString();
                    if (!input.isEmpty()) {
                        int points = Integer.parseInt(input);
                        callback.onTopUp(points);
                    }
                })
               .setNegativeButton("Cancel", null)
               .show();
    }

    public interface TopUpCallback {
        void onTopUp(int points);
    }
} 