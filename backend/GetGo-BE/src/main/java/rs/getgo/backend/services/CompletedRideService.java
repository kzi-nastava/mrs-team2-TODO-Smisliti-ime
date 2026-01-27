package rs.getgo.backend.services;

import rs.getgo.backend.dtos.inconsistencyReport.GetInconsistencyReportDTO;
import rs.getgo.backend.model.entities.CompletedRide;

import java.util.List;

public interface CompletedRideService {
    public Long getDriverIdByRideId(Long rideId);
    List<GetInconsistencyReportDTO> getInconsistencyReportsByRideId(Long rideId);
}
