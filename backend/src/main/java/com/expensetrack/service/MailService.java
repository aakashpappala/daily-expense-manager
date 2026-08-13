package com.expensetrack.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.resend.com")
            .build();

    public void sendNotificationEmail(String to, String message) {

        System.out.println("EMAIL DEBUG - Resend called for: " + to);

        try {

            Map<String, Object> requestBody = Map.of(
                    "from", "Daily Expense Manager <onboarding@resend.dev>",
                    "to", new String[]{to},
                    "subject", "Daily Expense Manager - Alert",
                    "text",
                    "Hello,\n\n" +
                            "You have a new expense alert:\n\n" +
                            message +
                            "\n\n" +
                            "Please check your Daily Expense Manager dashboard.\n\n" +
                            "Regards,\n" +
                            "Daily Expense Manager"
            );

            String response = restClient.post()
                    .uri("/emails")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + resendApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            System.out.println("EMAIL DEBUG - Resend response: " + response);

        } catch (Exception e) {

            System.err.println("Failed to send notification email to " + to);
            e.printStackTrace();
        }
    }
}