package rs.getgo.backend.services;

import org.springframework.web.multipart.MultipartFile;
import rs.getgo.backend.dtos.authentication.GetActivationTokenDTO;
import rs.getgo.backend.dtos.authentication.UpdateDriverPasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatePasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatedPasswordDTO;
import rs.getgo.backend.dtos.driver.GetDriverDTO;
import rs.getgo.backend.dtos.driver.UpdateDriverLocationDTO;
import rs.getgo.backend.dtos.driver.UpdateDriverPersonalDTO;
import rs.getgo.backend.dtos.driver.UpdateDriverVehicleDTO;
import rs.getgo.backend.dtos.request.CreatedDriverChangeRequestDTO;
import rs.getgo.backend.dtos.ride.GetRideDTO;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.Driver;

import java.time.LocalDate;
import java.util.List;

public interface DriverService {
    List<GetRideDTO> getDriverRides(Long driverId, LocalDate startDate);
    GetActivationTokenDTO validateActivationToken(String token);
    UpdatedPasswordDTO setDriverPassword(UpdateDriverPasswordDTO passwordDTO);
    GetDriverDTO getDriverById(Long driverId);
    UpdatedPasswordDTO updatePassword(Long driverId, UpdatePasswordDTO updatePasswordDTO);
    CreatedDriverChangeRequestDTO requestPersonalInfoChange(Long driverId, UpdateDriverPersonalDTO updateDTO);
    CreatedDriverChangeRequestDTO requestVehicleInfoChange(Long driverId, UpdateDriverVehicleDTO updateDTO);
    CreatedDriverChangeRequestDTO requestProfilePictureChange(Long driverId, MultipartFile file);
    void updateLocation(String driverEmail, Double latitude, Double longitude);
    UpdateDriverLocationDTO getLocation(String driverEmail);
    void updateActiveStatus(String driverEmail, boolean isActive);
    Driver findAvailableDriver(ActiveRide ride);
}
