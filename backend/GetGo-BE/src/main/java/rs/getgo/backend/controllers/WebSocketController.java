package rs.getgo.backend.controllers;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import rs.getgo.backend.dtos.driver.GetDriverLocationDTO;
import rs.getgo.backend.dtos.message.GetMessageDTO;
import rs.getgo.backend.dtos.ride.GetDriverActiveRideDTO;
import rs.getgo.backend.model.enums.RideStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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
        Map<String, Object> update = new HashMap<>();
        update.put("rideId", rideId);
        update.put("status", status);
        update.put("timestamp", LocalDateTime.now());

        messagingTemplate.convertAndSend(
                "/socket-publisher/driver/" + driverEmail + "/status-update",
                update
        );
    }

    public void notifyDriverRideFinished(String driverEmail, Long rideId, Double price,
                                         LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> completion = new HashMap<>();
        completion.put("rideId", rideId);
        completion.put("status", RideStatus.FINISHED.toString());
        completion.put("price", price);
        completion.put("startTime", startTime);
        completion.put("endTime", endTime);
        completion.put("durationMinutes",
                java.time.Duration.between(startTime, endTime).toMinutes());

        messagingTemplate.convertAndSend(
                "/socket-publisher/driver/" + driverEmail + "/ride-finished",
                completion
        );
    }

    public void notifyPassengerRideStatusUpdate(Long rideId, String status, String message) {
        Map<String, Object> update = new HashMap<>();
        update.put("rideId", rideId);
        update.put("status", status);
        update.put("message", message);
        update.put("timestamp", LocalDateTime.now());

        messagingTemplate.convertAndSend(
                "/socket-publisher/ride/" + rideId + "/status-update",
                update
        );
    }

    public void notifyPassengerRideFinished(Long rideId, Double price, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> completion = new HashMap<>();
        completion.put("rideId", rideId);
        completion.put("status", RideStatus.FINISHED.toString());
        completion.put("price", price);
        completion.put("startTime", startTime);
        completion.put("endTime", endTime);
        completion.put("durationMinutes",
                java.time.Duration.between(startTime, endTime).toMinutes());
        completion.put("message", "Ride completed! Total: " + price + " RSD");
        completion.put("timestamp", LocalDateTime.now());

        messagingTemplate.convertAndSend(
                "/socket-publisher/ride/" + rideId + "/ride-finished",
                completion
        );
    }

    // === notify passengers when ride is stopped early ===
    public void notifyPassengerRideStoppedEarly(Long rideId,
                                                Double price,
                                                LocalDateTime startTime,
                                                LocalDateTime endTime) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("rideId", rideId);
        payload.put("status", "STOPPED_EARLY");
        payload.put("price", price);
        payload.put("startTime", startTime);
        payload.put("endTime", endTime);
        payload.put("durationMinutes",
                java.time.Duration.between(startTime, endTime).toMinutes());
        payload.put("message", "Ride was stopped early at passenger request.");
        payload.put("timestamp", LocalDateTime.now());

        messagingTemplate.convertAndSend(
                "/socket-publisher/ride/" + rideId + "/ride-stopped",
                payload
        );
    }

    // === notify admins when PANIC is triggered ===
    public void notifyAdminsPanicTriggered(Long rideId,
                                           Long userId,
                                           String userEmail,
                                           LocalDateTime triggeredAt) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("rideId", rideId);
        payload.put("userId", userId);
        payload.put("userEmail", userEmail);
        payload.put("triggeredAt", triggeredAt);
        payload.put("message", "Panic button pressed for ride " + rideId);

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