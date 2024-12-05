package com.example.mad_project.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TicketDao {
    @Insert
    long insert(Ticket ticket);

    @Update
    void update(Ticket ticket);

    @Query("SELECT * FROM tickets WHERE id = :ticketId")
    Ticket getTicketById(int ticketId);

    @Query("SELECT * FROM tickets WHERE is_active = 1")
    List<Ticket> getAllActiveTickets();

    @Query("SELECT * FROM tickets WHERE user_id = :userId ORDER BY journey_date DESC")
    List<Ticket> getTicketsByUserId(int userId);

    @Query("SELECT * FROM tickets WHERE bus_id = :busId AND is_active = 1")
    List<Ticket> getTicketsByBusId(int busId);

    @Query("SELECT * FROM tickets WHERE journey_date >= :date AND is_active = 1")
    List<Ticket> getUpcomingTickets(long date);

    @Query("UPDATE tickets SET status = :status, updated_at = :timestamp WHERE id = :ticketId")
    void updateTicketStatus(int ticketId, String status, long timestamp);

    @Query("UPDATE tickets SET is_active = 0 WHERE id = :ticketId")
    void deactivateTicket(int ticketId);

    @Query("UPDATE tickets SET payment_id = :paymentId WHERE id = :ticketId")
    void updateTicketPaymentId(int ticketId, int paymentId);

    @Query("SELECT * FROM tickets")
    List<Ticket> getAllTickets();

    @Delete
    void delete(Ticket ticket);

    @Query("SELECT * FROM tickets WHERE bus_id = :busId AND seat_number = :seatNumber")
    List<Ticket> getTicketsByBusAndSeat(int busId, int seatNumber);
}