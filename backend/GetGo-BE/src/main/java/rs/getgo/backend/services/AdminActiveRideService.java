package rs.getgo.backend.services;

import rs.getgo.backend.dtos.activeRide.GetActiveRideAdminDTO;
import rs.getgo.backend.dtos.activeRide.GetActiveRideAdminDetailsDTO;
import rs.getgo.backend.model.entities.ActiveRide;

import java.util.List;

public interface AdminActiveRideService {
    GetActiveRideAdminDTO toAdminDTO(ActiveRide ride);
    List<GetActiveRideAdminDTO> getAllActiveRidesForAdmin();
    GetActiveRideAdminDetailsDTO getActiveRideDetails(Long id);
}
