package online.lucianmusat.Parastas.application.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationContext;

import online.lucianmusat.Parastas.presentation.controllers.MainController;

@Service
public class EmailService {

    private static final String FROM_EMAIL = "noreply@lucianmusat.nl";

    private static final Logger logger = LogManager.getLogger(MainController.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Async
    public void sendEmail(String recipient, String subject, String body) {
        if (recipient == null || recipient.isEmpty()) {
            logger.error("Recipient is empty, not sending email");
            return;
        }
        if (subject == null || subject.isEmpty()) {
            logger.error("Subject is empty, not sending email");
            return;
        }
        logger.info("Sending email to " + recipient);
        JavaMailSender mailSender = getMailSender();
        SimpleMailMessage message = createMessage(recipient, subject, body);
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            ex.printStackTrace();
        }
    }

    // Separated for easier unittesting
    protected JavaMailSender getMailSender() {
        return applicationContext.getBean(JavaMailSender.class);
    }

    protected SimpleMailMessage createMessage(String recipient, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(FROM_EMAIL);
        return message;
    }
}
