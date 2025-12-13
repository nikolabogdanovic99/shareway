package ch.zhaw.shareway.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import ch.zhaw.shareway.model.Mail;

@ExtendWith(MockitoExtension.class)
public class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailService mailService;

    @Test
    void testSendMailSuccess() {
        Mail mail = new Mail();
        mail.setTo("recipient@test.com");
        mail.setSubject("Test Subject");
        mail.setMessage("Test Message");

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        boolean result = mailService.sendMail(mail);

        assertTrue(result);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendMailFailure() {
        Mail mail = new Mail();
        mail.setTo("recipient@test.com");
        mail.setSubject("Test Subject");
        mail.setMessage("Test Message");

        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        boolean result = mailService.sendMail(mail);

        assertFalse(result);
    }
}
