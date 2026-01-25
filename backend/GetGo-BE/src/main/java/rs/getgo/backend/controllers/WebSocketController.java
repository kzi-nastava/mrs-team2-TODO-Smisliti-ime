package rs.getgo.backend.controllers;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import rs.getgo.backend.dtos.driver.GetDriverLocationDTO;
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

}