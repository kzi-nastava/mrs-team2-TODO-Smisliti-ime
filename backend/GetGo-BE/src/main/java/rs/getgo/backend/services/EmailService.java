package rs.getgo.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    public void sendActivationEmail(String toEmail, String activationToken) {
        String activationLink = "http://localhost:4200/activate?token=" + activationToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Activate Your GetGo Account");
        message.setText(
                "Your account has been created.\n\n" +
                        "Click the link below to activate your account (valid 24 hours):\n" +
                        activationLink + "\n\n" +
                        "If you didn't request this, ignore this email.\n\n"
        );

        mailSender.send(message);
    }

    public void sendResetEmail(String toEmail, String resetUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("GetGo - Password reset");
        message.setText(
                "Hello,\n\nTo reset your password click the link below (valid for 15 minutes):\n\n"
                        + resetUrl + "\n\nIf you didn't request this, ignore this email.\n\nRegards,\nGetGo Team"
        );
        mailSender.send(message);
    }
}