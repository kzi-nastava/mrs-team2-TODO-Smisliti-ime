package rs.getgo.backend.services;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.getgo.backend.dtos.authentication.GetActivationTokenDTO;
import rs.getgo.backend.dtos.authentication.UpdateDriverPasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatePasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatedPasswordDTO;
import rs.getgo.backend.dtos.driver.*;
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
import java.util.stream.Collectors;

@Service
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final CompletedRideRepository completedRideRepository;
    private final PassengerRepository passengerRepository;
    private final PersonalChangeRequestRepository personalChangeRequestRepo;
    private final VehicleChangeRequestRepository vehicleChangeRequestRepo;
    private final AvatarChangeRequestRepository avatarChangeRequestRepo;
    private final DriverActivationTokenRepository driverActivationTokenRepo;
    private final BlockNoteRepository blockNoteRepository;
    private final ActiveRideRepository activeRideRepository;
    private final ModelMapper modelMapper;
    private final FileStorageService fileStorageService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public DriverServiceImpl(
            DriverRepository driverRepository,
            CompletedRideRepository completedRideRepository,
            PassengerRepository passengerRepository,
            PersonalChangeRequestRepository personalChangeRequestRepo,
            VehicleChangeRequestRepository vehicleChangeRequestRepo,
            AvatarChangeRequestRepository avatarChangeRequestRepo,
            DriverActivationTokenRepository driverActivationTokenRepo,
            BlockNoteRepository blockNoteRepository,
            ActiveRideRepository activeRideRepository,
            ModelMapper modelMapper,
            FileStorageService fileStorageService,
            BCryptPasswordEncoder passwordEncoder,
            UserRepository userRepository
    ) {
        this.driverRepository = driverRepository;
        this.completedRideRepository = completedRideRepository;
        this.passengerRepository = passengerRepository;
        this.personalChangeRequestRepo = personalChangeRequestRepo;
        this.vehicleChangeRequestRepo = vehicleChangeRequestRepo;
        this.avatarChangeRequestRepo = avatarChangeRequestRepo;
        this.driverActivationTokenRepo = driverActivationTokenRepo;
        this.blockNoteRepository = blockNoteRepository;
        this.activeRideRepository = activeRideRepository;
        this.modelMapper = modelMapper;
        this.fileStorageService = fileStorageService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public List<GetActiveDriverLocationDTO> getActiveDriverLocations() {
        List<Driver> activeDrivers = driverRepository.findByIsActive(true);

        return activeDrivers.stream()
                .map(driver -> {
                    GetActiveDriverLocationDTO dto = new GetActiveDriverLocationDTO();
                    dto.setDriverId(driver.getId());
                    dto.setLatitude(driver.getCurrentLatitude());
                    dto.setLongitude(driver.getCurrentLongitude());
                    dto.setVehicleType(driver.getVehicle().getType().toString());

                    boolean isBusy = activeRideRepository.existsByDriverAndStatusIn(
                            driver,
                            List.of(RideStatus.DRIVER_READY, RideStatus.DRIVER_INCOMING,
                                    RideStatus.DRIVER_ARRIVED, RideStatus.ACTIVE, RideStatus.DRIVER_ARRIVED_AT_DESTINATION
                            )
                    );
                    dto.setIsAvailable(!isBusy);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<GetRideDTO> getDriverRides(String email, LocalDate startDate, int page, int size) {
        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found with email: " + email));

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("startTime").descending()
        );

        Page<CompletedRide> ridesPage;

        if (startDate == null) {
            ridesPage = completedRideRepository
                    .findByDriverId(driver.getId(), pageable);
        } else {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = startDate.atTime(23, 59, 59);

            ridesPage = completedRideRepository.findByDriverIdAndStartTimeBetween(
                    driver.getId(),
                    start,
                    end,
                    pageable
            );
        }

        return ridesPage.map(this::mapToDTO);
    }

    private GetRideDTO mapToDTO(CompletedRide r) {

        List<GetRidePassengerDTO> passengerDTOs = new ArrayList<>();

        if (r.getPayingPassengerId() != null) {
            passengerDTOs.add(
                    new GetRidePassengerDTO(
                            r.getPayingPassengerId(),
                            r.getPayingPassengerEmail()
                    )
            );
        }

        if (r.getLinkedPassengerIds() != null && !r.getLinkedPassengerIds().isEmpty()) {
            List<Passenger> passengers =
                    passengerRepository.findAllById(r.getLinkedPassengerIds());

            passengers.forEach(p ->
                    passengerDTOs.add(
                            new GetRidePassengerDTO(p.getId(), p.getEmail())
                    )
            );
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
                r.getStartTime() != null && r.getEndTime() != null
                        ? (int) java.time.Duration
                        .between(r.getStartTime(), r.getEndTime())
                        .toMinutes()
                        : 0,
                r.isCancelled(),
                false,
                r.isCompletedNormally() ? "FINISHED" :
                        (r.isCancelled() ? "CANCELLED" : "ACTIVE"),
                r.getEstimatedPrice(),
                r.isPanicPressed(),
                r.getEstDistanceKm(),
                r.getEstTime(),
                cancelledUserEmail,
                r.getCancelReason()
        );
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

        dto.setRecentHoursWorked(calculateRecentHoursWorked(email));

        dto.setBlocked(driver.isBlocked());
        if (driver.isBlocked()) {
            blockNoteRepository.findByUserAndUnblockedAtIsNull(driver)
                    .ifPresent(note -> dto.setBlockReason(note.getReason()));
        }

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

        // Check if driver has any active rides before allowing status change
        if (!isActive) {
            boolean hasActiveRide = activeRideRepository.existsByDriverAndStatusIn(
                    driver,
                    List.of(RideStatus.DRIVER_READY, RideStatus.DRIVER_INCOMING,
                            RideStatus.DRIVER_ARRIVED, RideStatus.ACTIVE,
                            RideStatus.DRIVER_ARRIVED_AT_DESTINATION)
            );

            if (hasActiveRide) {
                throw new RuntimeException("Cannot change status. Driver has an active ride.");
            }
        }

        driver.setActive(isActive);
        driverRepository.save(driver);
    }

    @Override
    public boolean isDriverActive(String email) {
        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        return driver.getActive();
    }

    @Override
    public Driver findAvailableDriver(ActiveRide ride) {
        WayPoint startPoint = ride.getRoute().getWaypoints().getFirst();
        double lat = startPoint.getLatitude();
        double lng = startPoint.getLongitude();

        List<Driver> candidates = driverRepository.findByIsActive(true)
                .stream()
                .filter(d -> !d.isBlocked())
                .filter(d -> isVehicleTypeMatch(d, ride))
                .filter(d -> !hasExceededWorkingHours(d))
                .toList();
        if (candidates.isEmpty()) return null;

        // Try to assign the closest free driver
        List<Driver> freeDrivers = candidates.stream()
                .filter(this::isFree)
                .toList();
        if (!freeDrivers.isEmpty()) return findClosestDriver(freeDrivers, lat, lng);

        // Try to assign the closest driver that's finishing his ride soon and is not reserved
        List<Driver> finishingDrivers = candidates.stream()
                .filter(this::canAcceptNextRideWhileFinishing)
                .toList();
        if (finishingDrivers.isEmpty()) return null;

        return findClosestDriver(finishingDrivers, lat, lng);
    }

    private boolean isVehicleTypeMatch(Driver driver, ActiveRide ride) {
        if (ride.getVehicleType() == null) {
            return true;
        }
        return driver.getVehicle().getType() == ride.getVehicleType();
    }

    private boolean isFree(Driver driver) {
        return !activeRideRepository
                .existsByDriverAndStatusIn(
                        driver,
                        List.of(
                                RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE,
                                RideStatus.DRIVER_READY,
                                RideStatus.DRIVER_INCOMING,
                                RideStatus.DRIVER_ARRIVED,
                                RideStatus.ACTIVE,
                                RideStatus.DRIVER_ARRIVED_AT_DESTINATION
                        )
                );
    }

    private boolean canAcceptNextRideWhileFinishing(Driver driver) {

        boolean hasActive = activeRideRepository
                .existsByDriverAndStatus(driver, RideStatus.ACTIVE);

        boolean hasFutureAssigned = activeRideRepository
                .existsByDriverAndStatusIn(
                        driver,
                        List.of(
                                RideStatus.DRIVER_FINISHING_PREVIOUS_RIDE,
                                RideStatus.DRIVER_INCOMING,
                                RideStatus.SCHEDULED
                        )
                );

        return hasActive && !hasFutureAssigned && isFinishingSoon(driver);
    }

    private boolean isFinishingSoon(Driver driver) {
        ActiveRide currentRide = activeRideRepository
                .findByDriverAndStatus(driver, RideStatus.ACTIVE)
                .orElse(null);
        if (currentRide == null || currentRide.getActualStartTime() == null) {
            return false; // Not in an active ride or hasn't started yet
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

    // Returns closest driver closest to target position
    // 1) Driver free: calculate distance using current driver location
    // 2) Driver finishing previous ride: calculate distance using position of the end of his current ride
    private Driver findClosestDriver(List<Driver> drivers, double targetLat, double targetLng) {
        Driver closest = null;
        double minDistance = Double.MAX_VALUE;

        for (Driver d : drivers) {
            double srcLat;
            double srcLng;

            Optional<ActiveRide> active =
                    activeRideRepository.findByDriverAndStatus(d, RideStatus.ACTIVE);

            if (active.isPresent()) {
                WayPoint end = active.get()
                        .getRoute()
                        .getWaypoints()
                        .getLast();
                srcLat = end.getLatitude();
                srcLng = end.getLongitude();
            } else {
                if (d.getCurrentLatitude() == null || d.getCurrentLongitude() == null)
                    continue;
                srcLat = d.getCurrentLatitude();
                srcLng = d.getCurrentLongitude();
            }

            double dLat = srcLat - targetLat;
            double dLng = (srcLng - targetLng) * Math.cos(Math.toRadians(targetLat));
            double dist = dLat * dLat + dLng * dLng;

            if (dist < minDistance) {
                minDistance = dist;
                closest = d;
            }
        }

        return closest;
    }

    public double calculateRecentHoursWorked(String email) {
        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found with email: " + email));

        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

        List<CompletedRide> recentRides = completedRideRepository
                .findByDriverIdAndEndTimeAfter(driver.getId(), twentyFourHoursAgo);

        double totalMinutes = 0.0;

        for (CompletedRide ride : recentRides) {
            if (ride.getStartTime() != null && ride.getEndTime() != null) {
                long minutes = java.time.Duration.between(
                        ride.getStartTime(),
                        ride.getEndTime()
                ).toMinutes();
                totalMinutes += minutes;
            }
        }

        return Math.round((totalMinutes / 60.0) * 100.0) / 100.0;
    }

    public boolean hasExceededWorkingHours(Driver driver) {
        return calculateRecentHoursWorked(driver.getEmail()) >= 8.0;
    }

    @Override
    public GetDriverDTO findDriverById(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
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

        dto.setProfilePictureUrl(driver.getProfilePictureUrl());

        return dto;
    }
}
