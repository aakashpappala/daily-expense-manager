package com.expensetrack.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendNotificationEmail(String to, String message) {

        try {
            SimpleMailMessage mail = new SimpleMailMessage();

            mail.setTo(to);
            mail.setSubject("Daily Expense Manager - Alert");

            mail.setText(
                    "Hello,\n\n" +
                            "You have a new expense alert:\n\n" +
                            message +
                            "\n\n" +
                            "Please check your Daily Expense Manager dashboard.\n\n" +
                            "Regards,\n" +
                            "Daily Expense Manager"
            );

            mailSender.send(mail);

            System.out.println(
                    "Notification email sent successfully to: " + to
            );

        } catch (Exception e) {
            System.err.println(
                    "Failed to send notification email to " + to
            );
            System.err.println("Reason: " + e.getMessage());
        }
    }
}