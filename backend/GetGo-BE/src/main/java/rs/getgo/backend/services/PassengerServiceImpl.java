package rs.getgo.backend.services;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.getgo.backend.dtos.authentication.UpdatePasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatedPasswordDTO;
import rs.getgo.backend.dtos.passenger.GetPassengerDTO;
import rs.getgo.backend.dtos.passenger.GetRidePassengerDTO;
import rs.getgo.backend.dtos.passenger.UpdatePassengerDTO;
import rs.getgo.backend.dtos.passenger.UpdatedPassengerDTO;
import rs.getgo.backend.dtos.ride.GetReorderRideDTO;
import rs.getgo.backend.dtos.ride.GetRideDTO;
import rs.getgo.backend.dtos.user.UpdatedProfilePictureDTO;
import rs.getgo.backend.model.entities.CompletedRide;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.model.entities.User;
import rs.getgo.backend.repositories.CompletedRideRepository;
import rs.getgo.backend.repositories.PassengerRepository;
import rs.getgo.backend.repositories.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PassengerServiceImpl implements PassengerService {

    private final PassengerRepository passengerRepo;
    private final ModelMapper modelMapper;
    private final FileStorageService fileStorageService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CompletedRideRepository completedRideRepository;
    private final UserRepository userRepository;

    public PassengerServiceImpl(
            PassengerRepository passengerRepo,
            ModelMapper modelMapper,
            FileStorageService fileStorageService,
            BCryptPasswordEncoder passwordEncoder,
            CompletedRideRepository completedRideRepository,
            UserRepository userRepository
    ) {
        this.passengerRepo = passengerRepo;
        this.modelMapper = modelMapper;
        this.fileStorageService = fileStorageService;
        this.passwordEncoder = passwordEncoder;
        this.completedRideRepository = completedRideRepository;
        this.userRepository = userRepository;
    }

    public GetPassengerDTO getPassenger(String email) {
        Passenger passenger = passengerRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Passenger not found with email: " + email));

        return modelMapper.map(passenger, GetPassengerDTO.class);
    }

    public GetPassengerDTO getPassengerById(Long passengerId) {
        Passenger passenger = passengerRepo.findById(passengerId)
                .orElseThrow(() -> new RuntimeException("Passenger not found with id: " + passengerId));

        return modelMapper.map(passenger, GetPassengerDTO.class);
    }

    public UpdatedPassengerDTO updateProfile(String email, UpdatePassengerDTO updatePassengerDTO) {
        Passenger passenger = passengerRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Passenger not found with email: " + email));

        if (updatePassengerDTO.getName() != null && !updatePassengerDTO.getName().trim().isEmpty()) {
            passenger.setName(updatePassengerDTO.getName().trim());
        }
        if (updatePassengerDTO.getSurname() != null && !updatePassengerDTO.getSurname().trim().isEmpty()) {
            passenger.setSurname(updatePassengerDTO.getSurname().trim());
        }
        if (updatePassengerDTO.getPhone() != null && !updatePassengerDTO.getPhone().trim().isEmpty()) {
            passenger.setPhone(updatePassengerDTO.getPhone().trim());
        }
        if (updatePassengerDTO.getAddress() != null && !updatePassengerDTO.getAddress().trim().isEmpty()) {
            passenger.setAddress(updatePassengerDTO.getAddress().trim());
        }

        Passenger savedPassenger = passengerRepo.save(passenger);
        return modelMapper.map(savedPassenger, UpdatedPassengerDTO.class);
    }

    public UpdatedPasswordDTO updatePassword(String email, UpdatePasswordDTO updatePasswordDTO) {
        if (!updatePasswordDTO.getPassword().equals(updatePasswordDTO.getConfirmPassword())) {
            return new UpdatedPasswordDTO(false, "Passwords do not match");
        }

        Passenger passenger = passengerRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Passenger not found with email: " + email));

        if (!passwordEncoder.matches(updatePasswordDTO.getOldPassword(), passenger.getPassword())) {
            return new UpdatedPasswordDTO(false, "Old password is incorrect");
        }

        passenger.setPassword(passwordEncoder.encode(updatePasswordDTO.getPassword()));
        passengerRepo.save(passenger);

        return new UpdatedPasswordDTO(true, "Password updated successfully");
    }

    public UpdatedProfilePictureDTO uploadProfilePicture(String email, MultipartFile file) {
        Passenger passenger = passengerRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Passenger not found with email: " + email));

        // Delete old picture if exists
        if (passenger.getProfilePictureUrl() != null) {
            fileStorageService.deleteFile(passenger.getProfilePictureUrl());
        }

        String fileUrl = fileStorageService.storeFile(file, "passenger_" + passenger.getId());
        passenger.setProfilePictureUrl(fileUrl);
        passengerRepo.save(passenger);

        return new UpdatedProfilePictureDTO(
                fileUrl,
                "Profile picture updated successfully");
    }

    @Override
    public Page<GetRideDTO> getPassengerRides(String email, LocalDate startDate, int page, int size) {
        Passenger passenger = passengerRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Passenger not found with email: " + email));

        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());

        Page<CompletedRide> ridesPage;
        if (startDate != null) {
            // Filter rides ON the specific date (from 00:00 to 23:59:59.999)
            ridesPage = completedRideRepository.findByPassengerIdAndStartTimeBetween(
                    passenger.getId(),
                    startDate.atStartOfDay(),
                    startDate.plusDays(1).atStartOfDay(),
                    pageable);
        } else {
            // Get all rides where passenger is either paying or linked
            ridesPage = completedRideRepository.findByPassengerId(passenger.getId(), pageable);
        }

        return ridesPage.map(this::mapCompletedRideToDTO);
    }

    @Override
    public GetReorderRideDTO getPassengerRideById(String email, Long rideId) {
        Passenger passenger = passengerRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Passenger not found with email: " + email));

        CompletedRide r = completedRideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with id: " + rideId));

        boolean isPaying = r.getPayingPassengerId() != null && r.getPayingPassengerId().equals(passenger.getId());
        boolean isLinked = r.getLinkedPassengerIds() != null && r.getLinkedPassengerIds().contains(passenger.getId());

        if (!isPaying && !isLinked) {
            throw new RuntimeException("Passenger is not allowed to view this ride");
        }

        return mapCompletedReorderedRideToDTO(r);
    }

    private GetRideDTO mapCompletedRideToDTO(CompletedRide r) {
        List<GetRidePassengerDTO> passengerDTOs = new ArrayList<>();

        if (r.getPayingPassengerId() != null) {
            passengerDTOs.add(new GetRidePassengerDTO(
                    r.getPayingPassengerId(),
                    r.getPayingPassengerEmail()
            ));
        }

        if (r.getLinkedPassengerIds() != null && !r.getLinkedPassengerIds().isEmpty()) {
            List<Passenger> passengers = passengerRepo.findAllById(r.getLinkedPassengerIds());
            for (Passenger p : passengers) {
                passengerDTOs.add(new GetRidePassengerDTO(p.getId(), p.getEmail()));
            }
        }

        String cancelledUserEmail = null;
        if (r.getCancelledByUserId() != null) {
            Optional<User> user = userRepository.findById(r.getCancelledByUserId());
            if (user.isPresent()) {
                cancelledUserEmail = user.get().getEmail();
            }
        }

        return new GetRideDTO(
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
                r.isCompletedNormally() ? "FINISHED" : (r.isCancelled() ? "CANCELLED" : (r.isStoppedEarly() ? "STOPPED" : "ACTIVE")),
                r.getEstimatedPrice(),
                r.isPanicPressed(),
                r.getEstDistanceKm(),
                r.getEstTime(),
                cancelledUserEmail,
                r.getCancelReason()
        );
    }

    private GetReorderRideDTO mapCompletedReorderedRideToDTO(CompletedRide r) {
        List<GetRidePassengerDTO> passengerDTOs = new ArrayList<>();

        if (r.getPayingPassengerId() != null) {
            passengerDTOs.add(new GetRidePassengerDTO(
                    r.getPayingPassengerId(),
                    r.getPayingPassengerEmail()
            ));
        }

        if (r.getLinkedPassengerIds() != null && !r.getLinkedPassengerIds().isEmpty()) {
            List<Passenger> passengers = passengerRepo.findAllById(r.getLinkedPassengerIds());
            for (Passenger p : passengers) {
                passengerDTOs.add(new GetRidePassengerDTO(p.getId(), p.getEmail()));
            }
        }

        String cancelledUserEmail = null;
        if (r.getCancelledByUserId() != null) {
            Optional<User> user = userRepository.findById(r.getCancelledByUserId());
            if (user.isPresent()) {
                cancelledUserEmail = user.get().getEmail();
            }
        }
        return new GetReorderRideDTO(
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
                r.isCompletedNormally() ? "FINISHED" : (r.isCancelled() ? "CANCELLED" : (r.isStoppedEarly() ? "STOPPED" : "ACTIVE")),
                r.getEstimatedPrice(),
                r.isPanicPressed(),
                r.getVehicleType(),
                r.isNeedsBabySeats(),
                r.isNeedsPetFriendly(),
                r.getRoute(),
                r.getEstDistanceKm(),
                r.getEstTime(),
                cancelledUserEmail,
                r.getCancelReason()
        );
    }
}
