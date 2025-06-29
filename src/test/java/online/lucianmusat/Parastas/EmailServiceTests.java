package online.lucianmusat.Parastas;

import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;


@SpringBootTest
public class EmailServiceTests {

    @InjectMocks
    private TestableEmailService emailService;

    @Mock
    JavaMailSender mailSender;

    private static final String TO_EMAIL = "test@domain.com";
    private static final String SUBJECT = "Test Subject";
    private static final String BODY = "Test Body";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        emailService.setMailSender(mailSender);
    }

    @Test
    public void sendEmail() {
        emailService.sendEmail(TO_EMAIL, SUBJECT, BODY);
        SimpleMailMessage message = emailService.createMessage(TO_EMAIL, SUBJECT, BODY);
        verify(mailSender, times(1)).send(message);
    }

    @Test
    public void testEmptyRecipient() {
        emailService.sendEmail("", SUBJECT, BODY);
        verify(mailSender, times(0)).send(any(SimpleMailMessage.class));
    }

    @Test
    public void testEmptySubject() {
        emailService.sendEmail(TO_EMAIL, "", BODY);
        verify(mailSender, times(0)).send(any(SimpleMailMessage.class));
    }

    @Test
    public void testEmptyBody() {
        emailService.sendEmail(TO_EMAIL, SUBJECT, "");
        SimpleMailMessage message = emailService.createMessage(TO_EMAIL, SUBJECT, "");
        verify(mailSender, times(1)).send(message);
    }

}
