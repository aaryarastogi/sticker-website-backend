package com.stickers.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:}")
    private String fromEmail;
    
    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;
    
    public void sendStickerUnpublishedEmail(String toEmail, String userName, String stickerName) {
        if (!emailEnabled || mailSender == null || fromEmail == null || fromEmail.isEmpty()) {
            System.out.println("Email service is disabled or not configured. Skipping email to: " + toEmail);
            System.out.println("Email would have been sent with subject: Your sticker '" + stickerName + "' has been unpublished");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your Sticker Has Been Unpublished - Stickkery");
            
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "We wanted to inform you that your sticker '%s' has been unpublished by an administrator.\n\n" +
                "Important Information:\n" +
                "• Your sticker remains in your profile and in our database\n" +
                "• It is currently not visible to other users on the main website\n" +
                "• It may be published again in the future\n\n" +
                "If you have any questions or concerns, please don't hesitate to contact our support team.\n\n" +
                "Thank you for using Stickkery!\n\n" +
                "Best regards,\n" +
                "The Stickkery Team",
                userName != null ? userName : "User",
                stickerName
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            System.out.println("Email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void sendStickerRejectedEmail(String toEmail, String userName, String stickerName, String rejectionNote) {
        System.out.println("=== EMAIL SERVICE DEBUG ===");
        System.out.println("Email enabled: " + emailEnabled);
        System.out.println("MailSender is null: " + (mailSender == null));
        System.out.println("From email: " + fromEmail);
        System.out.println("To email: " + toEmail);
        System.out.println("User name: " + userName);
        System.out.println("Sticker name: " + stickerName);
        System.out.println("Rejection note: " + rejectionNote);
        
        if (!emailEnabled) {
            System.out.println("ERROR: Email service is disabled in configuration");
            return;
        }
        
        if (mailSender == null) {
            System.err.println("ERROR: JavaMailSender is null - mail configuration failed");
            return;
        }
        
        if (fromEmail == null || fromEmail.isEmpty()) {
            System.err.println("ERROR: From email is not configured");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your Sticker Has Been Rejected - Stickkery");
            
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "We regret to inform you that your sticker '%s' has been rejected by our admin team.\n\n",
                userName != null ? userName : "User",
                stickerName
            );
            
            if (rejectionNote != null && !rejectionNote.trim().isEmpty()) {
                emailBody += String.format(
                    "Reason for rejection:\n%s\n\n",
                    rejectionNote.trim()
                );
            }
            
            emailBody += String.format(
                "What this means:\n" +
                "• Your sticker will not be published on the main website\n" +
                "• You can create and submit new stickers at any time\n" +
                "• Please review our guidelines before submitting new stickers\n\n" +
                "If you have any questions or would like to discuss this decision, please don't hesitate to contact our support team.\n\n" +
                "Thank you for using Stickkery!\n\n" +
                "Best regards,\n" +
                "The Stickkery Team"
            );
            
            message.setText(emailBody);
            
            System.out.println("Attempting to send email...");
            System.out.println("From: " + message.getFrom());
            System.out.println("To: " + message.getTo()[0]);
            System.out.println("Subject: " + message.getSubject());
            
            mailSender.send(message);
            System.out.println("✓ Rejection email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("✗ Failed to send rejection email to " + toEmail);
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            
            // Print more details about the exception
            if (e.getCause() != null) {
                System.err.println("Caused by: " + e.getCause().getClass().getName());
                System.err.println("Cause message: " + e.getCause().getMessage());
            }
        }
    }
    
    public void sendUserWarningEmail(String toEmail, String userName) {
        System.out.println("=== USER WARNING EMAIL DEBUG ===");
        System.out.println("Email enabled: " + emailEnabled);
        System.out.println("MailSender is null: " + (mailSender == null));
        System.out.println("From email: " + fromEmail);
        System.out.println("To email: " + toEmail);
        System.out.println("User name: " + userName);
        
        if (!emailEnabled) {
            System.out.println("ERROR: Email service is disabled in configuration");
            return;
        }
        
        if (mailSender == null) {
            System.err.println("ERROR: JavaMailSender is null - mail configuration failed");
            return;
        }
        
        if (fromEmail == null || fromEmail.isEmpty()) {
            System.err.println("ERROR: From email is not configured");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Warning from Stickkery Admin - Account Action Required");
            
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "This is an official warning from the Stickkery administration team.\n\n" +
                "You have been warned for violating our community guidelines or terms of service.\n\n" +
                "IMPORTANT NOTICE:\n" +
                "• This is your first and final warning\n" +
                "• If you continue with the same activity or violate our guidelines again, your account will be permanently disabled\n" +
                "• Your account access may be restricted until further review\n\n" +
                "We take our community guidelines seriously to ensure a safe and positive environment for all users.\n\n" +
                "If you believe this warning was issued in error, or if you have any questions, please contact our support team immediately.\n\n" +
                "Please review our terms of service and community guidelines to ensure compliance.\n\n" +
                "Thank you for your understanding.\n\n" +
                "Best regards,\n" +
                "The Stickkery Administration Team",
                userName != null ? userName : "User"
            );
            
            message.setText(emailBody);
            
            System.out.println("Attempting to send warning email...");
            System.out.println("From: " + message.getFrom());
            System.out.println("To: " + message.getTo()[0]);
            System.out.println("Subject: " + message.getSubject());
            
            mailSender.send(message);
            System.out.println("✓ User warning email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("✗ Failed to send warning email to " + toEmail);
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            
            if (e.getCause() != null) {
                System.err.println("Caused by: " + e.getCause().getClass().getName());
                System.err.println("Cause message: " + e.getCause().getMessage());
            }
        }
    }
    
    public void sendAccountDeactivatedEmail(String toEmail, String userName) {
        System.out.println("=== ACCOUNT DEACTIVATION EMAIL DEBUG ===");
        System.out.println("Email enabled: " + emailEnabled);
        System.out.println("MailSender is null: " + (mailSender == null));
        System.out.println("From email: " + fromEmail);
        System.out.println("To email: " + toEmail);
        System.out.println("User name: " + userName);
        
        if (!emailEnabled) {
            System.out.println("ERROR: Email service is disabled in configuration");
            return;
        }
        
        if (mailSender == null) {
            System.err.println("ERROR: JavaMailSender is null - mail configuration failed");
            return;
        }
        
        if (fromEmail == null || fromEmail.isEmpty()) {
            System.err.println("ERROR: From email is not configured");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your Account Has Been Deactivated - Stickkery");
            
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "We are writing to inform you that your Stickkery account has been deactivated by an administrator.\n\n" +
                "What this means:\n" +
                "• You will no longer be able to log in to your account\n" +
                "• Your account and data remain in our system\n" +
                "• Your stickers and profile information are preserved\n\n" +
                "If you believe this deactivation was made in error, or if you have any questions about this action, " +
                "please contact our support team immediately. We are here to help resolve any issues.\n\n" +
                "You can request account reactivation by contacting our support team. We will review your request " +
                "and may reactivate your account if appropriate.\n\n" +
                "Thank you for your understanding.\n\n" +
                "Best regards,\n" +
                "The Stickkery Administration Team",
                userName != null ? userName : "User"
            );
            
            message.setText(emailBody);
            
            System.out.println("Attempting to send account deactivation email...");
            System.out.println("From: " + message.getFrom());
            System.out.println("To: " + message.getTo()[0]);
            System.out.println("Subject: " + message.getSubject());
            
            mailSender.send(message);
            System.out.println("✓ Account deactivation email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("✗ Failed to send account deactivation email to " + toEmail);
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            
            if (e.getCause() != null) {
                System.err.println("Caused by: " + e.getCause().getClass().getName());
                System.err.println("Cause message: " + e.getCause().getMessage());
            }
        }
    }
    
    public void sendAccountActivatedEmail(String toEmail, String userName) {
        System.out.println("=== ACCOUNT ACTIVATION EMAIL DEBUG ===");
        System.out.println("Email enabled: " + emailEnabled);
        System.out.println("MailSender is null: " + (mailSender == null));
        System.out.println("From email: " + fromEmail);
        System.out.println("To email: " + toEmail);
        System.out.println("User name: " + userName);
        
        if (!emailEnabled) {
            System.out.println("ERROR: Email service is disabled in configuration");
            return;
        }
        
        if (mailSender == null) {
            System.err.println("ERROR: JavaMailSender is null - mail configuration failed");
            return;
        }
        
        if (fromEmail == null || fromEmail.isEmpty()) {
            System.err.println("ERROR: From email is not configured");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your Account Has Been Reactivated - Stickkery");
            
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "We are pleased to inform you that your Stickkery account has been reactivated by an administrator.\n\n" +
                "What this means:\n" +
                "• You can now log in to your account again\n" +
                "• All your data, stickers, and profile information are intact\n" +
                "• You have full access to all account features\n\n" +
                "Welcome back! We're glad to have you as part of our community.\n\n" +
                "If you have any questions or need assistance, please don't hesitate to contact our support team.\n\n" +
                "Thank you for being a part of Stickkery!\n\n" +
                "Best regards,\n" +
                "The Stickkery Administration Team",
                userName != null ? userName : "User"
            );
            
            message.setText(emailBody);
            
            System.out.println("Attempting to send account activation email...");
            System.out.println("From: " + message.getFrom());
            System.out.println("To: " + message.getTo()[0]);
            System.out.println("Subject: " + message.getSubject());
            
            mailSender.send(message);
            System.out.println("✓ Account activation email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("✗ Failed to send account activation email to " + toEmail);
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            
            if (e.getCause() != null) {
                System.err.println("Caused by: " + e.getCause().getClass().getName());
                System.err.println("Cause message: " + e.getCause().getMessage());
            }
        }
    }
}

