package edu.cit.gako.brainbox.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * EmailService is a thin facade over the EmailSender strategy.
 * The concrete sender (SMTP or Resend) is selected at startup by EmailSenderFactory.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailSenderFactory emailSenderFactory;

    public void sendEmail(String to, String subject, String htmlContent) {
        emailSenderFactory.getSender().send(to, subject, htmlContent);
    }
}
