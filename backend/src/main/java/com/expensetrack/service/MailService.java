package com.expensetrack.service;
import org.springframework.scheduling.annotation.Async;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    @Async
    public void sendNotificationEmail(String to, String message) {

        System.out.println("EMAIL DEBUG - Brevo SMTP called for: " + to);

        try {

            SimpleMailMessage mail = new SimpleMailMessage();

            mail.setFrom("dailyexpense.alerts@gmail.com");
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

            System.out.println("EMAIL DEBUG - Brevo SMTP email sent successfully");

        } catch (Exception e) {

            System.err.println("EMAIL ERROR TYPE: " + e.getClass().getName());
            System.err.println("EMAIL ERROR MESSAGE: " + e.getMessage());

            if (e.getCause() != null) {
                System.err.println("EMAIL ROOT CAUSE: " + e.getCause().getClass().getName());
                System.err.println("EMAIL ROOT MESSAGE: " + e.getCause().getMessage());
            }

            e.printStackTrace();
        }
    }
}