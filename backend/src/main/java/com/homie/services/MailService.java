package com.homie.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final String from;

    public MailService(JavaMailSender mailSender, @Value("${app.mail-from}") String from) {
        this.mailSender = mailSender;
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
        mailSender.send(message);
    }
}
