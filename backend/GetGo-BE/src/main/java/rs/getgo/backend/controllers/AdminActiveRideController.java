package rs.getgo.backend.controllers;

import org.springframework.web.bind.annotation.*;
import rs.getgo.backend.dtos.activeRide.GetActiveRideAdminDTO;
import rs.getgo.backend.dtos.activeRide.GetActiveRideAdminDetailsDTO;
import rs.getgo.backend.services.AdminActiveRideService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/active-rides")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminActiveRideController {
    private final AdminActiveRideService activeRideService;

    public AdminActiveRideController(AdminActiveRideService activeRideService) {
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
