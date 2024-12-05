package com.example.mad_project.controller;

import static com.example.mad_project.utils.Validation.hashPassword;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;

import com.example.mad_project.data.AppDatabase;
import com.example.mad_project.data.BusDriver;
import com.example.mad_project.data.BusDriverDao;
import com.example.mad_project.data.BusOwner;
import com.example.mad_project.data.BusOwnerDao;
import com.example.mad_project.data.User;
import com.example.mad_project.data.UserDao;
import com.example.mad_project.utils.SessionManager;
import com.example.mad_project.utils.Validation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class UserController {

    private final AppDatabase db;
    private final UserDao userDao;
    private final BusDriverDao driverDao;
    private final BusOwnerDao ownerDao;
    private final ExecutorService executorService;
    private final SessionManager sessionManager;

    public UserController(Context context) {
        db = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "mad_project_db").build();
        userDao = db.userDao();
        driverDao = db.busDriverDao();
        ownerDao = db.busOwnerDao();
        executorService = Executors.newSingleThreadExecutor();
        sessionManager = new SessionManager(context);
    }

    public void register(String name, String email, String phone, String password, String role, Consumer<User> callback) {
        executorService.execute(() -> {
            try {
                if (userDao.isEmailExists(email)) {
                    callback.accept(null);
                    return;
                }

                String hashedPassword = hashPassword(password);
                User user = new User(name, email, phone, hashedPassword, role);
                int userId = (int) userDao.insert(user);
                user.setId((int) userId);
                callback.accept(user);
            } catch (Exception e) {
                Log.e("UserController", "Error registering user", e);
                callback.accept(null);
            }
        });
    }

    public void login(String email, String password, Consumer<User> callback) {
        executorService.execute(() -> {
            try {
                User user = userDao.getUserByEmail(email);
                if (user != null && Validation.verifyPassword(password, user.getPassword())) {
                    sessionManager.setLogin(true, user.getName(), email, user.getId(), user.getRole(), user.getImage());
                    callback.accept(user);
                } else {
                    callback.accept(null);
                }
            } catch (Exception e) {
                Log.e("UserController", "Error logging in", e);
                callback.accept(null);
            }
        });
    }

    public void upgradeToDriver(String licenseNumber, long licenseExpiry, int yearsExperience,
                             Consumer<Boolean> callback) {
        executorService.execute(() -> {
            try {
                // Get current user ID from session
                int userId = sessionManager.getUserId();
                if (userId == -1) {
                    callback.accept(false);
                    return;
                }

                // Check if license exists
                if (driverDao.isLicenseExists(licenseNumber)) {
                    callback.accept(false);
                    return;
                }

                // Use a transaction to ensure both operations succeed or fail together
                db.runInTransaction(() -> {
                    // Update user role to driver
                    User user = userDao.getUserById(userId);
                    user.setRole("driver");
                    userDao.update(user);

                    // Create driver record
                    BusDriver driver = new BusDriver(userId, licenseNumber, licenseExpiry, yearsExperience);
                    driverDao.insert(driver);
                });

                callback.accept(true);
            } catch (Exception e) {
                Log.e("UserController", "Error upgrading to driver", e);
                callback.accept(false);
            }
        });
    }

    public void upgradeToOwner(String companyName, String companyRegistration, String taxId,
                            Consumer<Boolean> callback) {
        executorService.execute(() -> {
            try {
                // Get current user ID from session
                int userId = sessionManager.getUserId();
                if (userId == -1) {
                    callback.accept(false);
                    return;
                }

                // Check if registration number exists
                if (ownerDao.getBusOwnerByRegistration(companyRegistration) != null) {
                    callback.accept(false);
                    return;
                }

                // Use a transaction to ensure both operations succeed or fail together
                db.runInTransaction(() -> {
                    // Update user role to owner
                    User user = userDao.getUserById(userId);
                    user.setRole("owner");
                    userDao.update(user);

                    // Create owner record
                    BusOwner owner = new BusOwner(userId, companyName, companyRegistration, taxId);
                    ownerDao.insert(owner);
                });

                callback.accept(true);
            } catch (Exception e) {
                Log.e("UserController", "Error upgrading to owner", e);
                callback.accept(false);
            }
        });
    }

    public void getUserById(int userId, Consumer<User> callback) {
        executorService.execute(() -> {
            try {
                User user = userDao.getUserById(userId);
                callback.accept(user);
            } catch (Exception e) {
                Log.e("UserController", "Error getting user by ID", e);
                callback.accept(null);
            }
        });
    }

    public void update(User user, Consumer<Boolean> callback) {
        executorService.execute(() -> {
            try {
                // Check if email exists for other users
                User existingUser = userDao.getUserByEmail(user.getEmail());
                if (existingUser != null && existingUser.getId() != user.getId()) {
                    callback.accept(false);
                    return;
                }

                user.setUpdatedAt(System.currentTimeMillis());
                int result = userDao.update(user);
                
                if (result > 0) {
                    // Update session
                    sessionManager.setLogin(true, user.getName(), user.getEmail(), user.getId(),
                        user.getRole(), user.getImage());
                    callback.accept(true);
                } else {
                    callback.accept(false);
                }
            } catch (Exception e) {
                Log.e("UserController", "Error updating user", e);
                callback.accept(false);
            }
        });
    }

    public void addPoints(int userId, int points, Consumer<Boolean> callback) {
        executorService.execute(() -> {
            try {
                userDao.addPoints(userId, points);
                callback.accept(true);
            } catch (Exception e) {
                Log.e("UserController", "Error adding points", e);
                callback.accept(false);
            }
        });
    }

    public void getUserPoints(int userId, Consumer<Integer> callback) {
        executorService.execute(() -> {
            try {
                int points = userDao.getUserPoints(userId);
                callback.accept(points);
            } catch (Exception e) {
                Log.e("UserController", "Error getting user points", e);
                callback.accept(0);
            }
        });
    }
}