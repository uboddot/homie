package com.homie.services;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;

/** How {@link MailService} delivers a mail: SMTP by default, Resend's HTTP API when RESEND_API_KEY is set. */
public interface MailTransport {

    /** @throws MailException if the mail couldn't be sent */
    void send(SimpleMailMessage message);
}
