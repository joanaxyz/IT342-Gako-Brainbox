package com.example.brainbox_api.email;

import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {

    public String buildVerificationEmail(String name, String verificationLink) {
        return "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;\">" +
                "<h2 style=\"color: #333;\">Welcome to Brainbox, " + name + "!</h2>" +
                "<p>Please verify your email address by clicking the button below:</p>" +
                "<div style=\"text-align: center; margin: 30px 0;\">" +
                "<a href=\"" + verificationLink + "\" style=\"background-color: #007bff; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;\">Verify Email</a>" +
                "</div>" +
                "<p>If the button doesn't work, you can copy and paste this link into your browser:</p>" +
                "<p style=\"word-break: break-all; color: #007bff;\">" + verificationLink + "</p>" +
                "<hr style=\"border: 0; border-top: 1px solid #e0e0e0; margin: 20px 0;\">" +
                "<p style=\"font-size: 12px; color: #777;\">If you didn't create an account, you can safely ignore this email.</p>" +
                "</div>";
    }

    public String buildPasswordResetEmail(String name, String code) {
        return "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;\">" +
                "<h2 style=\"color: #333;\">Password Reset Request</h2>" +
                "<p>Hi " + name + ",</p>" +
                "<p>We received a request to reset your password. Please use the following verification code to proceed:</p>" +
                "<div style=\"text-align: center; margin: 30px 0;\">" +
                "<h1 style=\"background-color: #f8f9fa; color: #333; padding: 20px; letter-spacing: 5px; border-radius: 5px; display: inline-block; border: 1px solid #dee2e6;\">" + code + "</h1>" +
                "</div>" +
                "<p>This code will expire in 5 minutes. If you didn't request a password reset, please ignore this email.</p>" +
                "<hr style=\"border: 0; border-top: 1px solid #e0e0e0; margin: 20px 0;\">" +
                "<p style=\"font-size: 12px; color: #777;\">Brainbox Team</p>" +
                "</div>";
    }
}
