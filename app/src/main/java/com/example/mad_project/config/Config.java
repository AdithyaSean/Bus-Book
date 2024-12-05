package com.example.mad_project.config;

import android.content.Context;
import com.example.mad_project.R;

public class Config {
    public static String HOST;
    public static String PORT;
    public static String USERNAME;
    public static String PASSWORD;
    public static String FROM_EMAIL;
    public static String FROM_NAME;

    public static void initialize(Context context) {
        HOST = context.getString(R.string.SMTP_HOST);
        PORT = context.getString(R.string.SMTP_PORT);
        USERNAME = context.getString(R.string.SMTP_EMAIL);
        PASSWORD = context.getString(R.string.SMTP_PASSWORD);
        FROM_EMAIL = context.getString(R.string.SMTP_EMAIL);
        FROM_NAME = context.getString(R.string.app_name);
    }
}