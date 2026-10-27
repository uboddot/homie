package com.homie.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Sends mails over Resend's HTTP API (https://resend.com/docs/api-reference/emails/send-email).
 * Used on Railway, whose free and hobby plans block outgoing SMTP.
 */
@Component
@ConditionalOnExpression("!'${app.resend-api-key:}'.isEmpty()")
public class ResendMailTransport implements MailTransport {

    private final RestClient client;

    public ResendMailTransport(@Value("${app.resend-api-key}") String apiKey) {
        this.client = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public void send(SimpleMailMessage message) {
        Map<String, Object> body = Map.of(
                "from", message.getFrom(),
                "to", List.of(message.getTo()),
                "subject", message.getSubject(),
                "text", message.getText());
        try {
            client.post().uri("/emails").contentType(MediaType.APPLICATION_JSON).body(body).retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            throw new MailSendException("Resend rejected the mail", e);
        }
    }
}
