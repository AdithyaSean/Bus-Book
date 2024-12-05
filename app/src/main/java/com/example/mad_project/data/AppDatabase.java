package com.example.mad_project.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.mad_project.utils.Converters;

@Database(entities = {User.class, BusOwner.class, BusDriver.class, Bus.class, Payment.class, Ticket.class, Notification.class, Location.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();

    public abstract BusOwnerDao busOwnerDao();

    public abstract BusDriverDao busDriverDao();

    public abstract BusDao busDao();

    public abstract TicketDao ticketDao();

    public abstract PaymentDao paymentDao();

    public abstract NotificationDao notificationDao();

    public abstract LocationDao locationDao();

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "mad_project_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void closeDatabase() {
        if (INSTANCE != null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE != null) {
                    INSTANCE.close();
                    INSTANCE = null;
                }
            }
        }
    }
}