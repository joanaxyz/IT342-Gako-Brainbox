package edu.cit.gako.brainbox.email;

import com.resend.Resend;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Factory Method: creates the appropriate EmailSender strategy based on the
 * runtime environment (debug → SMTP, production → Resend).
 */
@Component
public class EmailSenderFactory {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderFactory.class);

    @Value("${app.debug}")
    private boolean debug;

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.from.email}")
    private String resendFromEmail;

    @Value("${spring.mail.from:noreply@brainbox.com}")
    private String smtpMailFrom;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    private EmailSender emailSender;

    @PostConstruct
    public void init() {
        this.emailSender = create();
    }

    /**
     * Factory method — returns the concrete EmailSender strategy.
     */
    public EmailSender create() {
        if (debug) {
            if (mailSender == null) {
                log.warn("Debug mode active but JavaMailSender is not configured — check SMTP env vars.");
                throw new IllegalStateException("SMTP email service is not configured.");
            }
            log.info("Email sender initialized in debug mode (SMTP).");
            return new SmtpEmailSender(mailSender, smtpMailFrom);
        }

        if (resendApiKey == null || resendApiKey.isBlank() || resendApiKey.equals("${RESEND_API_KEY}")) {
            log.warn("Resend API key is not configured correctly. Emails will not be sent.");
            throw new IllegalStateException("Email service is not configured. Please contact support.");
        }
        if (resendFromEmail == null || resendFromEmail.isBlank() || resendFromEmail.equals("${RESEND_FROM_EMAIL}")) {
            log.warn("Resend from-email is not configured.");
            throw new IllegalStateException("Email sender address is not configured. Please contact support.");
        }

        log.info("Email sender initialized with Resend (production).");
        return new ResendEmailSender(new Resend(resendApiKey), resendFromEmail);
    }

    public EmailSender getSender() {
        return emailSender;
    }
}
