package com.example.mad_project.adapter;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.R;
import com.example.mad_project.data.Payment;
import com.example.mad_project.data.PaymentDao;
import com.example.mad_project.data.Ticket;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {
    private final List<Ticket> tickets;
    private final OnTicketClickListener listener;
    private final PaymentDao paymentDao;
    private final Executor executor;
    private final NumberFormat currencyFormat;

    public interface OnTicketClickListener {
        void onTicketClick(Ticket ticket);
    }
    
    public TicketAdapter(List<Ticket> tickets, OnTicketClickListener listener, PaymentDao paymentDao) {
        this.tickets = tickets;
        this.listener = listener;
        this.paymentDao = paymentDao;
        this.executor = Executors.newSingleThreadExecutor();
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "LK"));
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ticket_item, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        holder.bind(tickets.get(position));
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    class TicketViewHolder extends RecyclerView.ViewHolder {
        private final TextView ticketNumberText;
        private final TextView startLocationText;
        private final TextView endLocationText;
        private final TextView ticketStatusText;
        private final TextView journeyDateText;
        private final TextView busDetailsText;
        private final TextView seatNumberText;
        private final TextView ticketFareText;
        private final TextView pointsText;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            ticketNumberText = itemView.findViewById(R.id.ticketNumberText);
            startLocationText = itemView.findViewById(R.id.startLocationText);
            endLocationText = itemView.findViewById(R.id.endLocationText);
            ticketStatusText = itemView.findViewById(R.id.ticketStatusText);
            journeyDateText = itemView.findViewById(R.id.journeyDateText);
            busDetailsText = itemView.findViewById(R.id.busDetailsText);
            seatNumberText = itemView.findViewById(R.id.seatNumberText);
            ticketFareText = itemView.findViewById(R.id.ticketFareText);
            pointsText = itemView.findViewById(R.id.pointsText);
        }

        public void bind(Ticket ticket) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
            
            ticketNumberText.setText("Ticket #" + ticket.getId());
            startLocationText.setText(ticket.getSource());
            endLocationText.setText(ticket.getDestination());
            journeyDateText.setText(dateFormat.format(new Date(ticket.getJourneyDate())));
            seatNumberText.setText("Seat: " + ticket.getSeatNumber());
            
            // Set status text and style
            ticketStatusText.setText(ticket.getStatus().toUpperCase());
            
            // Set status background color
            int statusColor;
            switch (ticket.getStatus().toLowerCase()) {
                case "booked":
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.green_light);
                    break;
                case "cancelled":
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.red);
                    break;
                default:
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.gray);
                    break;
            }
            
            ticketStatusText.setBackgroundTintList(ColorStateList.valueOf(statusColor));
            
            // Load payment info asynchronously
            executor.execute(() -> {
                Payment payment = ticket.getPaymentId() != null ? 
                    paymentDao.getPaymentById(ticket.getPaymentId()) : null;
                
                itemView.post(() -> {
                    if (payment != null) {
                        String currencyValue = currencyFormat.format(payment.getPointsUsed());
                        ticketFareText.setText(currencyValue);
                        pointsText.setText(String.format("(%d Points)", payment.getPointsUsed()));
                    }
                });
            });
            
            // Set click listener
            itemView.setOnClickListener(v -> listener.onTicketClick(ticket));
        }
    }
}
