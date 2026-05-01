package edu.cit.gako.brainbox.shared.email;

/**
 * Strategy interface for email delivery.
 * Concrete strategies: SmtpEmailSender (debug/dev) and ResendEmailSender (production).
 */
public interface EmailSender {
    void send(String to, String subject, String htmlContent);
}
