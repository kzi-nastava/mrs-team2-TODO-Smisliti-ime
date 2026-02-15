package rs.getgo.backend.S1.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.getgo.backend.dtos.ride.CreateRideRequestDTO;
import rs.getgo.backend.dtos.ride.CreatedRideResponseDTO;
import rs.getgo.backend.model.entities.BlockNote;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.repositories.ActiveRideRepository;
import rs.getgo.backend.repositories.BlockNoteRepository;
import rs.getgo.backend.validators.RideOrderValidator;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RideOrderValidatorTest {

    @Mock private BlockNoteRepository blockNoteRepository;
    @Mock private ActiveRideRepository activeRideRepository;

    @InjectMocks
    private RideOrderValidator validator;

    @Test
    void should_fail_when_coordinatesSizesDiffer() {
        CreateRideRequestDTO dto = new CreateRideRequestDTO(
                List.of(20.0, 30.0),
                List.of(20.0, 30.0, 40.0),
                List.of("adr1", "adr2"),
                null,
                List.of(),
                false,
                false,
                null
        );

        CreatedRideResponseDTO result = validator.validateCoordinates(dto);

        assertNotNull(result);
        assertEquals("INVALID_REQUEST", result.getStatus());
        assertEquals("Invalid coordinates or addresses", result.getMessage());
    }

    @Test
    void should_fail_when_coordinatesAndAddressSizesDiffer() {
        CreateRideRequestDTO dto = new CreateRideRequestDTO(
                List.of(20.0, 30.0),
                List.of(20.0, 30.0),
                List.of("adr1", "adr2", "adr3"),
                null,
                List.of(),
                false,
                false,
                null
        );

        CreatedRideResponseDTO result = validator.validateCoordinates(dto);

        assertNotNull(result);
        assertEquals("INVALID_REQUEST", result.getStatus());
        assertEquals("Invalid coordinates or addresses", result.getMessage());
    }

    @Test
    void should_pass_when_coordinatesAreValid() {
        CreateRideRequestDTO dto = new CreateRideRequestDTO(
                List.of(20.0, 25.0, 30.0, 35.0),
                List.of(20.0, 25.0, 30.0, 35.0),
                List.of("start", "stop1", "stop2", "end"),
                null,
                List.of(),
                false,
                false,
                null
        );

        CreatedRideResponseDTO result = validator.validateCoordinates(dto);

        assertNull(result);
    }

    @Test
    void should_fail_when_passengerNotFound() {
        CreatedRideResponseDTO result = validator.validatePayingPassenger(null);

        assertNotNull(result);
        assertEquals("PASSENGER_NOT_FOUND", result.getStatus());
        assertEquals("Passenger account not found", result.getMessage());
    }

    @Test
    void should_fail_when_passengerBlocked() {
        Passenger passenger = new Passenger();
        passenger.setId(1L);
        passenger.setBlocked(true);

        BlockNote blockNote = new BlockNote();
        blockNote.setReason("Fraudulent activity");

        when(blockNoteRepository.findByUserAndUnblockedAtIsNull(passenger))
                .thenReturn(Optional.of(blockNote));

        CreatedRideResponseDTO result = validator.validatePayingPassenger(passenger);

        assertNotNull(result);
        assertEquals("blocked", result.getStatus());
        assertEquals("Cannot order ride: user is blocked. Reason: Fraudulent activity", result.getMessage());
    }

    @Test
    void should_fail_when_passengerHasActiveRide() {
        Passenger passenger = new Passenger();
        passenger.setId(1L);
        passenger.setBlocked(false);

        when(activeRideRepository.existsByPayingPassengerAndStatusNot(passenger, RideStatus.SCHEDULED))
                .thenReturn(true);

        CreatedRideResponseDTO result = validator.validatePayingPassenger(passenger);

        assertNotNull(result);
        assertEquals("PASSENGER_HAS_ACTIVE_RIDE", result.getStatus());
        assertEquals("You already have an active or upcoming ride", result.getMessage());
    }

    @Test
    void should_fail_when_passengerIsLinkedInActiveRide() {
        Passenger passenger = new Passenger();
        passenger.setId(1L);
        passenger.setBlocked(false);

        when(activeRideRepository.existsByPayingPassengerAndStatusNot(passenger, RideStatus.SCHEDULED))
                .thenReturn(false);
        when(activeRideRepository.existsByLinkedPassengersContainingAndStatusNot(passenger, RideStatus.SCHEDULED))
                .thenReturn(true);

        CreatedRideResponseDTO result = validator.validatePayingPassenger(passenger);

        assertNotNull(result);
        assertEquals("PASSENGER_HAS_ACTIVE_RIDE", result.getStatus());
        assertEquals("You already have an active or upcoming ride", result.getMessage());
    }

    @Test
    void should_fail_when_passengerHasUpcomingScheduledRide() {
        Passenger passenger = new Passenger();
        passenger.setId(1L);
        passenger.setBlocked(false);

        when(activeRideRepository.existsByPayingPassengerAndStatusNot(passenger, RideStatus.SCHEDULED))
                .thenReturn(false);
        when(activeRideRepository.existsByLinkedPassengersContainingAndStatusNot(passenger, RideStatus.SCHEDULED))
                .thenReturn(false);
        when(activeRideRepository.existsByPayingPassengerAndStatusAndScheduledTimeBefore(
                eq(passenger), eq(RideStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(true);

        CreatedRideResponseDTO result = validator.validatePayingPassenger(passenger);

        assertNotNull(result);
        assertEquals("PASSENGER_HAS_ACTIVE_RIDE", result.getStatus());
        assertEquals("You already have an active or upcoming ride", result.getMessage());
    }


    @Test
    void should_fail_when_passengerIsLinkedInUpcomingScheduledRide() {
        Passenger passenger = new Passenger();
        passenger.setId(1L);
        passenger.setBlocked(false);

        when(activeRideRepository.existsByPayingPassengerAndStatusNot(passenger, RideStatus.SCHEDULED))
                .thenReturn(false);
        when(activeRideRepository.existsByLinkedPassengersContainingAndStatusNot(passenger, RideStatus.SCHEDULED))
                .thenReturn(false);
        when(activeRideRepository.existsByPayingPassengerAndStatusAndScheduledTimeBefore(
                        eq(passenger), eq(RideStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(false);
        when(activeRideRepository.existsByLinkedPassengersContainingAndStatusAndScheduledTimeBefore(
                        eq(passenger), eq(RideStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(true);

        CreatedRideResponseDTO result = validator.validatePayingPassenger(passenger);

        assertNotNull(result);
        assertEquals("PASSENGER_HAS_ACTIVE_RIDE", result.getStatus());
        assertEquals("You already have an active or upcoming ride", result.getMessage());
    }

    @Test
    void should_pass_when_passengerIsValid() {
        Passenger passenger = new Passenger();
        passenger.setId(1L);
        passenger.setBlocked(false);

        when(activeRideRepository.existsByPayingPassengerAndStatusNot(passenger, RideStatus.SCHEDULED))
                .thenReturn(false);
        when(activeRideRepository.existsByLinkedPassengersContainingAndStatusNot(passenger, RideStatus.SCHEDULED))
                .thenReturn(false);
        when(activeRideRepository.existsByPayingPassengerAndStatusAndScheduledTimeBefore(
                eq(passenger), eq(RideStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(false);
        when(activeRideRepository.existsByLinkedPassengersContainingAndStatusAndScheduledTimeBefore(
                eq(passenger), eq(RideStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(false);

        CreatedRideResponseDTO result = validator.validatePayingPassenger(passenger);

        assertNull(result);
    }

    @Test
    void should_pass_when_scheduledTimeIsValid() {
        LocalDateTime validTime = LocalDateTime.now().plusHours(2);

        CreatedRideResponseDTO result = validator.validateScheduledTime(validTime);

        assertNull(result);
    }

    @Test
    void should_fail_when_scheduledTimeIsInPast() {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);

        CreatedRideResponseDTO result = validator.validateScheduledTime(pastTime);

        assertNotNull(result);
        assertEquals("INVALID_SCHEDULED_TIME", result.getStatus());
        assertEquals("Scheduled time must be within the next 5 hours", result.getMessage());
    }

    @Test
    void should_fail_when_scheduledTimeIsMoreThan5HoursAway() {
        LocalDateTime farFuture = LocalDateTime.now().plusHours(6);

        CreatedRideResponseDTO result = validator.validateScheduledTime(farFuture);

        assertNotNull(result);
        assertEquals("INVALID_SCHEDULED_TIME", result.getStatus());
        assertEquals("Scheduled time must be within the next 5 hours", result.getMessage());
    }

    @Test
    void should_pass_when_friendEmailsIsNull() {
        CreatedRideResponseDTO result = validator.validateLinkedPassengers(null, List.of());

        assertNull(result);
    }

    @Test
    void should_pass_when_linkedPassengersAreAvailable() {
        Passenger linked1 = new Passenger();
        linked1.setId(2L);
        linked1.setEmail("friend1@gmail.com");

        Passenger linked2 = new Passenger();
        linked2.setId(3L);
        linked2.setEmail("friend2@gmail.com");

        when(activeRideRepository.existsByPayingPassengerAndStatusNot(any(Passenger.class), any(RideStatus.class)))
                .thenReturn(false);
        when(activeRideRepository.existsByLinkedPassengersContainingAndStatusNot(any(Passenger.class), any(RideStatus.class)))
                .thenReturn(false);
        when(activeRideRepository.existsByPayingPassengerAndStatusAndScheduledTimeBefore(
                any(Passenger.class), any(RideStatus.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(activeRideRepository.existsByLinkedPassengersContainingAndStatusAndScheduledTimeBefore(
                any(Passenger.class), any(RideStatus.class), any(LocalDateTime.class)))
                .thenReturn(false);

        CreatedRideResponseDTO result = validator.validateLinkedPassengers(
                List.of("friend1@gmail.com", "friend2@gmail.com"),
                List.of(linked1, linked2)
        );

        assertNull(result);
    }

    @Test
    void should_fail_when_linkedPassengerHasActiveRide() {
        Passenger linked = new Passenger();
        linked.setId(2L);
        linked.setEmail("friend@gmail.com");

        when(activeRideRepository.existsByPayingPassengerAndStatusNot(linked, RideStatus.SCHEDULED))
                .thenReturn(true);

        CreatedRideResponseDTO result = validator.validateLinkedPassengers(
                List.of("friend@gmail.com"),
                List.of(linked)
        );

        assertNotNull(result);
        assertEquals("LINKED_PASSENGER_HAS_ACTIVE_RIDE", result.getStatus());
        assertEquals("Passenger friend@gmail.com already has an active or upcoming ride", result.getMessage());
    }

    @Test
    void should_fail_when_linkedPassengerIsInActiveRide() {
        Passenger linked = new Passenger();
        linked.setId(2L);
        linked.setEmail("friend@gmail.com");

        when(activeRideRepository.existsByPayingPassengerAndStatusNot(linked, RideStatus.SCHEDULED))
                .thenReturn(false);
        when(activeRideRepository.existsByLinkedPassengersContainingAndStatusNot(linked, RideStatus.SCHEDULED))
                .thenReturn(true);

        CreatedRideResponseDTO result = validator.validateLinkedPassengers(
                List.of("friend@gmail.com"),
                List.of(linked)
        );

        assertNotNull(result);
        assertEquals("LINKED_PASSENGER_HAS_ACTIVE_RIDE", result.getStatus());
        assertEquals("Passenger friend@gmail.com already has an active or upcoming ride", result.getMessage());
    }

    @Test
    void should_fail_when_linkedPassengerHasUpcomingScheduledRide() {
        Passenger linked = new Passenger();
        linked.setId(2L);
        linked.setEmail("friend@gmail.com");

        when(activeRideRepository.existsByPayingPassengerAndStatusNot(linked, RideStatus.SCHEDULED))
                .thenReturn(false);
        when(activeRideRepository.existsByLinkedPassengersContainingAndStatusNot(linked, RideStatus.SCHEDULED))
                .thenReturn(false);
        when(activeRideRepository.existsByPayingPassengerAndStatusAndScheduledTimeBefore(
                any(Passenger.class), any(RideStatus.class), any(LocalDateTime.class)))
                .thenReturn(true);

        CreatedRideResponseDTO result = validator.validateLinkedPassengers(
                List.of("friend@gmail.com"),
                List.of(linked)
        );

        assertNotNull(result);
        assertEquals("LINKED_PASSENGER_HAS_ACTIVE_RIDE", result.getStatus());
        assertEquals("Passenger friend@gmail.com already has an active or upcoming ride", result.getMessage());
    }



}