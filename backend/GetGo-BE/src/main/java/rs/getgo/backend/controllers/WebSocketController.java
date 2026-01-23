package rs.getgo.backend.controllers;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import rs.getgo.backend.dtos.driver.GetDriverLocationDTO;
import rs.getgo.backend.dtos.ride.GetDriverActiveRideDTO;

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
}