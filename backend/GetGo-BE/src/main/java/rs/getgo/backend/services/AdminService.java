package rs.getgo.backend.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.getgo.backend.dtos.admin.GetAdminDTO;
import rs.getgo.backend.dtos.admin.UpdateAdminDTO;
import rs.getgo.backend.dtos.admin.UpdatedAdminDTO;
import rs.getgo.backend.dtos.authentication.UpdatePasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatedPasswordDTO;
import rs.getgo.backend.model.entities.Administrator;
import rs.getgo.backend.repositories.AdministratorRepository;

@Service
public class AdminService {

    @Autowired
    private AdministratorRepository adminRepo;

    @Autowired
    private ModelMapper modelMapper;

    public GetAdminDTO getAdminById(Long adminId) {
        Administrator admin = adminRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));

        return modelMapper.map(admin, GetAdminDTO.class);
    }

    public UpdatedAdminDTO updateAdmin(Long adminId, UpdateAdminDTO updateAdminDTO) {
        Administrator admin = adminRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));

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

    public UpdatedPasswordDTO updatePassword(Long adminId, UpdatePasswordDTO updatePasswordDTO) {
        if (!updatePasswordDTO.getPassword().equals(updatePasswordDTO.getConfirmPassword())) {
            return new UpdatedPasswordDTO(false, "Passwords do not match");
        }

        if (updatePasswordDTO.getPassword().length() < 8) {
            return new UpdatedPasswordDTO(false, "Password must be at least 8 characters long");
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

    public void blockUser() {
        // TODO
    }

    public void unblockUser() {
        // TODO
    }

    public void getReports() {
        // TODO
    }

    public void createAdmin() {
        // TODO
    }
}