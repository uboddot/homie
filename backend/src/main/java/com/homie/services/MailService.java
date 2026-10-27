package com.homie.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final MailTransport transport;
    private final String from;

    public MailService(MailTransport transport, @Value("${app.mail-from}") String from) {
        this.transport = transport;
        this.from = from;
    }

    public void sendVerificationMail(String to, String username, String link) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Confirm your Homie account");
        message.setText("""
                Hi %s,

                please confirm your email address by opening this link:

                %s

                The link is valid for 24 hours. If you didn't register at Homie, just ignore this mail.
                """.formatted(username, link));
        transport.send(message);
    }
}
