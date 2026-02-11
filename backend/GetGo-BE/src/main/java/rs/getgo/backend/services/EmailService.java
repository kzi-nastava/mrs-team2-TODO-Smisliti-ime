package rs.getgo.backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.Passenger;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Value("${app.backend.base-url:http://localhost:8080}")
    private String backendBaseUrl;

    @Value("${app.web.base-url:http://localhost:4200}")
    private String webBaseUrl;

    @Value("${app.mobile.scheme:getgo}")
    private String mobileScheme;

    public void sendActivationEmail(String toEmail, String activationToken) {

        // Deep link (works only if the mobile app registers it)
        String mobileDeepLink = mobileScheme + "://activate/?token=" + activationToken;

        // HTTP fallback (must be reachable from the phone; configure app.backend.base-url accordingly)
        String mobileHttpLink = backendBaseUrl + "/api/auth/activate-mobile?token=" + activationToken;

        // Web fallback (if you still want it)
        String webLink = webBaseUrl + "/activate?token=" + activationToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Activate Your GetGo Account");
        message.setText(
                "Your account has been created.\n\n" +
                "Activate your account:\n\n" +
                /*"1) Mobile app (if installed):\n" +
                mobileDeepLink + "\n\n" +*/
                "1) Browser fallback (works on phone):\n" +
                mobileHttpLink + "\n\n" +
                "2) Web app:\n" +
                webLink + "\n\n" +
                "This activation link is valid for 24 hours.\n"
        );

        mailSender.send(message);
    }

    public void sendDriverActivationEmail(String toEmail, String activationToken) {
        String activationLink = webBaseUrl + "/driver/activate/" + activationToken;

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

        String mobileDeepLink = mobileScheme + "://reset-password/?token=" + resetUrl; // NOTE: resetUrl may be full URL today

        // If caller passes a full URL, try to extract token from it (best-effort) to build correct links.
        String token = null;
        int idx = resetUrl != null ? resetUrl.indexOf("token=") : -1;
        if (idx >= 0) {
            token = resetUrl.substring(idx + "token=".length());
        }

        String mobileHttpLink = token == null
                ? (backendBaseUrl + "/api/auth/reset-password-mobile")
                : (backendBaseUrl + "/api/auth/reset-password-mobile?token=" + token);

        String webLink = resetUrl;


        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("GetGo - Password reset");
        message.setText(
                "Hello,\n\n" +
                "To reset your password (valid for 15 minutes), use one of the links below:\n\n" +
                /*"1) Mobile app (if installed):\n" +
                (token == null ? "(missing token in reset URL)\n" : (mobileScheme + "://reset-password/?token=" + token + "\n")) +
              */"\n1) Phone browser fallback:\n" +
                mobileHttpLink + "\n\n" +
                "2) Web app:\n" +
                webLink + "\n\n" +
                "If you didn't request this, ignore this email.\n\nRegards,\nGetGo Team"
        );

        mailSender.send(message);
    }

    public void sendRideFinishedEmail(
            String toEmail,
            String passengerName,
            Long rideId,
            Long passengerId
    ) {

        String ratingLink = webBaseUrl + "/rides/" + rideId + "/rate";


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

    public void sendLinkedPassengerEmail(Passenger passenger, ActiveRide ride) {
        if (passenger == null || ride == null) return;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(passenger.getEmail());
        message.setSubject("You have been added to a ride!");

        String text = "Hi " + passenger.getName() + ",\n\n" +
                "You have been added to a ride (ID: " + ride.getId() + ").\n" +
                "The driver has accepted the ride!\n\n" +
                "Pickup location: " + ride.getRoute().getStartingPoint() + "\n" +
                "Destination: " + ride.getRoute().getEndingPoint() + "\n" +
                (ride.getScheduledTime() != null ? "Scheduled time: " + ride.getScheduledTime() + "\n\n" : "\n") +
                "You can track the ride in the app.\n\n" +
                "Best regards,\n" +
                "GetGo Team";

        message.setText(text);

        mailSender.send(message);
    }

}