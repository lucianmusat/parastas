package online.lucianmusat.Parastas;

import online.lucianmusat.Parastas.application.services.EmailService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

import org.mockito.Mock;

public class TestableEmailService extends EmailService {

    @Mock
    private JavaMailSender mailSender;

    @Override
    protected JavaMailSender getMailSender() {
        return mailSender;
    }

    @Override
    protected SimpleMailMessage createMessage(String recipient, String subject, String body) {
        SimpleMailMessage message = super.createMessage(recipient, subject, body);
        return message;
    }
}
