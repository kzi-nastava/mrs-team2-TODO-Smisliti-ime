package rs.getgo.backend.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.getgo.backend.dtos.activeRide.GetActiveRideAdminDTO;
import rs.getgo.backend.dtos.activeRide.GetActiveRideAdminDetailsDTO;
import rs.getgo.backend.services.ActiveRideService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/active-rides")
public class ActiveRideController {
    private final ActiveRideService activeRideService;

    public ActiveRideController(ActiveRideService activeRideService) {
        this.activeRideService = activeRideService;
    }

    // GET /api/admin/active-rides
    @GetMapping
    public List<GetActiveRideAdminDTO> getAllActiveRides() {
        return activeRideService.getAllActiveRidesForAdmin();
    }

    @GetMapping("/{id}")
    public GetActiveRideAdminDetailsDTO getActiveRideDetails(@PathVariable Long id) {
        return activeRideService.getActiveRideDetails(id);
    }


}
