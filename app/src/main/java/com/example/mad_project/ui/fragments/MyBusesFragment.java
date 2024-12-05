package com.example.mad_project.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mad_project.R;
import com.example.mad_project.adapter.BusAdapter;
import com.example.mad_project.data.AppDatabase;
import com.example.mad_project.data.Bus;
import com.example.mad_project.ui.ManageBusActivity;
import com.example.mad_project.utils.SessionManager;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import android.util.Log;
import android.widget.Toast;

public class MyBusesFragment extends Fragment implements BusAdapter.OnBusClickListener {
    private RecyclerView busRecyclerView;
    private TextView emptyStateText;
    private AppDatabase db;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_buses, container, false);
        
        busRecyclerView = view.findViewById(R.id.busRecyclerView);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        busRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        db = AppDatabase.getDatabase(requireContext());
        loadOwnerBuses();
        
        return view;
    }

    public void loadOwnerBuses() {
        SessionManager sessionManager = new SessionManager(requireContext());
        int userId = sessionManager.getUserId();

        executor.execute(() -> {
            try {
                // Get owner ID from user ID
                int ownerId = db.busOwnerDao().getBusOwnerByUserId(userId).getId();
                // Get all buses for this owner
                List<Bus> ownerBuses = db.busDao().getBusesByOwnerId(ownerId);
                
                requireActivity().runOnUiThread(() -> {
                    if (ownerBuses.isEmpty()) {
                        emptyStateText.setVisibility(View.VISIBLE);
                        busRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyStateText.setVisibility(View.GONE);
                        busRecyclerView.setVisibility(View.VISIBLE);
                        
                        setupRecyclerView(ownerBuses);
                    }
                });
            } catch (Exception e) {
                Log.e("MyBusesFragment", "Error loading buses", e);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), 
                        "Failed to load buses: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setupRecyclerView(List<Bus> buses) {
        BusAdapter adapter = new BusAdapter(buses, this, true, false);  // true for owner view
        busRecyclerView.setAdapter(adapter);
        busRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    @Override
    public void onBusClick(Bus bus) {
        // Launch AddBusActivity in edit mode
        Intent intent = new Intent(requireContext(), ManageBusActivity.class);
        intent.putExtra("EDIT_MODE", true);
        intent.putExtra("BUS_ID", bus.getId());
        intent.putExtra("BUS_REGISTRATION", bus.getRegistrationNumber());
        intent.putExtra("BUS_MODEL", bus.getModel());
        intent.putExtra("BUS_SEATS", bus.getTotalSeats());
        intent.putExtra("BUS_FROM", bus.getRouteFrom());
        intent.putExtra("BUS_TO", bus.getRouteTo());
        intent.putExtra("BUS_BASE_POINTS", bus.getBasePoints());
        intent.putExtra("BUS_DEPARTURE_TIME", bus.getDepartureTime());
        intent.putExtra("BUS_ARRIVAL_TIME", bus.getArrivalTime());
        startActivity(intent);
    }

    @Override
    public void onBookClick(Bus bus) {
        // Not needed for owner view
    }
} 