package rs.getgo.backend.controllers;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import rs.getgo.backend.dtos.driver.GetDriverLocationDTO;
import rs.getgo.backend.dtos.panic.GetPanicAlertDTO;
import rs.getgo.backend.dtos.message.GetMessageDTO;
import rs.getgo.backend.dtos.ride.GetDriverActiveRideDTO;
import rs.getgo.backend.dtos.ride.GetRideStatusUpdateDTO;
import rs.getgo.backend.dtos.ride.GetRideFinishedDTO;
import rs.getgo.backend.dtos.ride.GetRideStoppedEarlyDTO;

import java.time.LocalDateTime;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Notify specific driver about ride assignment
     */
    public void notifyDriverRideAssigned(String driverEmail, GetDriverActiveRideDTO rideDTO) {
        messagingTemplate.convertAndSend(
                "/socket-publisher/driver/" + driverEmail + "/ride-assigned",
                rideDTO
        );
    }

    /**
     * Broadcast driver location update
     */
    public void broadcastDriverLocation(String driverEmail, GetDriverLocationDTO locationDTO) {
        messagingTemplate.convertAndSend(
                "/socket-publisher/driver/" + driverEmail + "/location",
                locationDTO
        );
    }

    /**
     * Broadcast driver location to ride (for passengers tracking)
     */
    public void broadcastDriverLocationToRide(Long rideId, GetDriverLocationDTO locationDTO) {
        messagingTemplate.convertAndSend(
                "/socket-publisher/ride/" + rideId + "/driver-location",
                locationDTO
        );
    }

    public void notifyDriverStatusUpdate(String driverEmail, Long rideId, String status) {
        GetRideStatusUpdateDTO update = new GetRideStatusUpdateDTO(
                rideId,
                status,
                null,
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend(
                "/socket-publisher/driver/" + driverEmail + "/status-update",
                update
        );
    }

    public void notifyDriverRideFinished(String driverEmail, Long rideId, Double price,
                                         LocalDateTime startTime, LocalDateTime endTime, Long driverId) {
        GetRideFinishedDTO completion = new GetRideFinishedDTO(
                rideId,
                "FINISHED",
                price,
                startTime,
                endTime,
                java.time.Duration.between(startTime, endTime).toMinutes(),
                driverId
        );

        messagingTemplate.convertAndSend(
                "/socket-publisher/driver/" + driverEmail + "/ride-finished",
                completion
        );
    }

    public void notifyPassengerRideStatusUpdate(Long rideId, String status, String message) {
        GetRideStatusUpdateDTO update = new GetRideStatusUpdateDTO(
                rideId,
                status,
                message,
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend(
                "/socket-publisher/ride/" + rideId + "/status-update",
                update
        );
    }

    public void notifyPassengerRideFinished(Long rideId, Double price, LocalDateTime startTime, LocalDateTime endTime, Long driverId) {
        GetRideFinishedDTO completion = new GetRideFinishedDTO(
                rideId,
                "FINISHED",
                price,
                startTime,
                endTime,
                java.time.Duration.between(startTime, endTime).toMinutes(),
                driverId
        );

        messagingTemplate.convertAndSend(
                "/socket-publisher/ride/" + rideId + "/ride-finished",
                completion
        );
    }

    public void notifyPassengerRideStoppedEarly(Long rideId,
                                                Double price,
                                                LocalDateTime startTime,
                                                LocalDateTime endTime,
                                                Long driverId) {
        GetRideStoppedEarlyDTO payload = new GetRideStoppedEarlyDTO(
                rideId,
                "STOPPED_EARLY",
                price,
                startTime,
                endTime,
                java.time.Duration.between(startTime, endTime).toMinutes(),
                "Ride was stopped early at passenger request.",
                LocalDateTime.now(),
                driverId
        );

        messagingTemplate.convertAndSend(
                "/socket-publisher/ride/" + rideId + "/ride-stopped",
                payload
        );
    }

    public void notifyAdminsPanicTriggered(Long rideId,
                                           Long userId,
                                           String userEmail,
                                           LocalDateTime triggeredAt) {
        GetPanicAlertDTO payload = new GetPanicAlertDTO(
                rideId,
                userId,
                userEmail,
                triggeredAt,
                "Panic button pressed for ride " + rideId
        );

        messagingTemplate.convertAndSend(
                "/socket-publisher/admin/panic-alerts",
                payload
        );
    }


    public void broadcastChatMessage(Long chatId, GetMessageDTO messageDto) {
        messagingTemplate.convertAndSend(
                "/socket-publisher/chat/" + chatId,
                messageDto
        );
    }
}