package rs.getgo.backend.services.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import rs.getgo.backend.dtos.admin.*;
import rs.getgo.backend.dtos.authentication.UpdatePasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatedPasswordDTO;
import rs.getgo.backend.dtos.driver.CreateDriverDTO;
import rs.getgo.backend.dtos.driver.CreatedDriverDTO;
import rs.getgo.backend.dtos.request.*;
import rs.getgo.backend.model.entities.*;
import rs.getgo.backend.model.enums.RequestStatus;
import rs.getgo.backend.model.enums.UserRole;
import rs.getgo.backend.model.enums.VehicleType;
import rs.getgo.backend.repositories.*;
import rs.getgo.backend.services.AdminService;
import rs.getgo.backend.services.EmailService;
import rs.getgo.backend.services.FileStorageService;

import java.time.LocalDateTime;
import java.util.List;
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
    private final FileStorageService fileStorageService;
    private final EmailService emailService;
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminServiceImpl(
            AdministratorRepository adminRepo,
            DriverRepository driverRepo,
            PersonalChangeRequestRepository personalChangeRequestRepo,
            VehicleChangeRequestRepository vehicleChangeRequestRepo,
            AvatarChangeRequestRepository avatarChangeRequestRepo,
            DriverActivationTokenRepository driverActivationTokenRepo,
            FileStorageService fileStorageService,
            EmailService emailService,
            ModelMapper modelMapper,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.adminRepo = adminRepo;
        this.driverRepo = driverRepo;
        this.personalChangeRequestRepo = personalChangeRequestRepo;
        this.vehicleChangeRequestRepo = vehicleChangeRequestRepo;
        this.avatarChangeRequestRepo = avatarChangeRequestRepo;
        this.driverActivationTokenRepo = driverActivationTokenRepo;
        this.fileStorageService = fileStorageService;
        this.emailService = emailService;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public CreatedAdminDTO createAdmin(CreateAdminDTO createAdminDTO) {
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
        Vehicle vehicle = fillVehicle(createDriverDTO);
        Driver driver = fillDriver(createDriverDTO, vehicle);

        // Set base location and update so it's never null
        driver.setCurrentLatitude(45.240806);  // 45°14'26.9"N
        driver.setCurrentLongitude(19.828611); // 19°49'43.0"E
        driver.setLastLocationUpdate(LocalDateTime.now());

        Driver savedDriver = driverRepo.save(driver); // Should save both vehicle and driver due to CascadeType.ALL

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

        if (updateAdminDTO.getName() != null && !updateAdminDTO.getName().trim().isEmpty()) {
            admin.setName(updateAdminDTO.getName().trim());
        }
        if (updateAdminDTO.getSurname() != null && !updateAdminDTO.getSurname().trim().isEmpty()) {
            admin.setSurname(updateAdminDTO.getSurname().trim());
        }
        if (updateAdminDTO.getPhone() != null && !updateAdminDTO.getPhone().trim().isEmpty()) {
            admin.setPhone(updateAdminDTO.getPhone().trim());
        }
        if (updateAdminDTO.getAddress() != null && !updateAdminDTO.getAddress().trim().isEmpty()) {
            admin.setAddress(updateAdminDTO.getAddress().trim());
        }

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
    public List<GetPersonalDriverChangeRequestDTO> getPendingPersonalChangeRequests() {
        List<PersonalChangeRequest> requests = personalChangeRequestRepo.findByStatus(RequestStatus.PENDING);

        return requests.stream().map(request -> {
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
        }).collect(Collectors.toList());
    }

    @Override
    public List<GetDriverVehicleChangeRequestDTO> getPendingVehicleChangeRequests() {
        List<VehicleChangeRequest> requests = vehicleChangeRequestRepo.findByStatus(RequestStatus.PENDING);

        return requests.stream().map(request -> {
            Driver driver = request.getDriver();
            Vehicle vehicle = driver.getVehicle();

            GetDriverVehicleChangeRequestDTO dto = new GetDriverVehicleChangeRequestDTO();
            dto.setRequestId(request.getId());
            dto.setDriverId(driver.getId());
            dto.setDriverEmail(driver.getEmail());
            dto.setDriverName(driver.getName() + " " + driver.getSurname());

            // Current vehicle data
            if (vehicle != null) {
                dto.setCurrentVehicleModel(vehicle.getModel());
                dto.setCurrentVehicleType(vehicle.getType().toString());
                dto.setCurrentVehicleLicensePlate(vehicle.getLicensePlate());
                dto.setCurrentVehicleSeats(vehicle.getNumberOfSeats());
                dto.setCurrentVehicleHasBabySeats(vehicle.getIsBabyFriendly());
                dto.setCurrentVehicleAllowsPets(vehicle.getIsPetFriendly());
            }

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
        }).collect(Collectors.toList());
    }

    @Override
    public List<GetDriverAvatarChangeRequestDTO> getPendingAvatarChangeRequests() {
        List<AvatarChangeRequest> requests = avatarChangeRequestRepo.findByStatus(RequestStatus.PENDING);

        return requests.stream().map(request -> {
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
        }).collect(Collectors.toList());
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

        if (vehicle != null) {
            dto.setCurrentVehicleModel(vehicle.getModel());
            dto.setCurrentVehicleType(vehicle.getType().toString());
            dto.setCurrentVehicleLicensePlate(vehicle.getLicensePlate());
            dto.setCurrentVehicleSeats(vehicle.getNumberOfSeats());
            dto.setCurrentVehicleHasBabySeats(vehicle.getIsBabyFriendly());
            dto.setCurrentVehicleAllowsPets(vehicle.getIsPetFriendly());
        }

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
    public void blockUser() {
        // TODO
    }

    @Override
    public void unblockUser() {
        // TODO
    }

    @Override
    public void getReports() {
        // TODO
    }
}

