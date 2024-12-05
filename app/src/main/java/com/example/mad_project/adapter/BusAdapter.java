package com.example.mad_project.adapter;

import androidx.recyclerview.widget.RecyclerView;
import com.example.mad_project.data.Bus;
import java.util.List;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mad_project.R;
import com.example.mad_project.data.Bus;
import com.example.mad_project.utils.FareCalculator;

import java.util.List;

public class BusAdapter extends RecyclerView.Adapter<BusAdapter.BusViewHolder> {
    private final List<Bus> buses;
    private final OnBusClickListener listener;
    private final boolean isOwnerView;
    private final boolean isDriverView;

    public interface OnBusClickListener {
        void onBusClick(Bus bus);
        void onBookClick(Bus bus);
    }

    public BusAdapter(List<Bus> buses, OnBusClickListener listener, boolean isOwnerView, boolean isDriverView) {
        this.buses = buses;
        this.listener = listener;
        this.isOwnerView = isOwnerView;
        this.isDriverView = isDriverView;
    }

    @NonNull
    @Override
    public BusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bus_list_item, parent, false);
        return new BusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BusViewHolder holder, int position) {
        Bus bus = buses.get(position);
        
        holder.busNumberText.setText(bus.getRegistrationNumber());
        holder.startLocationText.setText(bus.getRouteFrom());
        holder.endLocationText.setText(bus.getRouteTo());
        holder.busModelText.setText(bus.getModel());
        holder.amenitiesText.setText(bus.getAmenities());
        holder.seatsText.setText(String.format("%d seats • %.1f (%d ratings)", 
            bus.getTotalSeats(), 
            bus.getRating(),
            bus.getRatingCount()));
        holder.departureTimeText.setText(bus.getFormattedDepartureTime());
        holder.arrivalTimeText.setText(bus.getFormattedArrivalTime());
        
        FareCalculator.PointsBreakdown points = FareCalculator.calculatePoints(bus);
        holder.priceText.setText(points.getFormattedTotalFare());
        holder.baseFareText.setText(String.format("%s (%d Points)",
            points.basePoints, points.basePoints));

        holder.ratingBar.setRating(bus.getRating());

        holder.itemView.setOnClickListener(v -> listener.onBusClick(bus));
        
        if (isOwnerView) {
            holder.bookButton.setText("Edit Bus");
            holder.bookButton.setOnClickListener(v -> listener.onBusClick(bus));
        } else if (!isOwnerView && !isDriverView) {
            holder.bookButton.setText("Book Now");
            holder.bookButton.setOnClickListener(v -> listener.onBookClick(bus));
        } else {
            holder.bookButton.setText("View Details");
            holder.bookButton.setOnClickListener(v -> listener.onBusClick(bus));
        }
    }

    @Override
    public int getItemCount() {
        return buses.size();
    }

    static class BusViewHolder extends RecyclerView.ViewHolder {
        private final TextView busNumberText;
        private final TextView startLocationText;
        private final TextView endLocationText;
        private final TextView busModelText;
        private final TextView amenitiesText;
        private final TextView seatsText;
        private final TextView departureTimeText;
        private final TextView arrivalTimeText;
        private final TextView priceText;
        private final TextView baseFareText;
        private final View priceBreakdownLayout;
        private final Button bookButton;
        private final RatingBar ratingBar;

        public BusViewHolder(@NonNull View itemView) {
            super(itemView);
            busNumberText = itemView.findViewById(R.id.busNumberText);
            startLocationText = itemView.findViewById(R.id.startLocationText);
            endLocationText = itemView.findViewById(R.id.endLocationText);
            busModelText = itemView.findViewById(R.id.busModelText);
            amenitiesText = itemView.findViewById(R.id.amenitiesText);
            seatsText = itemView.findViewById(R.id.seatsText);
            departureTimeText = itemView.findViewById(R.id.departureTimeText);
            arrivalTimeText = itemView.findViewById(R.id.arrivalTimeText);
            priceText = itemView.findViewById(R.id.priceText);
            baseFareText = itemView.findViewById(R.id.baseFareText);
            priceBreakdownLayout = itemView.findViewById(R.id.priceBreakdownLayout);
            bookButton = itemView.findViewById(R.id.bookButton);
            ratingBar = itemView.findViewById(R.id.busRating);
        }
    }
}
 