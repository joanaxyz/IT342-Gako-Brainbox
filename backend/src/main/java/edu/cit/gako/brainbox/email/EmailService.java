package edu.cit.gako.brainbox.email;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private Resend resend;

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    @Value("${app.debug}")
    private boolean debug;

    @Value("${spring.mail.from:noreply@brainbox.com}")
    private String mailFrom;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @PostConstruct
    public void init() {
        if (debug) {
            if (mailSender == null) {
                log.warn("Debug mode is enabled but JavaMailSender is not configured. Check SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD.");
            } else {
                log.info("Email service initialized in debug mode (SMTP).");
            }
        } else {
            if (apiKey == null || apiKey.isEmpty() || apiKey.equals("${RESEND_API_KEY}")) {
                log.warn("Resend API key is not configured correctly. Emails will not be sent.");
                return;
            }
            this.resend = new Resend(apiKey);
            log.info("Email service initialized with Resend.");
        }
    }

    public void sendEmail(String to, String subject, String htmlContent) {
        if (debug) {
            sendViaSMTP(to, subject, htmlContent);
        } else {
            sendViaResend(to, subject, htmlContent);
        }
    }

    private void sendViaSMTP(String to, String subject, String htmlContent) {
        if (mailSender == null) {
            log.error("Cannot send email via SMTP: JavaMailSender is not configured. Ensure SMTP environment variables are set.");
            throw new IllegalStateException("SMTP email service is not configured.");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Email sent successfully via SMTP to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email via SMTP to: {} | Cause: {} | Root cause: {}", to, e.getMessage(), getRootCause(e).getMessage(), e);
            throw new RuntimeException("Failed to send email via SMTP.", e);
        }
    }

    private void sendViaResend(String to, String subject, String htmlContent) {
        if (resend == null) {
            log.error("Cannot send email via Resend: client is not initialized. Check the RESEND_API_KEY.");
            throw new IllegalStateException("Email service is not configured. Please contact support.");
        }

        if (fromEmail == null || fromEmail.isEmpty() || fromEmail.equals("${RESEND_FROM_EMAIL}")) {
            log.error("Cannot send email via Resend: RESEND_FROM_EMAIL is not configured.");
            throw new IllegalStateException("Email sender is not configured. Please contact support.");
        }

        CreateEmailOptions sendEmailRequest = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(to)
                .subject(subject)
                .html(htmlContent)
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(sendEmailRequest);
            log.info("Email sent successfully via Resend to: {} | ID: {}", to, data.getId());
        } catch (ResendException e) {
            log.error("Failed to send email via Resend to: {} | Cause: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email. Please try again later.", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending email via Resend to: {} | Cause: {}", to, e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred during email delivery.", e);
        }
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        return (cause == null) ? throwable : getRootCause(cause);
    }
}
