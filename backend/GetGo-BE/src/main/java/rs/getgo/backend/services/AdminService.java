package rs.getgo.backend.services;

import rs.getgo.backend.dtos.admin.GetAdminDTO;
import rs.getgo.backend.dtos.admin.UpdateAdminDTO;
import rs.getgo.backend.dtos.admin.UpdatedAdminDTO;
import rs.getgo.backend.dtos.authentication.UpdatePasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatedPasswordDTO;
import rs.getgo.backend.dtos.driver.CreateDriverDTO;
import rs.getgo.backend.dtos.driver.CreatedDriverDTO;
import rs.getgo.backend.dtos.request.*;

import java.util.List;

// Auth/Admin service reduced to interface with signatures only.
public interface AdminService {

    CreatedDriverDTO registerDriver(CreateDriverDTO createDriverDTO);

    GetAdminDTO getAdminById(Long adminId);

    UpdatedAdminDTO updateProfile(Long adminId, UpdateAdminDTO updateAdminDTO);

    UpdatedPasswordDTO updatePassword(Long adminId, UpdatePasswordDTO updatePasswordDTO);

    List<GetPersonalDriverChangeRequestDTO> getPendingPersonalChangeRequests();

    List<GetDriverVehicleChangeRequestDTO> getPendingVehicleChangeRequests();

    List<GetDriverAvatarChangeRequestDTO> getPendingAvatarChangeRequests();

    GetPersonalDriverChangeRequestDTO getPersonalChangeRequest(Long requestId);

    GetDriverVehicleChangeRequestDTO getVehicleChangeRequest(Long requestId);

    GetDriverAvatarChangeRequestDTO getAvatarChangeRequest(Long requestId);

    AcceptDriverChangeRequestDTO approvePersonalChangeRequest(Long requestId, Long adminId);

    AcceptDriverChangeRequestDTO approveVehicleChangeRequest(Long requestId, Long adminId);

    AcceptDriverChangeRequestDTO approveAvatarChangeRequest(Long requestId, Long adminId);

    AcceptDriverChangeRequestDTO rejectPersonalChangeRequest(Long requestId, Long adminId, RejectDriverChangeRequestDTO rejectDTO);

    AcceptDriverChangeRequestDTO rejectVehicleChangeRequest(Long requestId, Long adminId, RejectDriverChangeRequestDTO rejectDTO);

    AcceptDriverChangeRequestDTO rejectAvatarChangeRequest(Long requestId, Long adminId, RejectDriverChangeRequestDTO rejectDTO);

    void blockUser();

    void unblockUser();

    void getReports();

    void createAdmin();
}