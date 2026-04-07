package edu.cit.gako.brainbox.email;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete Strategy: sends email via the Resend API (used in production).
 */
public class ResendEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailSender.class);

    private final Resend resend;
    private final String fromEmail;

    public ResendEmailSender(Resend resend, String fromEmail) {
        this.resend = resend;
        this.fromEmail = fromEmail;
    }

    @Override
    public void send(String to, String subject, String htmlContent) {
        CreateEmailOptions request = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(to)
                .subject(subject)
                .html(htmlContent)
                .build();
        try {
            CreateEmailResponse data = resend.emails().send(request);
            log.info("Email sent successfully via Resend to: {} | ID: {}", to, data.getId());
        } catch (ResendException e) {
            log.error("Failed to send email via Resend to: {}", to, e);
            throw new RuntimeException("Failed to send email. Please try again later.", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending email via Resend to: {}", to, e);
            throw new RuntimeException("An unexpected error occurred during email delivery.", e);
        }
    }
}
