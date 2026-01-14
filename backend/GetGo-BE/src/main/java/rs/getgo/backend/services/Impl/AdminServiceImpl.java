package rs.getgo.backend.services.Impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.getgo.backend.dtos.admin.GetAdminDTO;
import rs.getgo.backend.dtos.admin.UpdateAdminDTO;
import rs.getgo.backend.dtos.admin.UpdatedAdminDTO;
import rs.getgo.backend.dtos.authentication.UpdatePasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatedPasswordDTO;
import rs.getgo.backend.dtos.driver.CreateDriverDTO;
import rs.getgo.backend.dtos.driver.CreatedDriverDTO;
import rs.getgo.backend.dtos.request.*;
import rs.getgo.backend.model.entities.*;
import rs.getgo.backend.model.enums.RequestStatus;
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

    @Autowired
    private AdministratorRepository adminRepo;
    @Autowired
    private DriverRepository driverRepo;
    @Autowired
    private PersonalChangeRequestRepository personalChangeRequestRepo;
    @Autowired
    private VehicleChangeRequestRepository vehicleChangeRequestRepo;
    @Autowired
    private AvatarChangeRequestRepository avatarChangeRequestRepo;
    @Autowired
    private DriverActivationTokenRepository driverActivationTokenRepo;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CreatedDriverDTO registerDriver(CreateDriverDTO createDriverDTO) {
        Vehicle vehicle = fillVehicle(createDriverDTO);
        Driver driver = fillDriver(createDriverDTO, vehicle);
        Driver savedDriver = driverRepo.save(driver); // Should save both vehicle and driver due to CascadeType.ALL

        DriverActivationToken token = createDriverActivationToken(savedDriver);
        driverActivationTokenRepo.save(token);

        emailService.sendActivationEmail(savedDriver.getEmail(), token.getToken());

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
        // changed to firstName/lastName/phoneNumber
        driver.setFirstName(createDriverDTO.getName());
        driver.setLastName(createDriverDTO.getSurname());
        driver.setPhoneNumber(createDriverDTO.getPhone());
        driver.setAddress(createDriverDTO.getAddress());
        driver.setProfilePictureUrl(fileStorageService.getDefaultProfilePicture());
        driver.setActive(false);
        driver.setActivated(false);
        driver.setBlocked(false);
        driver.setVehicle(vehicle);
        // Password is set later
        return driver;
    }

    @Override
    public GetAdminDTO getAdminById(Long adminId) {
        Administrator admin = adminRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));

        return modelMapper.map(admin, GetAdminDTO.class);
    }

    @Override
    public UpdatedAdminDTO updateProfile(Long adminId, UpdateAdminDTO updateAdminDTO) {
        Administrator admin = adminRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));

        if (updateAdminDTO.getName() != null && !updateAdminDTO.getName().trim().isEmpty()) {
            admin.setFirstName(updateAdminDTO.getName().trim());
        }
        if (updateAdminDTO.getSurname() != null && !updateAdminDTO.getSurname().trim().isEmpty()) {
            admin.setLastName(updateAdminDTO.getSurname().trim());
        }
        if (updateAdminDTO.getPhone() != null && !updateAdminDTO.getPhone().trim().isEmpty()) {
            admin.setPhoneNumber(updateAdminDTO.getPhone().trim());
        }
        if (updateAdminDTO.getAddress() != null && !updateAdminDTO.getAddress().trim().isEmpty()) {
            admin.setAddress(updateAdminDTO.getAddress().trim());
        }

        Administrator savedAdmin = adminRepo.save(admin);
        return modelMapper.map(savedAdmin, UpdatedAdminDTO.class);
    }

    @Override
    public UpdatedPasswordDTO updatePassword(Long adminId, UpdatePasswordDTO updatePasswordDTO) {
        if (!updatePasswordDTO.getPassword().equals(updatePasswordDTO.getConfirmPassword())) {
            return new UpdatedPasswordDTO(false, "Passwords do not match");
        }

        Administrator admin = adminRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));

        if (!admin.getPassword().equals(updatePasswordDTO.getOldPassword())) {
            return new UpdatedPasswordDTO(false, "Old password is incorrect");
        }

        admin.setPassword(updatePasswordDTO.getPassword());
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
            // use firstName/lastName
            dto.setDriverName(driver.getFirstName() + " " + driver.getLastName());

            // Current data
            dto.setCurrentName(driver.getFirstName());
            dto.setCurrentSurname(driver.getLastName());
            dto.setCurrentPhone(driver.getPhoneNumber());
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
            dto.setDriverName(driver.getFirstName() + " " + driver.getLastName());

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
            dto.setDriverName(driver.getFirstName() + " " + driver.getLastName());

            // Current profile picture
            dto.setCurrentProfilePictureUrl(fileStorageService.getFileUrl(driver.getProfilePictureUrl()));

            // Requested profile picture
            dto.setRequestedProfilePictureUrl(fileStorageService.getFileUrl(request.getRequestedProfilePictureUrl()));

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
        dto.setDriverName(driver.getFirstName() + " " + driver.getLastName());

        dto.setCurrentName(driver.getFirstName());
        dto.setCurrentSurname(driver.getLastName());
        dto.setCurrentPhone(driver.getPhoneNumber());
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
        dto.setDriverName(driver.getFirstName() + " " + driver.getLastName());

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
        dto.setDriverName(driver.getFirstName() + " " + driver.getLastName());

        dto.setCurrentProfilePictureUrl(fileStorageService.getFileUrl(driver.getProfilePictureUrl()));
        dto.setRequestedProfilePictureUrl(fileStorageService.getFileUrl(request.getRequestedProfilePictureUrl()));

        dto.setStatus(request.getStatus().toString());
        dto.setCreatedAt(request.getCreatedAt());

        return dto;
    }

    @Override
    public AcceptDriverChangeRequestDTO approvePersonalChangeRequest(Long requestId, Long adminId) {
        PersonalChangeRequest request = personalChangeRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Personal change request not found with id: " + requestId));

        Administrator admin = adminRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));

        Driver driver = request.getDriver();

        // Apply changes to driver using firstName/lastName/phoneNumber
        driver.setFirstName(request.getRequestedName());
        driver.setLastName(request.getRequestedSurname());
        driver.setPhoneNumber(request.getRequestedPhone());
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
                "APPROVED",
                adminId,
                LocalDateTime.now()
        );
    }

    @Override
    public AcceptDriverChangeRequestDTO approveVehicleChangeRequest(Long requestId, Long adminId) {
        VehicleChangeRequest request = vehicleChangeRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Vehicle change request not found with id: " + requestId));

        Administrator admin = adminRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));

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
                "APPROVED",
                adminId,
                LocalDateTime.now()
        );
    }

    @Override
    public AcceptDriverChangeRequestDTO approveAvatarChangeRequest(Long requestId, Long adminId) {
        AvatarChangeRequest request = avatarChangeRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Avatar change request not found with id: " + requestId));

        Administrator admin = adminRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));

        Driver driver = request.getDriver();

        // Delete old profile picture if not default
        if (driver.getProfilePictureUrl() != null) {
            fileStorageService.deleteFile(driver.getProfilePictureUrl());
        }

        // Apply new profile picture
        driver.setProfilePictureUrl(request.getRequestedProfilePictureUrl());
        driverRepo.save(driver);

        // Update request status
        request.setStatus(RequestStatus.APPROVED);
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        avatarChangeRequestRepo.save(request);

        return new AcceptDriverChangeRequestDTO(
                request.getId(),
                driver.getId(),
                "APPROVED",
                adminId,
                LocalDateTime.now()
        );
    }

    @Override
    public AcceptDriverChangeRequestDTO rejectPersonalChangeRequest(Long requestId, Long adminId, RejectDriverChangeRequestDTO rejectDTO) {
        PersonalChangeRequest request = personalChangeRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Personal change request not found with id: " + requestId));

        Administrator admin = adminRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));

        request.setStatus(RequestStatus.REJECTED);
        request.setRejectionReason(rejectDTO.getReason());
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        personalChangeRequestRepo.save(request);

        return new AcceptDriverChangeRequestDTO(
                request.getId(),
                request.getDriver().getId(),
                "REJECTED",
                adminId,
                LocalDateTime.now()
        );
    }

    @Override
    public AcceptDriverChangeRequestDTO rejectVehicleChangeRequest(Long requestId, Long adminId, RejectDriverChangeRequestDTO rejectDTO) {
        VehicleChangeRequest request = vehicleChangeRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Vehicle change request not found with id: " + requestId));

        Administrator admin = adminRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));

        request.setStatus(RequestStatus.REJECTED);
        request.setRejectionReason(rejectDTO.getReason());
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        vehicleChangeRequestRepo.save(request);

        return new AcceptDriverChangeRequestDTO(
                request.getId(),
                request.getDriver().getId(),
                "REJECTED",
                adminId,
                LocalDateTime.now()
        );
    }

    @Override
    public AcceptDriverChangeRequestDTO rejectAvatarChangeRequest(Long requestId, Long adminId, RejectDriverChangeRequestDTO rejectDTO) {
        AvatarChangeRequest request = avatarChangeRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Avatar change request not found with id: " + requestId));

        Administrator admin = adminRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));

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
                "REJECTED",
                adminId,
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

    @Override
    public void createAdmin() {
        // TODO
    }
}

