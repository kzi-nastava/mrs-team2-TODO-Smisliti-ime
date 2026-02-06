package rs.getgo.backend.services;

import org.springframework.data.domain.Page;
import rs.getgo.backend.dtos.admin.*;
import rs.getgo.backend.dtos.authentication.UpdatePasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatedPasswordDTO;
import rs.getgo.backend.dtos.driver.CreateDriverDTO;
import rs.getgo.backend.dtos.driver.CreatedDriverDTO;
import rs.getgo.backend.dtos.request.*;
import rs.getgo.backend.dtos.ride.GetReorderRideDTO;
import rs.getgo.backend.dtos.ride.GetRideDTO;

import java.time.LocalDate;
import java.util.List;

public interface AdminService {

    CreatedDriverDTO registerDriver(CreateDriverDTO createDriverDTO);

    GetAdminDTO getAdmin(String email);

    UpdatedAdminDTO updateProfile(String email, UpdateAdminDTO updateAdminDTO);

    UpdatedPasswordDTO updatePassword(String email, UpdatePasswordDTO updatePasswordDTO);

    List<GetPersonalDriverChangeRequestDTO> getPendingPersonalChangeRequests();

    List<GetDriverVehicleChangeRequestDTO> getPendingVehicleChangeRequests();

    List<GetDriverAvatarChangeRequestDTO> getPendingAvatarChangeRequests();

    GetPersonalDriverChangeRequestDTO getPersonalChangeRequest(Long requestId);

    GetDriverVehicleChangeRequestDTO getVehicleChangeRequest(Long requestId);

    GetDriverAvatarChangeRequestDTO getAvatarChangeRequest(Long requestId);

    AcceptDriverChangeRequestDTO approvePersonalChangeRequest(Long requestId, String email);

    AcceptDriverChangeRequestDTO approveVehicleChangeRequest(Long requestId, String email);

    AcceptDriverChangeRequestDTO approveAvatarChangeRequest(Long requestId, String email);

    AcceptDriverChangeRequestDTO rejectPersonalChangeRequest(Long requestId, String email, RejectDriverChangeRequestDTO rejectDTO);

    AcceptDriverChangeRequestDTO rejectVehicleChangeRequest(Long requestId, String email, RejectDriverChangeRequestDTO rejectDTO);

    AcceptDriverChangeRequestDTO rejectAvatarChangeRequest(Long requestId, String email, RejectDriverChangeRequestDTO rejectDTO);

    void blockUser();

    void unblockUser();

    void getReports();

    CreatedAdminDTO createAdmin(CreateAdminDTO createAdminDTO);

    Page<GetRideDTO> getPassengerRides(String email, LocalDate startDate, int page, int size, String sortBy, String direction);

    GetReorderRideDTO getPassengerRideById(String email, Long rideId);

    Page<GetRideDTO> getDriverRides(String email, LocalDate startDate, int page, int size, String sortBy, String direction);

    GetReorderRideDTO getDriverRideById(String email, Long rideId);
}