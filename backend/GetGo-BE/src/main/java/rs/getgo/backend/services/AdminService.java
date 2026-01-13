package rs.getgo.backend.services;

import org.springframework.stereotype.Service;
import rs.getgo.backend.dtos.admin.GetAdminDTO;
import rs.getgo.backend.dtos.admin.UpdateAdminDTO;
import rs.getgo.backend.dtos.admin.UpdatedAdminDTO;
import rs.getgo.backend.dtos.authentication.UpdatePasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatedPasswordDTO;
<<<<<<< Updated upstream
=======
import rs.getgo.backend.dtos.driver.CreateDriverDTO;
import rs.getgo.backend.dtos.driver.CreatedDriverDTO;
import java.util.List;

import rs.getgo.backend.dtos.request.GetPersonalDriverChangeRequestDTO;
import rs.getgo.backend.dtos.request.GetDriverVehicleChangeRequestDTO;
import rs.getgo.backend.dtos.request.GetDriverAvatarChangeRequestDTO;
import rs.getgo.backend.dtos.request.AcceptDriverChangeRequestDTO;
import rs.getgo.backend.dtos.request.RejectDriverChangeRequestDTO;
>>>>>>> Stashed changes

@Service
public interface AdminService {

    public GetAdminDTO getAdminById(Long adminId);

    public UpdatedAdminDTO updateProfile(Long adminId, UpdateAdminDTO updateAdminDTO);

    public UpdatedPasswordDTO updatePassword(Long adminId, UpdatePasswordDTO updatePasswordDTO);

<<<<<<< Updated upstream
    public default void blockUser() {
        // TODO
    }

    public default void unblockUser() {
        // TODO
    }

    public default void getReports() {
        // TODO
    }

    public default void createAdmin() {
        // TODO
    }
=======
    // Driver registration
    public CreatedDriverDTO registerDriver(CreateDriverDTO createDriverDTO);

    // Change request listing
    public List<GetPersonalDriverChangeRequestDTO> getPendingPersonalChangeRequests();
    public List<GetDriverVehicleChangeRequestDTO> getPendingVehicleChangeRequests();
    public List<GetDriverAvatarChangeRequestDTO> getPendingAvatarChangeRequests();

    // Single change request retrieval
    public GetPersonalDriverChangeRequestDTO getPersonalChangeRequest(Long requestId);
    public GetDriverVehicleChangeRequestDTO getVehicleChangeRequest(Long requestId);
    public GetDriverAvatarChangeRequestDTO getAvatarChangeRequest(Long requestId);

    // Approve/reject change requests
    public AcceptDriverChangeRequestDTO approvePersonalChangeRequest(Long requestId, Long adminId);
    public AcceptDriverChangeRequestDTO approveVehicleChangeRequest(Long requestId, Long adminId);
    public AcceptDriverChangeRequestDTO approveAvatarChangeRequest(Long requestId, Long adminId);

    public AcceptDriverChangeRequestDTO rejectPersonalChangeRequest(Long requestId, Long adminId, RejectDriverChangeRequestDTO rejectDTO);
    public AcceptDriverChangeRequestDTO rejectVehicleChangeRequest(Long requestId, Long adminId, RejectDriverChangeRequestDTO rejectDTO);
    public AcceptDriverChangeRequestDTO rejectAvatarChangeRequest(Long requestId, Long adminId, RejectDriverChangeRequestDTO rejectDTO);

    // Administrative actions
    public void blockUser();

    public void unblockUser();

    public void getReports();

    public void createAdmin();

>>>>>>> Stashed changes
}