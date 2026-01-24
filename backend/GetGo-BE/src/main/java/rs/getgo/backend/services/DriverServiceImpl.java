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
import rs.getgo.backend.dtos.driver.UpdateDriverLocationDTO;
import rs.getgo.backend.dtos.driver.UpdateDriverPersonalDTO;
import rs.getgo.backend.dtos.driver.UpdateDriverVehicleDTO;
import rs.getgo.backend.dtos.passenger.GetRidePassengerDTO;
import rs.getgo.backend.dtos.request.CreatedDriverChangeRequestDTO;
import rs.getgo.backend.dtos.ride.GetRideDTO;
import rs.getgo.backend.model.entities.*;
import rs.getgo.backend.model.enums.RequestStatus;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.repositories.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final CompletedRideRepository completedRideRepository;
    private final PassengerRepository passengerRepository;
    private final PersonalChangeRequestRepository personalChangeRequestRepo;
    private final VehicleChangeRequestRepository vehicleChangeRequestRepo;
    private final AvatarChangeRequestRepository avatarChangeRequestRepo;
    private final DriverActivationTokenRepository driverActivationTokenRepo;
    private final ActiveRideRepository activeRideRepository;
    private final ModelMapper modelMapper;
    private final FileStorageService fileStorageService;
    private final BCryptPasswordEncoder passwordEncoder;

    public DriverServiceImpl(
            DriverRepository driverRepository,
            CompletedRideRepository completedRideRepository,
            PassengerRepository passengerRepository,
            PersonalChangeRequestRepository personalChangeRequestRepo,
            VehicleChangeRequestRepository vehicleChangeRequestRepo,
            AvatarChangeRequestRepository avatarChangeRequestRepo,
            DriverActivationTokenRepository driverActivationTokenRepo,
            ActiveRideRepository activeRideRepository,
            ModelMapper modelMapper,
            FileStorageService fileStorageService,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.driverRepository = driverRepository;
        this.completedRideRepository = completedRideRepository;
        this.passengerRepository = passengerRepository;
        this.personalChangeRequestRepo = personalChangeRequestRepo;
        this.vehicleChangeRequestRepo = vehicleChangeRequestRepo;
        this.avatarChangeRequestRepo = avatarChangeRequestRepo;
        this.driverActivationTokenRepo = driverActivationTokenRepo;
        this.activeRideRepository = activeRideRepository;
        this.modelMapper = modelMapper;
        this.fileStorageService = fileStorageService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<GetRideDTO> getDriverRides(String email, LocalDate startDate) {
        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found with email: " + email));

        List<CompletedRide> rides = completedRideRepository.findByDriverId(driver.getId());

        List<GetRideDTO> dtoList = new ArrayList<>();

        for (CompletedRide r : rides) {

            // filtering by startDate
            if (startDate != null) {
                if (r.getStartTime() == null) {
                    continue;
                }
                if (!r.getStartTime().toLocalDate().isEqual(startDate)) {
                    continue;
                }
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

    @Override
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

    @Override
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
        driverRepository.save(driver);

        // Mark token as used
        activationToken.setUsed(true);
        activationToken.setUsedAt(LocalDateTime.now());
        driverActivationTokenRepo.save(activationToken);

        return new UpdatedPasswordDTO(true, "Password set successfully. You can now log in.");
    }

    @Override
    public GetDriverDTO getDriver(String email) {
        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found with email: " + email));

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

        dto.setProfilePictureUrl(driver.getProfilePictureUrl());

        // TODO: Calculate recent hours worked in last 24h
        dto.setRecentHoursWorked(0);

        return dto;
    }

    @Override
    public UpdatedPasswordDTO updatePassword(String email, UpdatePasswordDTO updatePasswordDTO) {
        if (!updatePasswordDTO.getPassword().equals(updatePasswordDTO.getConfirmPassword())) {
            return new UpdatedPasswordDTO(false, "Passwords do not match");
        }

        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found with email: " + email));

        if (!passwordEncoder.matches(updatePasswordDTO.getOldPassword(), driver.getPassword())) {
            return new UpdatedPasswordDTO(false, "Old password is incorrect");
        }

        driver.setPassword(passwordEncoder.encode(updatePasswordDTO.getPassword()));
        driverRepository.save(driver);

        return new UpdatedPasswordDTO(true, "Password updated successfully");
    }

    @Override
    public CreatedDriverChangeRequestDTO requestPersonalInfoChange(String email, UpdateDriverPersonalDTO updateDTO) {
        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found with email: " + email));

        // Check for existing pending request
        if (personalChangeRequestRepo.existsByDriverAndStatus(driver, RequestStatus.PENDING)) {
            throw new RuntimeException("You already have a pending personal information change request");
        }

        // Check if no changes were made
        if (!hasPersonalInfoChanged(driver, updateDTO)) {
            throw new RuntimeException("No changes detected in personal information");
        }

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
                driver.getId(),
                "PENDING",
                "Personal info change request created successfully"
        );
    }

    private boolean hasPersonalInfoChanged(Driver driver, UpdateDriverPersonalDTO updateDTO) {
        return !driver.getName().equals(updateDTO.getName()) ||
                !driver.getSurname().equals(updateDTO.getSurname()) ||
                !driver.getPhone().equals(updateDTO.getPhone()) ||
                !driver.getAddress().equals(updateDTO.getAddress());
    }

    @Override
    public CreatedDriverChangeRequestDTO requestVehicleInfoChange(String email, UpdateDriverVehicleDTO updateDTO) {
        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + email));

        // Check for existing pending request
        if (vehicleChangeRequestRepo.existsByDriverAndStatus(driver, RequestStatus.PENDING)) {
            throw new RuntimeException("You already have a pending vehicle information change request");
        }

        Vehicle vehicle = driver.getVehicle();
        if (vehicle == null) {
            throw new RuntimeException("Driver does not have a vehicle assigned");
        }

        // Check if no changes were made
        if (!hasVehicleInfoChanged(vehicle, updateDTO)) {
            throw new RuntimeException("No changes detected in vehicle information");
        }

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
                driver.getId(),
                "PENDING",
                "Vehicle info change request created successfully"
        );
    }

    private boolean hasVehicleInfoChanged(Vehicle vehicle, UpdateDriverVehicleDTO updateDTO) {
        return !vehicle.getModel().equals(updateDTO.getVehicleModel()) ||
                !vehicle.getType().toString().equals(updateDTO.getVehicleType()) ||
                !vehicle.getLicensePlate().equals(updateDTO.getVehicleLicensePlate()) ||
                vehicle.getNumberOfSeats() != updateDTO.getVehicleSeats() ||
                !vehicle.getIsBabyFriendly().equals(updateDTO.getVehicleHasBabySeats()) ||
                !vehicle.getIsPetFriendly().equals(updateDTO.getVehicleAllowsPets());
    }

    @Override
    public CreatedDriverChangeRequestDTO requestProfilePictureChange(String email, MultipartFile file) {
        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + email));

        // Check for existing pending request
        if (avatarChangeRequestRepo.existsByDriverAndStatus(driver, RequestStatus.PENDING)) {
            throw new RuntimeException("You already have a pending profile picture change request");
        }

        // Store file temporarily until approval/rejection
        String filename = fileStorageService.storeFile(file, "driver_pending_" + driver.getId());

        // Create change request
        AvatarChangeRequest changeRequest = new AvatarChangeRequest();
        changeRequest.setDriver(driver);
        changeRequest.setRequestedProfilePictureUrl(filename);
        changeRequest.setStatus(RequestStatus.PENDING);
        changeRequest.setCreatedAt(LocalDateTime.now());

        AvatarChangeRequest savedRequest = avatarChangeRequestRepo.save(changeRequest);

        return new CreatedDriverChangeRequestDTO(
                savedRequest.getId(),
                driver.getId(),
                "PENDING",
                "Profile picture change request created successfully"
        );
    }

    @Override
    public void updateLocation(String driverEmail, Double latitude, Double longitude) {
        Driver driver = driverRepository.findByEmail(driverEmail)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        driver.setCurrentLatitude(latitude);
        driver.setCurrentLongitude(longitude);
        driver.setLastLocationUpdate(LocalDateTime.now());

        driverRepository.save(driver);
    }

    @Override
    public UpdateDriverLocationDTO getLocation(String driverEmail) {
        Driver driver = driverRepository.findByEmail(driverEmail)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        return new UpdateDriverLocationDTO(
                driver.getCurrentLatitude(),
                driver.getCurrentLongitude()
        );
    }

    @Override
    public void updateActiveStatus(String driverEmail, boolean isActive) {
        Driver driver = driverRepository.findByEmail(driverEmail)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        driver.setActive(isActive);
        driverRepository.save(driver);
    }

    @Override
    public Driver findAvailableDriver(ActiveRide ride) {
        // Get starting point coordinates
        WayPoint startPoint = ride.getRoute().getWaypoints().getFirst();
        double startLat = startPoint.getLatitude();
        double startLng = startPoint.getLongitude();

        // Get all active drivers
        List<Driver> allActiveDrivers = driverRepository.findByIsActive(true);
        if (allActiveDrivers.isEmpty()) {
            return null;
        }

        // Filter by vehicle type
        List<Driver> eligibleDrivers = allActiveDrivers.stream()
                .filter(driver -> isVehicleTypeMatch(driver, ride))
                .toList();
        if (eligibleDrivers.isEmpty()) {
            return null;
        }

        // Filter out those who worked 8+ hours
        List<Driver> nonOverworkedDrivers = eligibleDrivers.stream()
                .filter(driver -> !hasExceededWorkingHours(driver))
                .toList();
        if (nonOverworkedDrivers.isEmpty()) {
            return null;
        }

        // Separate drivers into: FREE and BUSY
        List<Driver> freeDrivers = nonOverworkedDrivers.stream()
                .filter(this::isFree)
                .toList();

        List<Driver> busyDrivers = nonOverworkedDrivers.stream()
                .filter(driver -> !isFree(driver))
                .toList();

        // Free drivers first
        if (!freeDrivers.isEmpty()) {
            // Find closest free driver to starting point
            return findClosestDriver(freeDrivers, startLat, startLng);
        }

        // If no free drivers, check busy drivers finishing soon
        List<Driver> finishingSoonDrivers = busyDrivers.stream()
                .filter(this::isFinishingSoon) // 10 minutes or less remaining
                .toList();
        if (finishingSoonDrivers.isEmpty()) {
            return null; // All drivers busy and won't finish soon
        }

        // Find closest driver among those finishing soon
        return findClosestDriver(finishingSoonDrivers, startLat, startLng);
    }

    private boolean isVehicleTypeMatch(Driver driver, ActiveRide ride) {
        if (ride.getVehicleType() == null) {
            return true;
        }
        return driver.getVehicle().getType() == ride.getVehicleType();
    }

    private boolean hasExceededWorkingHours(Driver driver) {
        List<CompletedRide> recentRides = completedRideRepository
                .findByDriverIdAndEndTimeAfter(driver.getId(), LocalDateTime.now().minusHours(24));

        // Calculate total working hours
        double totalMinutes = 0;
        for (CompletedRide completedRide : recentRides) {
            if (completedRide.getStartTime() != null && completedRide.getEndTime() != null) {
                long minutes = java.time.Duration.between(
                        completedRide.getStartTime(),
                        completedRide.getEndTime()
                ).toMinutes();
                totalMinutes += minutes;
            }
        }

        // Factor in current driving time
        ActiveRide currentRide = activeRideRepository
                .findByDriverAndStatus(driver, RideStatus.ACTIVE)
                .orElse(null);
        if (currentRide != null && currentRide.getActualStartTime() != null) {
            long currentRideMinutes = java.time.Duration.between(
                    currentRide.getActualStartTime(),
                    LocalDateTime.now()
            ).toMinutes();
            totalMinutes += currentRideMinutes;
        }

        return (totalMinutes / 60.0) >= 8.0;
    }

    private boolean isFree(Driver driver) {
        boolean hasActiveRide = activeRideRepository
                .findByDriverAndStatus(driver, RideStatus.ACTIVE)
                .isPresent();
        if (hasActiveRide) {
            return false;
        }
        boolean hasScheduledRide = activeRideRepository
                .findByDriverAndStatus(driver, RideStatus.SCHEDULED)
                .isPresent();

        return !hasScheduledRide;
    }

    private boolean isFinishingSoon(Driver driver) {
        ActiveRide currentRide = activeRideRepository
                .findByDriverAndStatus(driver, RideStatus.ACTIVE)
                .orElse(null);
        if (currentRide == null || currentRide.getActualStartTime() == null) {
            return false; // Not on an active ride or hasn't started yet
        }

        // Calculate elapsed time
        long elapsedMinutes = java.time.Duration.between(
                currentRide.getActualStartTime(),
                LocalDateTime.now()
        ).toMinutes();

        // Estimate remaining time
        double estimatedTotalMinutes = currentRide.getRoute().getEstTimeMin();
        double remainingMinutes = estimatedTotalMinutes - elapsedMinutes;

        return remainingMinutes <= 10;
    }

    // TODO: CALL MAPS API FOR THIS?
    private Driver findClosestDriver(List<Driver> drivers, double targetLat, double targetLng) {
        Driver closestDriver = null;
        double minDistanceSquared = Double.MAX_VALUE;

        // Correct for map distortion
        double latRad = Math.toRadians(targetLat);
        double cosLat = Math.cos(latRad);

        for (Driver driver : drivers) {
            if (driver.getCurrentLatitude() == null || driver.getCurrentLongitude() == null) {
                continue;
            }

            double dLat = driver.getCurrentLatitude() - targetLat;
            double dLng = (driver.getCurrentLongitude() - targetLng) * cosLat;
            double distanceSquared = dLat * dLat + dLng * dLng;

            if (distanceSquared < minDistanceSquared) {
                minDistanceSquared = distanceSquared;
                closestDriver = driver;
            }
        }

        return closestDriver;
    }
}