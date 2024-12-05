package com.example.mad_project.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.mad_project.ui.fragments.AddBusFragment;
import com.example.mad_project.ui.fragments.MyBusesFragment;

public class BusManagementPagerAdapter extends FragmentStateAdapter {
    public BusManagementPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new AddBusFragment();
        } else {
            return new MyBusesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
} 