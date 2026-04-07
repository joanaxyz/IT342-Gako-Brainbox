package edu.cit.gako.brainbox.email;

/**
 * Strategy interface for email delivery.
 * Concrete strategies: SmtpEmailSender (debug/dev) and ResendEmailSender (production).
 */
public interface EmailSender {
    void send(String to, String subject, String htmlContent);
}
