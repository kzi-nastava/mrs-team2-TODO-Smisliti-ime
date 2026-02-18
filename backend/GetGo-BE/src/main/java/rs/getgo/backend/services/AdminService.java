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
import rs.getgo.backend.dtos.user.BlockUserRequestDTO;
import rs.getgo.backend.dtos.user.BlockUserResponseDTO;
import rs.getgo.backend.dtos.user.UserEmailDTO;

import java.time.LocalDate;
import java.util.List;

public interface AdminService {

    CreatedDriverDTO registerDriver(CreateDriverDTO createDriverDTO);

    GetAdminDTO getAdmin(String email);

    UpdatedAdminDTO updateProfile(String email, UpdateAdminDTO updateAdminDTO);

    UpdatedPasswordDTO updatePassword(String email, UpdatePasswordDTO updatePasswordDTO);

    Page<GetPersonalDriverChangeRequestDTO> getPendingPersonalChangeRequests(int page, int size);

    Page<GetDriverVehicleChangeRequestDTO> getPendingVehicleChangeRequests(int page, int size);

    Page<GetDriverAvatarChangeRequestDTO> getPendingAvatarChangeRequests(int page, int size);

    GetPersonalDriverChangeRequestDTO getPersonalChangeRequest(Long requestId);

    GetDriverVehicleChangeRequestDTO getVehicleChangeRequest(Long requestId);

    GetDriverAvatarChangeRequestDTO getAvatarChangeRequest(Long requestId);

    AcceptDriverChangeRequestDTO approvePersonalChangeRequest(Long requestId, String email);

    AcceptDriverChangeRequestDTO approveVehicleChangeRequest(Long requestId, String email);

    AcceptDriverChangeRequestDTO approveAvatarChangeRequest(Long requestId, String email);

    AcceptDriverChangeRequestDTO rejectPersonalChangeRequest(Long requestId, String email, RejectDriverChangeRequestDTO rejectDTO);

    AcceptDriverChangeRequestDTO rejectVehicleChangeRequest(Long requestId, String email, RejectDriverChangeRequestDTO rejectDTO);

    AcceptDriverChangeRequestDTO rejectAvatarChangeRequest(Long requestId, String email, RejectDriverChangeRequestDTO rejectDTO);

    CreatedAdminDTO createAdmin(CreateAdminDTO createAdminDTO);

    Page<GetRideDTO> getPassengerRides(String email, LocalDate startDate, int page, int size, String sortBy, String direction);

    GetReorderRideDTO getPassengerRideById(String email, Long rideId);

    Page<GetRideDTO> getDriverRides(String email, LocalDate startDate, int page, int size, String sortBy, String direction);

    GetReorderRideDTO getDriverRideById(String email, Long rideId);

    BlockUserResponseDTO blockUser(Long userId, String adminEmail, BlockUserRequestDTO dto);

    BlockUserResponseDTO unblockUser(Long userId, String adminEmail);

    Page<UserEmailDTO> getUnblockedUsers(String search, int page, int size);

    Page<UserEmailDTO> getBlockedUsers(String search, int page, int size);

    void getReports();
}