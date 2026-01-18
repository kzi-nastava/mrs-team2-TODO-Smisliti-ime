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
}