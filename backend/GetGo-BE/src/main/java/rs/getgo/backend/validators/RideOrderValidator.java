package rs.getgo.backend.validators;

import org.springframework.stereotype.Component;
import rs.getgo.backend.dtos.ride.CreateRideRequestDTO;
import rs.getgo.backend.dtos.ride.CreatedRideResponseDTO;
import rs.getgo.backend.model.entities.BlockNote;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.model.enums.RideOrderStatus;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.repositories.ActiveRideRepository;
import rs.getgo.backend.repositories.BlockNoteRepository;
import rs.getgo.backend.repositories.PassengerRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class RideOrderValidator {

    public record ValidationResult(
            CreatedRideResponseDTO error,
            Passenger payingPassenger,
            List<Passenger> linkedPassengers,
            LocalDateTime scheduledTime
    ) {
        public static ValidationResult error(CreatedRideResponseDTO error) {
            return new ValidationResult(error, null, null, null);
        }

        public static ValidationResult success(
                Passenger payingPassenger,
                List<Passenger> linkedPassengers,
                LocalDateTime scheduledTime
        ) {
            return new ValidationResult(null, payingPassenger, linkedPassengers, scheduledTime);
        }

        public boolean isValid() {
            return error == null;
        }
    }

    private final PassengerRepository passengerRepository;
    private final BlockNoteRepository blockNoteRepository;
    private final ActiveRideRepository activeRideRepository;

    public RideOrderValidator(
            PassengerRepository passengerRepository,
            BlockNoteRepository blockNoteRepository,
            ActiveRideRepository activeRideRepository
    ) {
        this.passengerRepository = passengerRepository;
        this.blockNoteRepository = blockNoteRepository;
        this.activeRideRepository = activeRideRepository;
    }

    public ValidationResult validateRideOrder(
            CreateRideRequestDTO request,
            String userEmail
    ) {
        CreatedRideResponseDTO coordinatesError = validateCoordinates(request);
        if (coordinatesError != null) return ValidationResult.error(coordinatesError);

        Passenger payingPassenger = passengerRepository.findByEmail(userEmail).orElse(null);
        CreatedRideResponseDTO passengerError = validatePayingPassenger(payingPassenger);
        if (passengerError != null) return ValidationResult.error(passengerError);

        LocalDateTime scheduledTime = null;
        if (request.getScheduledTime() != null && !request.getScheduledTime().isEmpty()) {
            scheduledTime = parseScheduledTime(request.getScheduledTime());
            CreatedRideResponseDTO timeError = validateScheduledTime(scheduledTime);
            if (timeError != null) return ValidationResult.error(timeError);
        }

        List<Passenger> linkedPassengers = collectLinkedPassengers(request.getFriendEmails());
        CreatedRideResponseDTO linkedError = validateLinkedPassengers(
                request.getFriendEmails(), linkedPassengers
        );
        if (linkedError != null) return ValidationResult.error(linkedError);

        return ValidationResult.success(payingPassenger, linkedPassengers, scheduledTime);
    }

    public CreatedRideResponseDTO validateCoordinates(CreateRideRequestDTO request) {
        if (request.getLatitudes().size() < 2 ||
                request.getLatitudes().size() != request.getLongitudes().size() ||
                request.getLatitudes().size() != request.getAddresses().size()) {
            return new CreatedRideResponseDTO(
                    "INVALID_REQUEST",
                    "Invalid coordinates or addresses",
                    null
            );
        }
        return null;
    }

    public CreatedRideResponseDTO validatePayingPassenger(Passenger payingPassenger) {
        if (payingPassenger == null) {
            return new CreatedRideResponseDTO(
                    RideOrderStatus.PASSENGER_NOT_FOUND.toString(),
                    "Passenger account not found",
                    null
            );
        }

        if (payingPassenger.isBlocked()) {
            String reason = blockNoteRepository.findByUserAndUnblockedAtIsNull(payingPassenger)
                    .map(BlockNote::getReason)
                    .orElse("You have been blocked.");
            return new CreatedRideResponseDTO(
                    "blocked",
                    "Cannot order ride: user is blocked. Reason: " + reason,
                    null
            );
        }

        // Check for any active ride that's not far-future scheduled
        boolean hasBlockingRide = activeRideRepository.existsByPayingPassengerAndStatusNot(
                payingPassenger, RideStatus.SCHEDULED
        ) || activeRideRepository.existsByLinkedPassengersContainingAndStatusNot(
                payingPassenger, RideStatus.SCHEDULED
        );

        // Cannot start ride if scheduled ride starts in less than 1h
        LocalDateTime soonThreshold = LocalDateTime.now().plusHours(1);
        boolean hasUpcomingScheduled = activeRideRepository.existsByPayingPassengerAndStatusAndScheduledTimeBefore(
                payingPassenger, RideStatus.SCHEDULED, soonThreshold
        ) || activeRideRepository.existsByLinkedPassengersContainingAndStatusAndScheduledTimeBefore(
                payingPassenger, RideStatus.SCHEDULED, soonThreshold
        );

        if (hasBlockingRide || hasUpcomingScheduled) {
            return new CreatedRideResponseDTO(
                    "PASSENGER_HAS_ACTIVE_RIDE",
                    "You already have an active or upcoming ride",
                    null
            );
        }

        return null;
    }

    public CreatedRideResponseDTO validateScheduledTime(LocalDateTime scheduledTime) {
        if (scheduledTime == null ||
                scheduledTime.isBefore(LocalDateTime.now()) ||
                scheduledTime.isAfter(LocalDateTime.now().plusHours(5))) {
            return new CreatedRideResponseDTO(
                    RideOrderStatus.INVALID_SCHEDULED_TIME.toString(),
                    "Scheduled time must be within the next 5 hours",
                    null
            );
        }
        return null;
    }

    public CreatedRideResponseDTO validateLinkedPassengers(
            List<String> friendEmails,
            List<Passenger> linkedPassengers
    ) {
        if (friendEmails == null) return null;

        LocalDateTime soonThreshold = LocalDateTime.now().plusHours(1);

        for (Passenger passenger : linkedPassengers) {
            boolean hasBlockingRide = activeRideRepository.existsByPayingPassengerAndStatusNot(
                    passenger, RideStatus.SCHEDULED
            ) || activeRideRepository.existsByLinkedPassengersContainingAndStatusNot(
                    passenger, RideStatus.SCHEDULED
            );

            boolean hasUpcomingScheduled = activeRideRepository.existsByPayingPassengerAndStatusAndScheduledTimeBefore(
                    passenger, RideStatus.SCHEDULED, soonThreshold
            ) || activeRideRepository.existsByLinkedPassengersContainingAndStatusAndScheduledTimeBefore(
                    passenger, RideStatus.SCHEDULED, soonThreshold
            );

            if (hasBlockingRide || hasUpcomingScheduled) {
                return new CreatedRideResponseDTO(
                        "LINKED_PASSENGER_HAS_ACTIVE_RIDE",
                        "Passenger " + passenger.getEmail() + " already has an active or upcoming ride",
                        null
                );
            }
        }

        return null;
    }

    public List<Passenger> collectLinkedPassengers(List<String> friendEmails) {
        List<Passenger> linkedPassengers = new ArrayList<>();
        if (friendEmails == null) return linkedPassengers;

        for (String email : friendEmails) {
            passengerRepository.findByEmail(email).ifPresent(linkedPassengers::add);
        }

        return linkedPassengers;
    }

    public LocalDateTime parseScheduledTime(String timeString) {
        try {
            LocalTime time = LocalTime.parse(timeString);
            return LocalDateTime.of(LocalDateTime.now().toLocalDate(), time);
        } catch (Exception e) {
            return null;
        }
    }
}