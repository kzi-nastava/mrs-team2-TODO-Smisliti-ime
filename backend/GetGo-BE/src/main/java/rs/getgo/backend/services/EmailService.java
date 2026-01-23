package rs.getgo.backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


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

    public void sendDriverActivationEmail(String toEmail, String activationToken) {
        String activationLink = "http://localhost:4200/driver/activate/" + activationToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Activate Your Driver Account");
        message.setText(
                "Your driver account has been created.\n\n" +
                        "Click the link below to set your password:\n" +
                        activationLink + "\n\n" +
                        "This link expires in 24 hours.\n\n"
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

    public void sendRideFinishedEmail(
            String toEmail,
            String passengerName,
            Long rideId,
            Long passengerId
    ) {

        String ratingLink = "http://localhost:4200/rides/" + rideId + "/rate";


        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Your GetGo ride has been completed");
        message.setText(
                "Hello " + passengerName + ",\n\n" +
                        "Your ride has been successfully completed.\n\n" +
                        "We would appreciate it if you could rate your ride:\n" +
                        ratingLink + "\n\n" +
                        "Thank you for choosing GetGo!\n\n" +
                        "Best regards,\nGetGo Team"
        );

        mailSender.send(message);
    }



}