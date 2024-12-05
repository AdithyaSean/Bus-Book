package com.example.mad_project.utils;

import android.util.Log;

import com.example.mad_project.config.Config;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailSender {
    private static final String TAG = "EmailSender";

    public static void sendTicketConfirmation(String toEmail, String userName, 
            int ticketId, String busNumber, String departure, String destination, 
            String date, String time, String seatNumber) {
        
        new Thread(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", Config.HOST);
                props.put("mail.smtp.port", Config.PORT);

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                            Config.USERNAME,
                            Config.PASSWORD
                        );
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(Config.FROM_EMAIL, Config.FROM_NAME));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject("Bus Ticket Confirmation - #" + ticketId);
                
                String htmlContent = buildConfirmationEmailTemplate(userName, ticketId, busNumber,
                    departure, destination, date, time, seatNumber);
                message.setContent(htmlContent, "text/html; charset=utf-8");

                Transport.send(message);
                Log.d(TAG, "Email sent successfully to " + toEmail);
            } catch (Exception e) {
                Log.e(TAG, "Failed to send email", e);
            }
        }).start();
    }

    private static String buildConfirmationEmailTemplate(String userName, int ticketId,
                                                         String busNumber, String departure, String destination,
                                                         String date, String time, String seatNumber) {
        return String.format("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <h1 style="color: #1976D2;">Bus Ticket Confirmation</h1>
                <p>Dear %s,</p>
                <p>Your bus ticket has been booked successfully. Here are your ticket details:</p>
                <div style="background-color: #f5f5f5; padding: 20px; border-radius: 8px; margin: 20px 0;">
                    <p><strong>Ticket ID:</strong> %d</p>
                    <p><strong>Bus Number:</strong> %s</p>
                    <p><strong>From:</strong> %s</p>
                    <p><strong>To:</strong> %s</p>
                    <p><strong>Date:</strong> %s</p>
                    <p><strong>Time:</strong> %s</p>
                    <p><strong>Seat Number:</strong> %s</p>
                </div>
                <p>Thank you for choosing our service!</p>
                <p>Best regards,<br>Bus Book Team</p>
            </div>
            """,
            userName, ticketId, busNumber, departure, destination, date, time, seatNumber);
    }

    public static void sendTicketCancellation(String email, String name, int id, String registrationNumber, String source, String destination, String date) {
        new Thread(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", Config.HOST);
                props.put("mail.smtp.port", Config.PORT);

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                            Config.USERNAME,
                            Config.PASSWORD
                        );
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(Config.FROM_EMAIL, Config.FROM_NAME));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                message.setSubject("Bus Ticket Cancellation - #" + id);

                String htmlContent = buildCancellationEmailTemplate(name, id, registrationNumber, source, destination, date);
                message.setContent(htmlContent, "text/html; charset=utf-8");

                Transport.send(message);
                Log.d(TAG, "Email sent successfully to " + email);
            } catch (Exception e) {
                Log.e(TAG, "Failed to send email", e);
            }
        }).start();
    }

    public static String buildCancellationEmailTemplate(String name, int id, String registrationNumber, String source, String destination, String date) {
        return String.format("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <h1 style="color: #1976D2;">Bus Ticket Cancellation</h1>
                <p>Dear %s,</p>
                <p>Your bus ticket has been cancelled successfully. Here are your ticket details:</p>
                <div style="background-color: #f5f5f5; padding: 20px; border-radius: 8px; margin: 20px 0;">
                    <p><strong>Ticket ID:</strong> %d</p>
                    <p><strong>Bus Registration Number:</strong> %s</p>
                    <p><strong>From:</strong> %s</p>
                    <p><strong>To:</strong> %s</p>
                    <p><strong>Date:</strong> %s</p>
                </div>
                <p>Thank you for choosing our service!</p>
                <p>Best regards,<br>Bus Book Team</p>
            </div>
            """,
            name, id, registrationNumber, source, destination, date);
    }

    public static void sendSeatSwapConfirmation(String toEmail, String userName, 
            int ticketId, String busNumber, String departure, String destination, 
            String date, String oldSeat, String newSeat) {
        
        new Thread(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", Config.HOST);
                props.put("mail.smtp.port", Config.PORT);

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                            Config.USERNAME,
                            Config.PASSWORD
                        );
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(Config.FROM_EMAIL, Config.FROM_NAME));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject("Seat Swap Confirmation - Ticket #" + ticketId);
                
                String htmlContent = String.format("""
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h1 style="color: #1976D2;">Seat Swap Confirmation</h1>
                        <p>Dear %s,</p>
                        <p>Your seat has been successfully swapped. Here are your updated ticket details:</p>
                        <div style="background-color: #f5f5f5; padding: 20px; border-radius: 8px; margin: 20px 0;">
                            <p><strong>Ticket ID:</strong> %d</p>
                            <p><strong>Bus Number:</strong> %s</p>
                            <p><strong>From:</strong> %s</p>
                            <p><strong>To:</strong> %s</p>
                            <p><strong>Date:</strong> %s</p>
                            <p><strong>Previous Seat:</strong> %s</p>
                            <p><strong>New Seat:</strong> %s</p>
                        </div>
                        <p>Thank you for using our service!</p>
                        <p>Best regards,<br>Bus Book Team</p>
                    </div>
                    """,
                    userName, ticketId, busNumber, departure, destination, date, oldSeat, newSeat);
                
                message.setContent(htmlContent, "text/html; charset=utf-8");
                Transport.send(message);
                Log.d("EmailSender", "Seat swap confirmation email sent successfully to " + toEmail);
            } catch (Exception e) {
                Log.e("EmailSender", "Failed to send seat swap confirmation email", e);
            }
        }).start();
    }
}