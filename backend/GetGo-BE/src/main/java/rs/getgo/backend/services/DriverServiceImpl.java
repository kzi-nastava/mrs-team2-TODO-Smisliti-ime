package rs.getgo.backend.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.getgo.backend.dtos.authentication.GetActivationTokenDTO;
import rs.getgo.backend.dtos.authentication.UpdateDriverPasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatePasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatedPasswordDTO;
import rs.getgo.backend.dtos.driver.GetDriverDTO;
import rs.getgo.backend.dtos.driver.UpdateDriverPersonalDTO;
import rs.getgo.backend.dtos.driver.UpdateDriverVehicleDTO;
import rs.getgo.backend.dtos.passenger.GetRidePassengerDTO;
import rs.getgo.backend.dtos.request.CreatedDriverChangeRequestDTO;
import rs.getgo.backend.dtos.ride.GetRideDTO;
import rs.getgo.backend.model.entities.*;
import rs.getgo.backend.model.enums.RequestStatus;
import rs.getgo.backend.repositories.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DriverServiceImpl {

    @Autowired
    private DriverRepository driverRepo;
    @Autowired
    private CompletedRideRepository completedRideRepository;
    @Autowired
    private PassengerRepository passengerRepository;
    @Autowired
    private PersonalChangeRequestRepository personalChangeRequestRepo;
    @Autowired
    private VehicleChangeRequestRepository vehicleChangeRequestRepo;
    @Autowired
    private AvatarChangeRequestRepository avatarChangeRequestRepo;
    @Autowired
    private DriverActivationTokenRepository driverActivationTokenRepo;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public List<GetRideDTO> getDriverRides(Long driverId, LocalDate startDate) {
        List<CompletedRide> rides = completedRideRepository.findByDriverId(driverId);

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
                    passengerDTOs.add(new GetRidePassengerDTO(p.getId(), p.getEmail()));
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
                    r.getActualPrice(),
                    r.isPanicPressed()
            );

            dtoList.add(dto);
        }

        return dtoList;
    }

    public GetActivationTokenDTO validateActivationToken(String token) {
        Optional<DriverActivationToken> tokenOptional = driverActivationTokenRepo.findByToken(token);

        if (tokenOptional.isEmpty()) {
            return new GetActivationTokenDTO(false, null, "Invalid activation token");
        }

        DriverActivationToken activationToken = tokenOptional.get();

        // Check if already used
        if (activationToken.isUsed()) {
            return new GetActivationTokenDTO(false, null, "Activation token has already been used");
        }

        // Check if expired
        if (activationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return new GetActivationTokenDTO(false, null, "Activation token has expired");
        }

        // Token is valid
        Driver driver = activationToken.getDriver();
        return new GetActivationTokenDTO(true, driver.getEmail(), null);
    }

    public UpdatedPasswordDTO setDriverPassword(UpdateDriverPasswordDTO passwordDTO) {
        if (!passwordDTO.getPassword().equals(passwordDTO.getConfirmPassword())) {
            return new UpdatedPasswordDTO(false, "Passwords do not match");
        }

        Optional<DriverActivationToken> tokenOpt =
                driverActivationTokenRepo.findByToken(passwordDTO.getToken());
        if (tokenOpt.isEmpty()) {
            return new UpdatedPasswordDTO(false, "Invalid activation token");
        }
        DriverActivationToken activationToken = tokenOpt.get();
        if (activationToken.isUsed()) {
            return new UpdatedPasswordDTO(false, "Activation token has already been used");
        }
        if (activationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return new UpdatedPasswordDTO(false, "Activation token expired");
        }

        // Set password and activate driver
        Driver driver = activationToken.getDriver();
        driver.setPassword(passwordEncoder.encode(passwordDTO.getPassword()));
        driver.setActivated(true);
        driverRepo.save(driver);

        // Mark token as used
        activationToken.setUsed(true);
        activationToken.setUsedAt(LocalDateTime.now());
        driverActivationTokenRepo.save(activationToken);

        return new UpdatedPasswordDTO(true, "Password set successfully. You can now log in.");
    }

    public GetDriverDTO getDriverById(Long driverId) {
        Driver driver = driverRepo.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + driverId));

        GetDriverDTO dto = modelMapper.map(driver, GetDriverDTO.class);

        // Map vehicle info
        if (driver.getVehicle() != null) {
            dto.setVehicleModel(driver.getVehicle().getModel());
            dto.setVehicleType(driver.getVehicle().getType().toString());
            dto.setVehicleLicensePlate(driver.getVehicle().getLicensePlate());
            dto.setVehicleSeats(driver.getVehicle().getNumberOfSeats());
            dto.setVehicleHasBabySeats(driver.getVehicle().getIsBabyFriendly());
            dto.setVehicleAllowsPets(driver.getVehicle().getIsPetFriendly());
        }

        // Get full URL so <img src=""> can load it
        dto.setProfilePictureUrl(fileStorageService.getFileUrl(driver.getProfilePictureUrl()));

        // TODO: Calculate recent hours worked in last 24h
        dto.setRecentHoursWorked(0);

        return dto;
    }

    public UpdatedPasswordDTO updatePassword(Long driverId, UpdatePasswordDTO updatePasswordDTO) {
        if (!updatePasswordDTO.getPassword().equals(updatePasswordDTO.getConfirmPassword())) {
            return new UpdatedPasswordDTO(false, "Passwords do not match");
        }

        Driver driver = driverRepo.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + driverId));

        if (!passwordEncoder.matches(updatePasswordDTO.getOldPassword(), driver.getPassword())) {
            return new UpdatedPasswordDTO(false, "Old password is incorrect");
        }

        driver.setPassword(updatePasswordDTO.getPassword());
        driverRepo.save(driver);

        return new UpdatedPasswordDTO(true, "Password updated successfully");
    }

    public CreatedDriverChangeRequestDTO requestPersonalInfoChange(Long driverId, UpdateDriverPersonalDTO updateDTO) {
        Driver driver = driverRepo.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + driverId));

        PersonalChangeRequest changeRequest = new PersonalChangeRequest();
        changeRequest.setDriver(driver);
        changeRequest.setRequestedName(updateDTO.getName());
        changeRequest.setRequestedSurname(updateDTO.getSurname());
        changeRequest.setRequestedPhone(updateDTO.getPhone());
        changeRequest.setRequestedAddress(updateDTO.getAddress());
        changeRequest.setStatus(RequestStatus.PENDING);
        changeRequest.setCreatedAt(LocalDateTime.now());

        PersonalChangeRequest savedRequest = personalChangeRequestRepo.save(changeRequest);

        return new CreatedDriverChangeRequestDTO(
                savedRequest.getId(),
                driverId,
                "PENDING",
                "Personal info change request created successfully"
        );
    }

    public CreatedDriverChangeRequestDTO requestVehicleInfoChange(Long driverId, UpdateDriverVehicleDTO updateDTO) {
        Driver driver = driverRepo.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + driverId));

        VehicleChangeRequest changeRequest = new VehicleChangeRequest();
        changeRequest.setDriver(driver);
        changeRequest.setRequestedVehicleModel(updateDTO.getVehicleModel());
        changeRequest.setRequestedVehicleType(updateDTO.getVehicleType());
        changeRequest.setRequestedVehicleLicensePlate(updateDTO.getVehicleLicensePlate());
        changeRequest.setRequestedVehicleSeats(updateDTO.getVehicleSeats());
        changeRequest.setRequestedVehicleHasBabySeats(updateDTO.getVehicleHasBabySeats());
        changeRequest.setRequestedVehicleAllowsPets(updateDTO.getVehicleAllowsPets());
        changeRequest.setStatus(RequestStatus.PENDING);
        changeRequest.setCreatedAt(LocalDateTime.now());

        VehicleChangeRequest savedRequest = vehicleChangeRequestRepo.save(changeRequest);

        return new CreatedDriverChangeRequestDTO(
                savedRequest.getId(),
                driverId,
                "PENDING",
                "Vehicle info change request created successfully"
        );
    }

    public CreatedDriverChangeRequestDTO requestProfilePictureChange(Long driverId, MultipartFile file) {
        Driver driver = driverRepo.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + driverId));

        // Store file temporarily until approval/rejection
        String filename = fileStorageService.storeFile(file, "driver_pending_" + driverId);

        // Create change request
        AvatarChangeRequest changeRequest = new AvatarChangeRequest();
        changeRequest.setDriver(driver);
        changeRequest.setRequestedProfilePictureUrl(filename);
        changeRequest.setStatus(RequestStatus.PENDING);
        changeRequest.setCreatedAt(LocalDateTime.now());

        AvatarChangeRequest savedRequest = avatarChangeRequestRepo.save(changeRequest);

        return new CreatedDriverChangeRequestDTO(
                savedRequest.getId(),
                driverId,
                "PENDING",
                "Profile picture change request created successfully"
        );
    }
}