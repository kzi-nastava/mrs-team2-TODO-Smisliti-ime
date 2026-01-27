package rs.getgo.backend.services;

import org.springframework.web.multipart.MultipartFile;
import rs.getgo.backend.dtos.authentication.UpdatePasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatedPasswordDTO;
import rs.getgo.backend.dtos.passenger.GetPassengerDTO;
import rs.getgo.backend.dtos.passenger.UpdatePassengerDTO;
import rs.getgo.backend.dtos.passenger.UpdatedPassengerDTO;
import rs.getgo.backend.dtos.ride.GetRideDTO;
import rs.getgo.backend.dtos.user.UpdatedProfilePictureDTO;

import java.time.LocalDate;
import java.util.List;

public interface PassengerService {
    GetPassengerDTO getPassenger(String email);
    UpdatedPassengerDTO updateProfile(String email, UpdatePassengerDTO dto);
    UpdatedPasswordDTO updatePassword(String email, UpdatePasswordDTO dto);
    UpdatedProfilePictureDTO uploadProfilePicture(String email, MultipartFile file);

    List<GetRideDTO> getPassengerRides(String email, LocalDate startDate);

    GetRideDTO getPassengerRideById(String email, Long rideId);
}
