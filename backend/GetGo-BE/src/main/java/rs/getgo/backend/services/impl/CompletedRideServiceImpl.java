package rs.getgo.backend.services.impl;

import org.springframework.stereotype.Service;
import rs.getgo.backend.dtos.inconsistencyReport.GetInconsistencyReportDTO;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.CompletedRide;
import rs.getgo.backend.repositories.ActiveRideRepository;
import rs.getgo.backend.repositories.CompletedRideRepository;
import rs.getgo.backend.repositories.InconsistencyReportRepository;
import rs.getgo.backend.services.CompletedRideService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompletedRideServiceImpl implements CompletedRideService {
    private final CompletedRideRepository completedRideRepository;
    private final InconsistencyReportRepository inconsistencyReportRepository;
    private final ActiveRideRepository activeRideRepository;

    public CompletedRideServiceImpl(CompletedRideRepository completedRideRepository, InconsistencyReportRepository inconsistencyReportRepository, ActiveRideRepository activeRideRepository) {
        this.completedRideRepository = completedRideRepository;
        this.inconsistencyReportRepository = inconsistencyReportRepository;
        this.activeRideRepository = activeRideRepository;
    }

    public Long getDriverIdByRideId(Long rideId) {
        CompletedRide ride = completedRideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));
        return ride.getDriverId();
    }

    @Override
    public List<GetInconsistencyReportDTO> getInconsistencyReportsByRideId(Long rideId) {
        return inconsistencyReportRepository
                .findByCompletedRide_IdOrderByCreatedAtDesc(rideId)
                .stream()
                .map(r -> new GetInconsistencyReportDTO(
                        r.getId(),
                        r.getCreatedAt().toString(),
                        r.getPassenger().getEmail(),
                        r.getText()
                ))
                .toList();
    }

}
