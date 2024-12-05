package com.example.mad_project.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);

    @Update
    int update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM users WHERE is_active = 1")
    List<User> getAllUsers();

    @Query("SELECT * FROM users WHERE email = :email AND is_active = 1")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE role = :role AND is_active = 1")
    List<User> getUsersByRole(String role);

    @Query("UPDATE users SET is_active = 0 WHERE id = :userId")
    void deactivateUser(int userId);

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    boolean isEmailExists(String email);

    @Query("SELECT * FROM users WHERE id = :userId")
    User getUserById(int userId);

    @Query("UPDATE users SET name = :name, email = :email, phone = :phone, updated_at = :updatedAt WHERE id = :userId")
    int updateUserProfile(int userId, String name, String email, String phone, long updatedAt);

    @Query("UPDATE users SET points = points - :points WHERE id = :userId")
    void deductPoints(int userId, int points);

    @Query("UPDATE users SET points = points + :points WHERE id = :userId")
    void addPoints(int userId, int points);

    @Query("SELECT points FROM users WHERE id = :userId")
    int getUserPoints(int userId);
}
