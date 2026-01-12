package rs.getgo.backend.services;

import org.springframework.stereotype.Service;
import rs.getgo.backend.dtos.passenger.GetRidePassengerDTO;
import rs.getgo.backend.dtos.ride.GetRideDTO;
import rs.getgo.backend.model.entities.CompletedRide;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.repositories.CompletedRideRepository;
import rs.getgo.backend.repositories.PassengerRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DriverServiceImpl implements DriverService {
    private final CompletedRideRepository rideRepository;
    private final PassengerRepository passengerRepository;

    public DriverServiceImpl(CompletedRideRepository rideRepository, PassengerRepository passengerRepository) {
        this.rideRepository = rideRepository;
        this.passengerRepository = passengerRepository;
    }

    @Override
    public List<GetRideDTO> getDriverRides(Long driverId, LocalDate startDate) {
        List<CompletedRide> rides = rideRepository.findByDriverId(driverId);

        List<GetRideDTO> dtoList = new ArrayList<>();

        for (CompletedRide r : rides) {

            // filtering by startDate
            if (startDate != null && !r.getScheduledTime().toLocalDate().isEqual(startDate)) {
                continue;
            }


            // mapping passengers
            List<GetRidePassengerDTO> passengerDTOs = new ArrayList<>();
            if (r.getLinkedPassengerIds() != null && !r.getLinkedPassengerIds().isEmpty()) {
                List<Passenger> passengers = passengerRepository.findAllById(r.getLinkedPassengerIds());

                for (Passenger p : passengers) {
                    passengerDTOs.add(new GetRidePassengerDTO(p.getId(), p.getUsername()));
                }
            }

            GetRideDTO dto = new GetRideDTO(
                    r.getId(),
                    r.getDriverId(),
                    passengerDTOs,
                    r.getRoute() != null ? r.getRoute().getStartingPoint() : "Unknown",
                    r.getRoute() != null ? r.getRoute().getEndingPoint() : "Unknown",
                    r.getStartTime(),
                    r.getEndTime(),
                    r.getStartTime() != null && r.getEndTime() != null ?
                            (int) java.time.Duration.between(r.getStartTime(), r.getEndTime()).toMinutes() : 0,
                    r.isCancelled(),
                    false,
                    r.isCompletedNormally() ? "FINISHED" : (r.isCancelled() ? "CANCELLED" : "ACTIVE"),
                    r.getActualPrice()
            );

            dtoList.add(dto);
        }

        return dtoList;
    }

}
