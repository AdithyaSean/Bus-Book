package com.example.mad_project.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import androidx.core.content.ContextCompat;
import android.graphics.Color;
import android.widget.GridLayout;
import com.example.mad_project.R;
import com.google.android.material.button.MaterialButton;

public class SeatLayoutManager {
    private Context context;
    private int screenWidth;
    private int buttonSize;

    public SeatLayoutManager(Context context) {
        this.context = context;
        setupDimensions();
    }

    private void setupDimensions() {
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        buttonSize = (screenWidth - 32 - 48) / 6; // Account for padding and aisle
    }

    public MaterialButton createSeatButton(String seatNumber, boolean isBooked, boolean isSelected) {
        MaterialButton button = new MaterialButton(context);
        
        // Set layout parameters with fixed size
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = buttonSize;
        params.height = buttonSize;
        params.setMargins(8, 8, 8, 8);
        button.setLayoutParams(params);
        
        // Set button appearance
        button.setText(seatNumber);
        button.setTextSize(16);
        button.setTextColor(Color.WHITE);
        button.setBackgroundResource(R.drawable.seat_color);
        button.setPadding(0, 0, 0, 0);
        button.setInsetTop(0);
        button.setInsetBottom(0);
        
        // Set button state
        updateButtonState(button, isBooked, isSelected);
        
        return button;
    }

    public void updateButtonState(MaterialButton button, boolean isBooked, boolean isSelected) {
        int color;
        if (isBooked) {
            color = ContextCompat.getColor(context, R.color.red);
            button.setEnabled(false);
        } else if (isSelected) {
            color = ContextCompat.getColor(context, R.color.accent_blue);
        } else {
            color = ContextCompat.getColor(context, R.color.green_light);
        }
        button.setBackgroundTintList(ColorStateList.valueOf(color));
    }
} 