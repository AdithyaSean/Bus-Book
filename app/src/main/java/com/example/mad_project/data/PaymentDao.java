package com.example.mad_project.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PaymentDao {
    @Insert
    long insert(Payment payment);

    @Update
    void update(Payment payment);

    @Query("SELECT * FROM payments WHERE is_active = 1")
    List<Payment> getAllActivePayments();

    @Query("SELECT * FROM payments WHERE user_id = :userId AND is_active = 1")
    List<Payment> getPaymentsByUserId(int userId);

    @Query("SELECT * FROM payments WHERE ticket_id = :ticketId AND is_active = 1")
    Payment getPaymentByTicketId(int ticketId);

    @Query("SELECT * FROM payments WHERE id = :paymentId AND is_active = 1")
    Payment getPaymentById(Integer paymentId);

    @Delete
    void delete(Payment payment);
}