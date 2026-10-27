package com.homie.services;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Default transport (spring.mail.*): Mailpit in dev, any SMTP server elsewhere. */
@Component
@ConditionalOnExpression("'${app.resend-api-key:}'.isEmpty()")
public class SmtpMailTransport implements MailTransport {

    private final JavaMailSender mailSender;

    public SmtpMailTransport(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(SimpleMailMessage message) {
        mailSender.send(message);
    }
}
