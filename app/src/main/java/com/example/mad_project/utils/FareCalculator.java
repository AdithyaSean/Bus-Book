package com.example.mad_project.utils;

import com.example.mad_project.data.Bus;
import java.text.NumberFormat;
import java.util.Locale;

public class FareCalculator {
    public static class PointsBreakdown {
        public int basePoints;
        public int bookingFeePoints;
        public int totalPoints;
        
        public String getFormattedBaseFare() {
            return NumberFormat.getCurrencyInstance(new Locale("en", "LK"))
                .format(basePoints);
        }
        
        public String getFormattedTotalFare() {
            return NumberFormat.getCurrencyInstance(new Locale("en", "LK"))
                .format(totalPoints);
        }
    }

    public static PointsBreakdown calculatePoints(Bus bus) {
        PointsBreakdown breakdown = new PointsBreakdown();
        breakdown.basePoints = (int)bus.getBasePoints();
        breakdown.bookingFeePoints = (int)(breakdown.basePoints * 0.05);
        breakdown.totalPoints = breakdown.basePoints + breakdown.bookingFeePoints;
        return breakdown;
    }
}
