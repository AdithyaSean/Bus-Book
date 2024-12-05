package com.example.mad_project.controller;

import android.content.Context;
import android.util.Log;
import androidx.room.Room;

import com.example.mad_project.data.AppDatabase;
import com.example.mad_project.data.Payment;
import com.example.mad_project.data.PaymentDao;
import com.example.mad_project.data.UserDao;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class PaymentController {
    private final AppDatabase db;
    private final PaymentDao paymentDao;
    private final ExecutorService executorService;
    private static final String TAG = "PaymentController";
    private final UserDao userDao;

    public PaymentController(Context context) {
        db = Room.databaseBuilder(context.getApplicationContext(), 
            AppDatabase.class, "mad_project_db").build();
        paymentDao = db.paymentDao();
        executorService = Executors.newSingleThreadExecutor();
        userDao = db.userDao();
    }

    public void processPointsPayment(Payment payment, Consumer<Boolean> callback) {
        executorService.execute(() -> {
            try {
                // Check if user has enough points
                int userPoints = userDao.getUserPoints(payment.getUserId());
                if (userPoints < payment.getPointsUsed()) {
                    callback.accept(false);
                    return;
                }

                // Deduct points and create payment record
                db.runInTransaction(() -> {
                    userDao.deductPoints(payment.getUserId(), payment.getPointsUsed());
                    paymentDao.insert(payment);
                });
                
                callback.accept(true);
            } catch (Exception e) {
                Log.e(TAG, "Error processing points payment", e);
                callback.accept(false);
            }
        });
    }

    public void getPaymentHistory(int userId, Consumer<List<Payment>> callback) {
        executorService.execute(() -> {
            try {
                List<Payment> payments = paymentDao.getPaymentsByUserId(userId);
                callback.accept(payments);
            } catch (Exception e) {
                Log.e(TAG, "Error getting payment history", e);
                callback.accept(Collections.emptyList());
            }
        });
    }
}
