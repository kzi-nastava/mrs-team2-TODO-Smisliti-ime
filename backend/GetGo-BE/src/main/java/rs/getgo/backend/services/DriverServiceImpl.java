package rs.getgo.backend.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.getgo.backend.dtos.authentication.UpdatePasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatedPasswordDTO;
import rs.getgo.backend.dtos.driver.GetDriverDTO;
import rs.getgo.backend.dtos.driver.UpdateDriverPersonalDTO;
import rs.getgo.backend.dtos.driver.UpdateDriverVehicleDTO;
import rs.getgo.backend.dtos.request.CreatedDriverChangeRequestDTO;
import rs.getgo.backend.model.entities.AvatarChangeRequest;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.entities.PersonalChangeRequest;
import rs.getgo.backend.model.entities.VehicleChangeRequest;
import rs.getgo.backend.model.enums.RequestStatus;
import rs.getgo.backend.repositories.AvatarChangeRequestRepository;
import rs.getgo.backend.repositories.DriverRepository;
import rs.getgo.backend.repositories.PersonalChangeRequestRepository;
import rs.getgo.backend.repositories.VehicleChangeRequestRepository;

import java.time.LocalDateTime;

@Service
public class DriverServiceImpl {

    @Autowired
    private DriverRepository driverRepo;
    @Autowired
    private PersonalChangeRequestRepository personalChangeRequestRepo;
    @Autowired
    private VehicleChangeRequestRepository vehicleChangeRequestRepo;
    @Autowired
    private AvatarChangeRequestRepository avatarChangeRequestRepo;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private FileStorageService fileStorageService;

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

        if (!driver.getPassword().equals(updatePasswordDTO.getOldPassword())) {
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