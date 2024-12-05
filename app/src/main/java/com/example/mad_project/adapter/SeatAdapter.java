package com.example.mad_project.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.R;
import com.example.mad_project.data.Bus;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.SeatViewHolder> {
    private List<String> bookedSeats = new ArrayList<>();
    private List<String> selectedSeats = new ArrayList<>();
    private String currentSeat;
    private boolean isSwapRequest;
    private Bus selectedBus;
    private OnSeatClickListener listener;

    public interface OnSeatClickListener {
        void onSeatSelected(String seatNumber);
        void onBookedSeatClick(String seatNumber);
    }

    public SeatAdapter(Bus bus, boolean isSwapRequest, String currentSeat, OnSeatClickListener listener) {
        this.selectedBus = bus;
        this.isSwapRequest = isSwapRequest;
        this.currentSeat = currentSeat;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_seat, parent, false);
        return new SeatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatViewHolder holder, int position) {
        String seatNumber = getSeatNumber(position);
        holder.bind(seatNumber);
    }

    @Override
    public int getItemCount() {
        return selectedBus.getTotalSeats();
    }

    private String getSeatNumber(int position) {
        return String.format("%c%d", (char)('A' + position / 4), (position % 4) + 1);
    }

    class SeatViewHolder extends RecyclerView.ViewHolder {
        private MaterialButton seatButton;

        SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            seatButton = itemView.findViewById(R.id.seatButton);
        }

        void bind(String seatNumber) {
            seatButton.setText(seatNumber);
            updateSeatAppearance(seatNumber);
            setupClickListener(seatNumber);
        }

        private void updateSeatAppearance(String seatNumber) {
            Context context = itemView.getContext();
            if (bookedSeats.contains(seatNumber)) {
                seatButton.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.red)));
                seatButton.setEnabled(isSwapRequest);
            } else if (seatNumber.equals(currentSeat)) {
                seatButton.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.accent_blue)));
            } else if (selectedSeats.contains(seatNumber)) {
                seatButton.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.accent_blue)));
            } else {
                seatButton.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.green_light)));
            }
        }

        private void setupClickListener(String seatNumber) {
            seatButton.setOnClickListener(v -> {
                if (bookedSeats.contains(seatNumber)) {
                    if (isSwapRequest) {
                        listener.onBookedSeatClick(seatNumber);
                    }
                } else {
                    listener.onSeatSelected(seatNumber);
                }
            });
        }
    }
} 