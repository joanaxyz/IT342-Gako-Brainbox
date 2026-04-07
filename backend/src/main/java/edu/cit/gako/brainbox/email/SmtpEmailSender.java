package edu.cit.gako.brainbox.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * Concrete Strategy: sends email via SMTP (used in debug / dev environments).
 */
public class SmtpEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);

    private final JavaMailSender mailSender;
    private final String mailFrom;

    public SmtpEmailSender(JavaMailSender mailSender, String mailFrom) {
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
    }

    @Override
    public void send(String to, String subject, String htmlContent) {
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
            log.error("Failed to send email via SMTP to: {}", to, e);
            throw new RuntimeException("Failed to send email via SMTP.", e);
        }
    }
}
