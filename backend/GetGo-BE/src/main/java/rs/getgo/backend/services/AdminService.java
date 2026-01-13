package rs.getgo.backend.services;

import org.springframework.stereotype.Service;
import rs.getgo.backend.dtos.admin.GetAdminDTO;
import rs.getgo.backend.dtos.admin.UpdateAdminDTO;
import rs.getgo.backend.dtos.admin.UpdatedAdminDTO;
import rs.getgo.backend.dtos.authentication.UpdatePasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatedPasswordDTO;

@Service
public interface AdminService {

    public GetAdminDTO getAdminById(Long adminId);

    public UpdatedAdminDTO updateProfile(Long adminId, UpdateAdminDTO updateAdminDTO);

    public UpdatedPasswordDTO updatePassword(Long adminId, UpdatePasswordDTO updatePasswordDTO);

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
}