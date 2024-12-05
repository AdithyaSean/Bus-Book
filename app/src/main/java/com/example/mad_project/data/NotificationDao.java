package com.example.mad_project.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface NotificationDao {
    @Insert
    long insert(Notification notification);

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    List<Notification> getNotificationsByUserId(int userId);

    @Query("SELECT * FROM notifications WHERE userId = :userId AND status = 'PENDING' ORDER BY timestamp DESC")
    List<Notification> getPendingForUser(int userId);

    @Query("SELECT * FROM notifications WHERE id = :notificationId")
    Notification getNotificationById(int notificationId);

    @Update
    void update(Notification notification);

    @Query("DELETE FROM notifications WHERE id = :notificationId")
    void delete(int notificationId);

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND status = 'PENDING'")
    int getPendingNotificationCount(int userId);

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    void markAsRead(int notificationId);

    @Query("DELETE FROM notifications WHERE userId = :userId AND timestamp < :timestamp")
    void deleteOldNotifications(int userId, long timestamp);

    @Query("SELECT * FROM notifications WHERE userId = :userId AND type = :type ORDER BY timestamp DESC")
    List<Notification> getNotificationsByType(int userId, String type);

    @Query("DELETE FROM notifications WHERE id = :notificationId")
    void deleteNotification(int notificationId);

    @Query("SELECT * FROM notifications WHERE additionalData LIKE '%' || :ticket1Id || ':' || :ticket2Id || '%' OR additionalData LIKE '%' || :ticket2Id || ':' || :ticket1Id || '%'")
    List<Notification> getNotificationsByTickets(int ticket1Id, int ticket2Id);
}
    