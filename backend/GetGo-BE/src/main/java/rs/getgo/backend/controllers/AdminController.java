package rs.getgo.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import rs.getgo.backend.dtos.admin.GetAdminDTO;
import rs.getgo.backend.dtos.admin.UpdateAdminDTO;
import rs.getgo.backend.dtos.admin.UpdatedAdminDTO;
import rs.getgo.backend.dtos.authentication.UpdatePasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatedPasswordDTO;
import rs.getgo.backend.dtos.driver.*;
import rs.getgo.backend.dtos.report.GetReportDTO;
import rs.getgo.backend.dtos.request.*;
import rs.getgo.backend.dtos.user.CreatedUserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.getgo.backend.services.AdminService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // 2.9.3 – Block user
    @PutMapping("/users/{id}/block")
    public ResponseEntity<CreatedUserDTO> blockUser(@PathVariable Long id) {
        CreatedUserDTO response = new CreatedUserDTO(id, "blocked@getgo.com", "Blocked", "User", "+38178687868");
        return ResponseEntity.ok(response);
    }

    // 2.9.3 – Unblock user
    @PutMapping("/users/{id}/unblock")
    public ResponseEntity<CreatedUserDTO> unblockUser(@PathVariable Long id) {
        CreatedUserDTO response = new CreatedUserDTO(id, "unblocked@getgo.com", "Active", "User", "+38178687868");
        return ResponseEntity.ok(response);
    }

    // 2.9.3 – View reports
    @GetMapping("/reports")
    public ResponseEntity<List<GetReportDTO>> getReports(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        GetReportDTO report = new GetReportDTO("01-01-2025 / 31-01-2025", 120, 860.5, 750.0);
        return ResponseEntity.ok(List.of(report));
    }

    // 2.9.3 – Create admin profile
    @PostMapping("/create")
    public ResponseEntity<CreatedUserDTO> createAdmin() {
        CreatedUserDTO response = new CreatedUserDTO(10L, "admin@getgo.com", "Admin", "User", "+38178687868");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2.2.3 - Driver registration
    @PostMapping(value = "/drivers/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedDriverDTO> registerDriver(
            @RequestBody CreateDriverDTO request) {

        CreatedDriverDTO response = new CreatedDriverDTO();
        response.setId(1L);
        response.setEmail(request.getEmail());
        response.setActivated(false);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2.3 - User profile
    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetAdminDTO> getProfile() {
        Long adminId = 1L; // TODO: get from cookie/whatever we decide to use
        GetAdminDTO response = adminService.getAdminById(adminId);
        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile
    @PutMapping(value = "/profile",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedAdminDTO> updateProfile(
            @RequestBody UpdateAdminDTO updateAdminDTO) {
        Long adminId = 1L; // TODO: get from cookie/whatever we decide to use
        UpdatedAdminDTO response = adminService.updateProfile(adminId, updateAdminDTO);
        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile (Change admin password)
    @PutMapping(value = "/profile/password",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedPasswordDTO> updatePassword(
            @RequestBody UpdatePasswordDTO updatePasswordDTO) {
        Long adminId = 1L; // TODO: get from cookie/whatever we decide to use
        UpdatedPasswordDTO response = adminService.updatePassword(adminId, updatePasswordDTO);
        if (!response.getSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    // 2.3 - Get all pending personal change requests
    @GetMapping(value = "/driver-change-requests/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GetPersonalDriverChangeRequestDTO>> getPendingPersonalChangeRequests() {
        List<GetPersonalDriverChangeRequestDTO> response = adminService.getPendingPersonalChangeRequests();
        return ResponseEntity.ok(response);
    }

    // 2.3 - Get all pending vehicle change requests
    @GetMapping(value = "/driver-change-requests/vehicle", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GetDriverVehicleChangeRequestDTO>> getPendingVehicleChangeRequests() {
        List<GetDriverVehicleChangeRequestDTO> response = adminService.getPendingVehicleChangeRequests();
        return ResponseEntity.ok(response);
    }

    // 2.3 - Get all pending picture change requests
    @GetMapping(value = "/driver-change-requests/picture", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GetDriverAvatarChangeRequestDTO>> getPendingPictureChangeRequests() {
        List<GetDriverAvatarChangeRequestDTO> response = adminService.getPendingAvatarChangeRequests();
        return ResponseEntity.ok(response);
    }

    // 2.3 - Get specific personal change request
    @GetMapping(value = "/driver-change-requests/personal/{requestId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetPersonalDriverChangeRequestDTO> getPersonalChangeRequest(
            @PathVariable Long requestId) {
        GetPersonalDriverChangeRequestDTO response = adminService.getPersonalChangeRequest(requestId);
        return ResponseEntity.ok(response);
    }

    // 2.3 - Get specific vehicle change request
    @GetMapping(value = "/driver-change-requests/vehicle/{requestId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetDriverVehicleChangeRequestDTO> getVehicleChangeRequest(
            @PathVariable Long requestId) {
        GetDriverVehicleChangeRequestDTO response = adminService.getVehicleChangeRequest(requestId);
        return ResponseEntity.ok(response);
    }

    // 2.3 - Get specific picture change request
    @GetMapping(value = "/driver-change-requests/picture/{requestId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetDriverAvatarChangeRequestDTO> getPictureChangeRequest(
            @PathVariable Long requestId) {
        GetDriverAvatarChangeRequestDTO response = adminService.getAvatarChangeRequest(requestId);
        return ResponseEntity.ok(response);
    }

    // 2.3 - Approve personal change request
    @PutMapping(value = "/driver-change-requests/personal/{requestId}/approve", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AcceptDriverChangeRequestDTO> approvePersonalChangeRequest(
            @PathVariable Long requestId) {
        Long adminId = 1L; // TODO: get from cookie/whatever we decide to use
        AcceptDriverChangeRequestDTO response = adminService.approvePersonalChangeRequest(requestId, adminId);
        return ResponseEntity.ok(response);
    }

    // 2.3 - Approve vehicle change request
    @PutMapping(value = "/driver-change-requests/vehicle/{requestId}/approve", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AcceptDriverChangeRequestDTO> approveVehicleChangeRequest(
            @PathVariable Long requestId) {
        Long adminId = 1L; // TODO: get from cookie/whatever we decide to use
        AcceptDriverChangeRequestDTO response = adminService.approveVehicleChangeRequest(requestId, adminId);
        return ResponseEntity.ok(response);
    }

    // 2.3 - Approve picture change request
    @PutMapping(value = "/driver-change-requests/picture/{requestId}/approve", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AcceptDriverChangeRequestDTO> approvePictureChangeRequest(
            @PathVariable Long requestId) {
        Long adminId = 1L; // TODO: get from cookie/whatever we decide to use
        AcceptDriverChangeRequestDTO response = adminService.approveAvatarChangeRequest(requestId, adminId);
        return ResponseEntity.ok(response);
    }

    // 2.3 - Reject personal change request
    @PutMapping(value = "/driver-change-requests/personal/{requestId}/reject",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AcceptDriverChangeRequestDTO> rejectPersonalChangeRequest(
            @PathVariable Long requestId,
            @RequestBody RejectDriverChangeRequestDTO rejectDriverChangeRequestDTO) {
        Long adminId = 1L; // TODO: get from cookie/whatever we decide to use
        AcceptDriverChangeRequestDTO response = adminService.rejectPersonalChangeRequest(requestId, adminId, rejectDriverChangeRequestDTO);
        return ResponseEntity.ok(response);
    }

    // 2.3 - Reject vehicle change request
    @PutMapping(value = "/driver-change-requests/vehicle/{requestId}/reject",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AcceptDriverChangeRequestDTO> rejectVehicleChangeRequest(
            @PathVariable Long requestId,
            @RequestBody RejectDriverChangeRequestDTO rejectDriverChangeRequestDTO) {
        Long adminId = 1L; // TODO: get from cookie/whatever we decide to use
        AcceptDriverChangeRequestDTO response = adminService.rejectVehicleChangeRequest(requestId, adminId, rejectDriverChangeRequestDTO);
        return ResponseEntity.ok(response);
    }

    // 2.3 - Reject picture change request
    @PutMapping(value = "/driver-change-requests/picture/{requestId}/reject",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AcceptDriverChangeRequestDTO> rejectPictureChangeRequest(
            @PathVariable Long requestId,
            @RequestBody RejectDriverChangeRequestDTO rejectDriverChangeRequestDTO) {
        Long adminId = 1L; // TODO: get from cookie/whatever we decide to use
        AcceptDriverChangeRequestDTO response = adminService.rejectAvatarChangeRequest(requestId, adminId, rejectDriverChangeRequestDTO);
        return ResponseEntity.ok(response);
    }

}