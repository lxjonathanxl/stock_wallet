package com.api.notificationApi.service;

import com.api.notificationApi.dto.NotificationRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailSenderService {

    private final JavaMailSender javaMailSender;

    public EmailSenderService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public Boolean sendEmail(NotificationRequest notification) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        String email = notification.getEmail();
        String subject = "a new transaction was made in your account";
        String text = "<p>Hello, "+ notification.getUsername() + "</p>" +
                "<p>A new transaction was made on your account wallet</p>"
                + "<p>action: "+ notification.getAction() + "</p>"
                + "<p>Stock: "+ notification.getName() + "</p>"
                + "<p>price: "+ notification.getPrice() + "</p>"
                + "<p>Quant: "+ notification.getQuant() + "</p>"
                + "<p>date: "+ notification.getDate() + "</p>"
                ;

        helper.setFrom("stockwalletfinance@gmail.com", "Stock Wallet");
        helper.setTo(email);

        helper.setSubject(subject);
        helper.setText(text, true);

        javaMailSender.send(message);
        return true;
    }
}
