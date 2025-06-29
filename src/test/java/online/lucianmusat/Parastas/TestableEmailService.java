package online.lucianmusat.Parastas;

import online.lucianmusat.Parastas.application.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

import org.mockito.Mock;

public class TestableEmailService extends EmailService {

    @Mock
    private JavaMailSender mailSender;

    @Autowired
    public TestableEmailService(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    protected JavaMailSender getMailSender() {
        return mailSender;
    }

    @Override
    protected SimpleMailMessage createMessage(String recipient, String subject, String body) {
        return super.createMessage(recipient, subject, body);
    }
}
