package rs.getgo.backend.services.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import rs.getgo.backend.dtos.admin.*;
import rs.getgo.backend.dtos.authentication.UpdatePasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatedPasswordDTO;
import rs.getgo.backend.dtos.driver.CreateDriverDTO;
import rs.getgo.backend.dtos.driver.CreatedDriverDTO;
import rs.getgo.backend.dtos.passenger.GetRidePassengerDTO;
import rs.getgo.backend.dtos.request.*;
import rs.getgo.backend.dtos.ride.GetReorderRideDTO;
import rs.getgo.backend.dtos.ride.GetRideDTO;
import rs.getgo.backend.dtos.user.BlockUserRequestDTO;
import rs.getgo.backend.dtos.user.BlockUserResponseDTO;
import rs.getgo.backend.dtos.user.UserEmailDTO;
import rs.getgo.backend.model.entities.*;
import rs.getgo.backend.model.enums.RequestStatus;
import rs.getgo.backend.model.enums.UserRole;
import rs.getgo.backend.model.enums.VehicleType;
import rs.getgo.backend.repositories.*;
import rs.getgo.backend.services.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdministratorRepository adminRepo;
    private final DriverRepository driverRepo;
    private final PersonalChangeRequestRepository personalChangeRequestRepo;
    private final VehicleChangeRequestRepository vehicleChangeRequestRepo;
    private final AvatarChangeRequestRepository avatarChangeRequestRepo;
    private final DriverActivationTokenRepository driverActivationTokenRepo;
    private final BlockNoteRepository blockNoteRepository;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CompletedRideRepository completedRideRepo;
    private final PassengerRepository passengerRepo;
    private final UserRepository userRepository;

    public AdminServiceImpl(
            AdministratorRepository adminRepo,
            DriverRepository driverRepo,
            PassengerRepository passengerRepo,
            CompletedRideRepository completedRideRepo,
            PersonalChangeRequestRepository personalChangeRequestRepo,
            VehicleChangeRequestRepository vehicleChangeRequestRepo,
            AvatarChangeRequestRepository avatarChangeRequestRepo,
            DriverActivationTokenRepository driverActivationTokenRepo,
            BlockNoteRepository blockNoteRepository,
            FileStorageService fileStorageService,
            EmailService emailService,
            ModelMapper modelMapper,
            BCryptPasswordEncoder passwordEncoder,
            UserRepository userRepository
    ) {
        this.adminRepo = adminRepo;
        this.driverRepo = driverRepo;
        this.passengerRepo = passengerRepo;
        this.completedRideRepo = completedRideRepo;
        this.personalChangeRequestRepo = personalChangeRequestRepo;
        this.vehicleChangeRequestRepo = vehicleChangeRequestRepo;
        this.avatarChangeRequestRepo = avatarChangeRequestRepo;
        this.driverActivationTokenRepo = driverActivationTokenRepo;
        this.blockNoteRepository = blockNoteRepository;
        this.fileStorageService = fileStorageService;
        this.emailService = emailService;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public CreatedAdminDTO createAdmin(CreateAdminDTO createAdminDTO) {
        if (userRepository.findByEmail(createAdminDTO.getEmail()).isPresent()) {
            throw new RuntimeException("User with email " + createAdminDTO.getEmail() + " already exists");
        }

        Administrator admin = new Administrator();
        admin.setEmail(createAdminDTO.getEmail());
        admin.setPassword(passwordEncoder.encode(createAdminDTO.getPassword()));
        admin.setName(createAdminDTO.getName());
        admin.setSurname(createAdminDTO.getSurname());
        admin.setAddress(createAdminDTO.getAddress());
        admin.setPhone(createAdminDTO.getPhoneNumber());
        admin.setRole(UserRole.ADMIN);
        admin.setBlocked(false);

        Administrator savedAdmin = adminRepo.save(admin);

        return modelMapper.map(savedAdmin, CreatedAdminDTO.class);
    }

    @Override
    public CreatedDriverDTO registerDriver(CreateDriverDTO createDriverDTO) {
        if (userRepository.findByEmail(createDriverDTO.getEmail()).isPresent()) {
            throw new RuntimeException("User with email " + createDriverDTO.getEmail() + " already exists");
        }

        // Validate vehicle type
        try {
            VehicleType.valueOf(createDriverDTO.getVehicleType());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid vehicle type: " + createDriverDTO.getVehicleType());
        }

        Vehicle vehicle = fillVehicle(createDriverDTO);
        Driver driver = fillDriver(createDriverDTO, vehicle);

        // Set base location so it's never null
        driver.setCurrentLatitude(45.240806);
        driver.setCurrentLongitude(19.828611);
        driver.setLastLocationUpdate(LocalDateTime.now());

        // Should save both vehicle and driver due to CascadeType.ALL
        Driver savedDriver = driverRepo.save(driver);

        DriverActivationToken token = createDriverActivationToken(savedDriver);
        driverActivationTokenRepo.save(token);

        emailService.sendDriverActivationEmail(savedDriver.getEmail(), token.getToken());

        return modelMapper.map(savedDriver, CreatedDriverDTO.class);
    }

    // Move to separate service if tokens become used besides here
    private static DriverActivationToken createDriverActivationToken(Driver driver) {
        String tokenString = UUID.randomUUID().toString();
        DriverActivationToken token = new DriverActivationToken();
        token.setToken(tokenString);
        token.setDriver(driver);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusHours(24));
        token.setUsed(false);
        return token;
    }

    private static Vehicle fillVehicle(CreateDriverDTO createDriverDTO) {
        Vehicle vehicle = new Vehicle();
        vehicle.setModel(createDriverDTO.getVehicleModel());
        vehicle.setType(VehicleType.valueOf(createDriverDTO.getVehicleType()));
        vehicle.setLicensePlate(createDriverDTO.getVehicleLicensePlate());
        vehicle.setNumberOfSeats(createDriverDTO.getVehicleSeats());
        vehicle.setIsBabyFriendly(createDriverDTO.getVehicleHasBabySeats());
        vehicle.setIsPetFriendly(createDriverDTO.getVehicleAllowsPets());
        vehicle.setIsAvailable(true);
        return vehicle;
    }

    private Driver fillDriver(CreateDriverDTO createDriverDTO, Vehicle vehicle) {
        Driver driver = new Driver();
        driver.setEmail(createDriverDTO.getEmail());
        driver.setName(createDriverDTO.getName());
        driver.setSurname(createDriverDTO.getSurname());
        driver.setPhone(createDriverDTO.getPhone());
        driver.setAddress(createDriverDTO.getAddress());
        driver.setRole(UserRole.DRIVER);
        driver.setProfilePictureUrl(null);
        driver.setActive(false);
        driver.setActivated(false);
        driver.setBlocked(false);
        driver.setVehicle(vehicle);
        // Password is set later
        return driver;
    }

    @Override
    public GetAdminDTO getAdmin(String email) {
        Administrator admin = adminRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found with email: " + email));

        return modelMapper.map(admin, GetAdminDTO.class);
    }

    @Override
    public UpdatedAdminDTO updateProfile(String email, UpdateAdminDTO updateAdminDTO) {
        Administrator admin = adminRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found with email: " + email));

        admin.setName(updateAdminDTO.getName().trim());
        admin.setSurname(updateAdminDTO.getSurname().trim());
        admin.setPhone(updateAdminDTO.getPhone().trim());
        admin.setAddress(updateAdminDTO.getAddress().trim());

        Administrator savedAdmin = adminRepo.save(admin);

        return modelMapper.map(savedAdmin, UpdatedAdminDTO.class);
    }

    @Override
    public UpdatedPasswordDTO updatePassword(String email, UpdatePasswordDTO updatePasswordDTO) {
        if (!updatePasswordDTO.getPassword().equals(updatePasswordDTO.getConfirmPassword())) {
            return new UpdatedPasswordDTO(false, "Passwords do not match");
        }

        Administrator admin = adminRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found with email: " + email));

        if (!passwordEncoder.matches(updatePasswordDTO.getOldPassword(), admin.getPassword())) {
            return new UpdatedPasswordDTO(false, "Old password is incorrect");
        }

        admin.setPassword(passwordEncoder.encode(updatePasswordDTO.getPassword()));
        adminRepo.save(admin);

        return new UpdatedPasswordDTO(true, "Password updated successfully");
    }

    @Override
    public Page<GetPersonalDriverChangeRequestDTO> getPendingPersonalChangeRequests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PersonalChangeRequest> requests = personalChangeRequestRepo.findByStatus(RequestStatus.PENDING, pageable);

        return requests.map(request -> {
            Driver driver = request.getDriver();

            GetPersonalDriverChangeRequestDTO dto = new GetPersonalDriverChangeRequestDTO();
            dto.setRequestId(request.getId());
            dto.setDriverId(driver.getId());
            dto.setDriverEmail(driver.getEmail());
            dto.setDriverName(driver.getName() + " " + driver.getSurname());

            // Current data
            dto.setCurrentName(driver.getName());
            dto.setCurrentSurname(driver.getSurname());
            dto.setCurrentPhone(driver.getPhone());
            dto.setCurrentAddress(driver.getAddress());

            // Requested data (kept as-is on request)
            dto.setRequestedName(request.getRequestedName());
            dto.setRequestedSurname(request.getRequestedSurname());
            dto.setRequestedPhone(request.getRequestedPhone());
            dto.setRequestedAddress(request.getRequestedAddress());

            dto.setStatus(request.getStatus().toString());
            dto.setCreatedAt(request.getCreatedAt());

            return dto;
        });
    }

    @Override
    public Page<GetDriverVehicleChangeRequestDTO> getPendingVehicleChangeRequests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<VehicleChangeRequest> requests = vehicleChangeRequestRepo.findByStatus(RequestStatus.PENDING, pageable);

        return requests.map(request -> {
            Driver driver = request.getDriver();
            Vehicle vehicle = driver.getVehicle();

            GetDriverVehicleChangeRequestDTO dto = new GetDriverVehicleChangeRequestDTO();
            dto.setRequestId(request.getId());
            dto.setDriverId(driver.getId());
            dto.setDriverEmail(driver.getEmail());
            dto.setDriverName(driver.getName() + " " + driver.getSurname());

            // Current vehicle data
            dto.setCurrentVehicleModel(vehicle.getModel());
            dto.setCurrentVehicleType(vehicle.getType().toString());
            dto.setCurrentVehicleLicensePlate(vehicle.getLicensePlate());
            dto.setCurrentVehicleSeats(vehicle.getNumberOfSeats());
            dto.setCurrentVehicleHasBabySeats(vehicle.getIsBabyFriendly());
            dto.setCurrentVehicleAllowsPets(vehicle.getIsPetFriendly());

            // Requested vehicle data
            dto.setRequestedVehicleModel(request.getRequestedVehicleModel());
            dto.setRequestedVehicleType(request.getRequestedVehicleType());
            dto.setRequestedVehicleLicensePlate(request.getRequestedVehicleLicensePlate());
            dto.setRequestedVehicleSeats(request.getRequestedVehicleSeats());
            dto.setRequestedVehicleHasBabySeats(request.getRequestedVehicleHasBabySeats());
            dto.setRequestedVehicleAllowsPets(request.getRequestedVehicleAllowsPets());

            dto.setStatus(request.getStatus().toString());
            dto.setCreatedAt(request.getCreatedAt());

            return dto;
        });
    }

    @Override
    public Page<GetDriverAvatarChangeRequestDTO> getPendingAvatarChangeRequests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AvatarChangeRequest> requests = avatarChangeRequestRepo.findByStatus(RequestStatus.PENDING, pageable);

        return requests.map(request -> {
            Driver driver = request.getDriver();

            GetDriverAvatarChangeRequestDTO dto = new GetDriverAvatarChangeRequestDTO();
            dto.setRequestId(request.getId());
            dto.setDriverId(driver.getId());
            dto.setDriverEmail(driver.getEmail());
            dto.setDriverName(driver.getName() + " " + driver.getSurname());

            // Current profile picture
            dto.setCurrentProfilePictureUrl(driver.getProfilePictureUrl());

            // Requested profile picture
            dto.setRequestedProfilePictureUrl(request.getRequestedProfilePictureUrl());

            dto.setStatus(request.getStatus().toString());
            dto.setCreatedAt(request.getCreatedAt());

            return dto;
        });
    }

    @Override
    public GetPersonalDriverChangeRequestDTO getPersonalChangeRequest(Long requestId) {
        PersonalChangeRequest request = personalChangeRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Personal change request not found with id: " + requestId));

        Driver driver = request.getDriver();

        GetPersonalDriverChangeRequestDTO dto = new GetPersonalDriverChangeRequestDTO();
        dto.setRequestId(request.getId());
        dto.setDriverId(driver.getId());
        dto.setDriverEmail(driver.getEmail());
        dto.setDriverName(driver.getName() + " " + driver.getSurname());

        dto.setCurrentName(driver.getName());
        dto.setCurrentSurname(driver.getSurname());
        dto.setCurrentPhone(driver.getPhone());
        dto.setCurrentAddress(driver.getAddress());

        dto.setRequestedName(request.getRequestedName());
        dto.setRequestedSurname(request.getRequestedSurname());
        dto.setRequestedPhone(request.getRequestedPhone());
        dto.setRequestedAddress(request.getRequestedAddress());

        dto.setStatus(request.getStatus().toString());
        dto.setCreatedAt(request.getCreatedAt());

        return dto;
    }

    @Override
    public GetDriverVehicleChangeRequestDTO getVehicleChangeRequest(Long requestId) {
        VehicleChangeRequest request = vehicleChangeRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Vehicle change request not found with id: " + requestId));

        Driver driver = request.getDriver();
        Vehicle vehicle = driver.getVehicle();

        GetDriverVehicleChangeRequestDTO dto = new GetDriverVehicleChangeRequestDTO();
        dto.setRequestId(request.getId());
        dto.setDriverId(driver.getId());
        dto.setDriverEmail(driver.getEmail());
        dto.setDriverName(driver.getName() + " " + driver.getSurname());

        dto.setCurrentVehicleModel(vehicle.getModel());
        dto.setCurrentVehicleType(vehicle.getType().toString());
        dto.setCurrentVehicleLicensePlate(vehicle.getLicensePlate());
        dto.setCurrentVehicleSeats(vehicle.getNumberOfSeats());
        dto.setCurrentVehicleHasBabySeats(vehicle.getIsBabyFriendly());
        dto.setCurrentVehicleAllowsPets(vehicle.getIsPetFriendly());

        dto.setRequestedVehicleModel(request.getRequestedVehicleModel());
        dto.setRequestedVehicleType(request.getRequestedVehicleType());
        dto.setRequestedVehicleLicensePlate(request.getRequestedVehicleLicensePlate());
        dto.setRequestedVehicleSeats(request.getRequestedVehicleSeats());
        dto.setRequestedVehicleHasBabySeats(request.getRequestedVehicleHasBabySeats());
        dto.setRequestedVehicleAllowsPets(request.getRequestedVehicleAllowsPets());

        dto.setStatus(request.getStatus().toString());
        dto.setCreatedAt(request.getCreatedAt());

        return dto;
    }

    @Override
    public GetDriverAvatarChangeRequestDTO getAvatarChangeRequest(Long requestId) {
        AvatarChangeRequest request = avatarChangeRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Avatar change request not found with id: " + requestId));

        Driver driver = request.getDriver();

        GetDriverAvatarChangeRequestDTO dto = new GetDriverAvatarChangeRequestDTO();
        dto.setRequestId(request.getId());
        dto.setDriverId(driver.getId());
        dto.setDriverEmail(driver.getEmail());
        dto.setDriverName(driver.getName() + " " + driver.getSurname());

        dto.setCurrentProfilePictureUrl(driver.getProfilePictureUrl());
        dto.setRequestedProfilePictureUrl(request.getRequestedProfilePictureUrl());

        dto.setStatus(request.getStatus().toString());
        dto.setCreatedAt(request.getCreatedAt());

        return dto;
    }

    @Override
    public AcceptDriverChangeRequestDTO approvePersonalChangeRequest(Long requestId, String email) {
        PersonalChangeRequest request = personalChangeRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Personal change request not found with id: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request has already been reviewed");
        }

        Administrator admin = adminRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found with email: " + email));

        Driver driver = request.getDriver();

        // Apply changes
        driver.setName(request.getRequestedName());
        driver.setSurname(request.getRequestedSurname());
        driver.setPhone(request.getRequestedPhone());
        driver.setAddress(request.getRequestedAddress());
        driverRepo.save(driver);

        // Update request status
        request.setStatus(RequestStatus.APPROVED);
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        personalChangeRequestRepo.save(request);

        return new AcceptDriverChangeRequestDTO(
                request.getId(),
                driver.getId(),
                request.getStatus().toString(),
                admin.getId(),
                LocalDateTime.now()
        );
    }

    @Override
    public AcceptDriverChangeRequestDTO approveVehicleChangeRequest(Long requestId, String email) {
        VehicleChangeRequest request = vehicleChangeRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Vehicle change request not found with id: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request has already been reviewed");
        }

        Administrator admin = adminRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found with email: " + email));

        Driver driver = request.getDriver();
        Vehicle vehicle = driver.getVehicle();

        if (vehicle == null) {
            throw new RuntimeException("Driver does not have a vehicle assigned");
        }

        // Apply changes to vehicle
        vehicle.setModel(request.getRequestedVehicleModel());
        vehicle.setType(VehicleType.valueOf(request.getRequestedVehicleType()));
        vehicle.setLicensePlate(request.getRequestedVehicleLicensePlate());
        vehicle.setNumberOfSeats(request.getRequestedVehicleSeats());
        vehicle.setIsBabyFriendly(request.getRequestedVehicleHasBabySeats());
        vehicle.setIsPetFriendly(request.getRequestedVehicleAllowsPets());
        driverRepo.save(driver);

        // Update request status
        request.setStatus(RequestStatus.APPROVED);
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        vehicleChangeRequestRepo.save(request);

        return new AcceptDriverChangeRequestDTO(
                request.getId(),
                driver.getId(),
                request.getStatus().toString(),
                admin.getId(),
                LocalDateTime.now()
        );
    }

    @Override
    public AcceptDriverChangeRequestDTO approveAvatarChangeRequest(Long requestId, String email) {
        AvatarChangeRequest request = avatarChangeRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Avatar change request not found with id: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request has already been reviewed");
        }

        Administrator admin = adminRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found with email: " + email));

        Driver driver = request.getDriver();

        // Delete old profile picture and apply new one
        replaceDriverProfilePicture(driver, request.getRequestedProfilePictureUrl());

        // Update request status
        request.setStatus(RequestStatus.APPROVED);
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        avatarChangeRequestRepo.save(request);

        return new AcceptDriverChangeRequestDTO(
                request.getId(),
                driver.getId(),
                request.getStatus().toString(),
                admin.getId(),
                LocalDateTime.now()
        );
    }

    private void replaceDriverProfilePicture(Driver driver, String pendingProfilePictureUrl) {
        if (driver.getProfilePictureUrl() != null) {
            fileStorageService.deleteFile(driver.getProfilePictureUrl());
        }

        String newUrl = fileStorageService.generateProfilePictureUrl("driver", driver.getId(), pendingProfilePictureUrl);
        String approvedFileUrl = fileStorageService.renameFile(pendingProfilePictureUrl, newUrl);

        driver.setProfilePictureUrl(approvedFileUrl);
        driverRepo.save(driver);
    }

    @Override
    public AcceptDriverChangeRequestDTO rejectPersonalChangeRequest(Long requestId, String email, RejectDriverChangeRequestDTO rejectDTO) {
        PersonalChangeRequest request = personalChangeRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Personal change request not found with id: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request has already been reviewed");
        }

        Administrator admin = adminRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found with email: " + email));

        request.setStatus(RequestStatus.REJECTED);
        request.setRejectionReason(rejectDTO.getReason());
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        personalChangeRequestRepo.save(request);

        return new AcceptDriverChangeRequestDTO(
                request.getId(),
                request.getDriver().getId(),
                request.getStatus().toString(),
                admin.getId(),
                LocalDateTime.now()
        );
    }

    @Override
    public AcceptDriverChangeRequestDTO rejectVehicleChangeRequest(Long requestId, String email, RejectDriverChangeRequestDTO rejectDTO) {
        VehicleChangeRequest request = vehicleChangeRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Vehicle change request not found with id: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request has already been reviewed");
        }

        Administrator admin = adminRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found with email: " + email));

        request.setStatus(RequestStatus.REJECTED);
        request.setRejectionReason(rejectDTO.getReason());
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        vehicleChangeRequestRepo.save(request);

        return new AcceptDriverChangeRequestDTO(
                request.getId(),
                request.getDriver().getId(),
                request.getStatus().toString(),
                admin.getId(),
                LocalDateTime.now()
        );
    }

    @Override
    public AcceptDriverChangeRequestDTO rejectAvatarChangeRequest(Long requestId, String email, RejectDriverChangeRequestDTO rejectDTO) {
        AvatarChangeRequest request = avatarChangeRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Avatar change request not found with id: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request has already been reviewed");
        }

        Administrator admin = adminRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found with email: " + email));

        // Delete the pending profile picture file
        fileStorageService.deleteFile(request.getRequestedProfilePictureUrl());

        request.setStatus(RequestStatus.REJECTED);
        request.setRejectionReason(rejectDTO.getReason());
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        avatarChangeRequestRepo.save(request);

        return new AcceptDriverChangeRequestDTO(
                request.getId(),
                request.getDriver().getId(),
                request.getStatus().toString(),
                admin.getId(),
                LocalDateTime.now()
        );
    }

    @Override
    public Page<GetRideDTO> getPassengerRides(String email, LocalDate startDate, int page, int size, String sortBy, String direction) {
        Passenger passenger = passengerRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Passenger not found with email: " + email));

        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<CompletedRide> ridesPage;
        if (startDate != null) {
            // Filter rides ON the specific date (from 00:00 to 23:59:59.999)
            ridesPage = completedRideRepo.findByPassengerIdAndStartTimeBetween(
                    passenger.getId(),
                    startDate.atStartOfDay(),
                    startDate.plusDays(1).atStartOfDay(),
                    pageable);
        } else {
            // Get all rides where passenger is either paying or linked
            ridesPage = completedRideRepo.findByPassengerId(passenger.getId(), pageable);
        }

        return ridesPage.map(this::mapCompletedRideToDTO);
    }

    @Override
    public GetReorderRideDTO getPassengerRideById(String email, Long rideId) {
        Passenger passenger = passengerRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Passenger not found with email: " + email));

        CompletedRide ride = completedRideRepo.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with id: " + rideId));

        boolean isPaying = ride.getPayingPassengerId() != null && ride.getPayingPassengerId().equals(passenger.getId());
        boolean isLinked = ride.getLinkedPassengerIds() != null && ride.getLinkedPassengerIds().contains(passenger.getId());

        if (!isPaying && !isLinked) {
            throw new RuntimeException("Passenger is not allowed to view this ride");
        }

        return mapCompletedReorderRideToDTO(ride);
    }

    @Override
    public Page<GetRideDTO> getDriverRides(String email, LocalDate startDate, int page, int size, String sortBy, String direction) {
        Driver driver = driverRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found with email: " + email));

        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<CompletedRide> ridesPage;
        if (startDate != null) {
            // Filter rides ON the specific date (from 00:00 to 23:59:59.999)
            ridesPage = completedRideRepo.findByDriverIdAndStartTimeBetween(
                    driver.getId(),
                    startDate.atStartOfDay(),
                    startDate.plusDays(1).atStartOfDay(),
                    pageable);
        } else {
            ridesPage = completedRideRepo.findByDriverId(driver.getId(), pageable);
        }

        return ridesPage.map(this::mapCompletedRideToDTO);
    }

    @Override
    public GetReorderRideDTO getDriverRideById(String email, Long rideId) {
        Driver driver = driverRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found with email: " + email));

        CompletedRide ride = completedRideRepo.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with id: " + rideId));

        if (!ride.getDriverId().equals(driver.getId())) {
            throw new RuntimeException("Driver is not allowed to view this ride");
        }

        return mapCompletedReorderRideToDTO(ride);
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

    private GetReorderRideDTO mapCompletedReorderRideToDTO(CompletedRide r) {
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

    @Override
    public BlockUserResponseDTO blockUser(Long userId, String adminEmail, BlockUserRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Administrator admin = adminRepo.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (user.isBlocked()) {
            throw new RuntimeException("User is already blocked");
        }

        user.setBlocked(true);
        userRepository.save(user);

        BlockNote note = new BlockNote();
        note.setUser(user);
        note.setAdmin(admin);
        note.setReason(dto.getReason());
        note.setBlockedAt(LocalDateTime.now());
        blockNoteRepository.save(note);

        return new BlockUserResponseDTO(
                user.getId(),
                user.getEmail(),
                true,
                dto.getReason(),
                note.getBlockedAt()
        );
    }

    @Override
    public BlockUserResponseDTO unblockUser(Long userId, String adminEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isBlocked()) {
            throw new RuntimeException("User is not blocked");
        }

        user.setBlocked(false);
        userRepository.save(user);

        BlockNote activeNote = blockNoteRepository.findByUserAndUnblockedAtIsNull(user)
                .orElse(null);

        if (activeNote != null) {
            activeNote.setUnblockedAt(LocalDateTime.now());
            blockNoteRepository.save(activeNote);
        }

        return new BlockUserResponseDTO(
                user.getId(),
                user.getEmail(),
                false,
                null,
                null
        );
    }

    @Override
    public Page<UserEmailDTO> getUnblockedUsers(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("email").ascending());
        Page<User> users = search.isEmpty()
                ? userRepository.findByIsBlocked(false, pageable)
                : userRepository.findByIsBlockedAndEmailContaining(false, search, pageable);
        return users.map(u -> new UserEmailDTO(u.getId(), u.getEmail(), u.getRole().toString()));
    }

    @Override
    public Page<UserEmailDTO> getBlockedUsers(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("email").ascending());
        Page<User> users = search.isEmpty()
                ? userRepository.findByIsBlocked(true, pageable)
                : userRepository.findByIsBlockedAndEmailContaining(true, search, pageable);
        return users.map(u -> new UserEmailDTO(u.getId(), u.getEmail(), u.getRole().toString()));
    }

    @Override
    public void getReports() {
        // TODO
    }
}
