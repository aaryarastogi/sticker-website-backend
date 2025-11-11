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
}

