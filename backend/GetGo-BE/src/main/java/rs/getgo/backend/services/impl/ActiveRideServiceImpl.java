package rs.getgo.backend.services.impl;

import org.springframework.stereotype.Service;
import rs.getgo.backend.dtos.activeRide.GetActiveRideAdminDTO;
import rs.getgo.backend.dtos.activeRide.GetActiveRideAdminDetailsDTO;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.repositories.ActiveRideRepository;
import rs.getgo.backend.services.ActiveRideService;

import java.util.List;

@Service
public class ActiveRideServiceImpl implements ActiveRideService {

    private final ActiveRideRepository activeRideRepository;

    public ActiveRideServiceImpl(ActiveRideRepository activeRideRepository) {
        this.activeRideRepository = activeRideRepository;
    }

    @Override
    public List<GetActiveRideAdminDTO> getAllActiveRidesForAdmin() {
        return activeRideRepository.findAll().stream()
                .map(this::toAdminDTO)
                .toList();
    }

    @Override
    public GetActiveRideAdminDTO toAdminDTO(ActiveRide ride) {
        return new GetActiveRideAdminDTO(
                ride.getId(),
                ride.getDriver().getId(),
                ride.getDriver().getName(),
                ride.getDriver().getEmail(),
                ride.getRoute().getStartingPoint(),
                ride.getRoute().getEndingPoint(),
                ride.getScheduledTime(),
                ride.getActualStartTime(),
                ride.getStatus(),
                ride.getVehicleType(),
                ride.getEstimatedPrice(),
                ride.getEstimatedDurationMin()
        );
    }

    @Override
    public GetActiveRideAdminDetailsDTO getActiveRideDetails(Long id) {
        ActiveRide ride = activeRideRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Active ride not found"));

        return new GetActiveRideAdminDetailsDTO(
                ride.getId(),
                ride.getDriver().getId(),
                ride.getDriver().getName(),
                ride.getDriver().getEmail(),
                ride.getActualStartTime(),
                ride.getScheduledTime(),
                ride.getStatus(),
                ride.getVehicleType(),
                ride.isNeedsBabySeats(),
                ride.isNeedsPetFriendly(),
                ride.getEstimatedPrice(),
                ride.getEstimatedDurationMin(),
                ride.getPayingPassenger().getEmail(),
                ride.getLinkedPassengers().stream().map(Passenger::getEmail).toList(),
                ride.getCurrentLocation().getAddress()
        );

    }


}
