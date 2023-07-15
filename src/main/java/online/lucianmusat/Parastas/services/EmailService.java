package online.lucianmusat.Parastas.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import online.lucianmusat.Parastas.controllers.MainController;

@Service
public class EmailService {

    private static final String FROM_EMAIL = "noreply@lucianmusat.online";

    private static final Logger logger = LogManager.getLogger(MainController.class);
    private JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendEmail(String recipient, String subject, String body) {
        logger.info("Sending email to " + recipient);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(FROM_EMAIL);
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            ex.printStackTrace();
        }
    }
}
