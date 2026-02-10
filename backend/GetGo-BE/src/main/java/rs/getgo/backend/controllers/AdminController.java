package rs.getgo.backend.controllers;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import rs.getgo.backend.dtos.admin.*;
import rs.getgo.backend.dtos.authentication.UpdatePasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatedPasswordDTO;
import rs.getgo.backend.dtos.driver.*;
import rs.getgo.backend.dtos.report.GetReportDTO;
import rs.getgo.backend.dtos.request.*;
import rs.getgo.backend.dtos.ride.GetReorderRideDTO;
import rs.getgo.backend.dtos.ride.GetRideDTO;
import rs.getgo.backend.dtos.user.BlockUserRequestDTO;
import rs.getgo.backend.dtos.user.BlockUserResponseDTO;
import rs.getgo.backend.dtos.user.CreatedUserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.getgo.backend.services.AdminService;
import rs.getgo.backend.utils.AuthUtils;

import java.time.LocalDate;
import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // 2.9.3 – Block user
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{id}/block")
    public ResponseEntity<BlockUserResponseDTO> blockUser(
            @PathVariable Long id,
            @RequestBody BlockUserRequestDTO blockUserRequestDTO) {
        String email = AuthUtils.getCurrentUserEmail();
        BlockUserResponseDTO response = adminService.blockUser(id, email, blockUserRequestDTO);
        return ResponseEntity.ok(response);
    }

    // 2.9.3 – Unblock user
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{id}/unblock")
    public ResponseEntity<BlockUserResponseDTO> unblockUser(@PathVariable Long id) {
        String email = AuthUtils.getCurrentUserEmail();
        BlockUserResponseDTO response = adminService.unblockUser(id, email);
        return ResponseEntity.ok(response);
    }

    // 2.9.3 – View reports
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reports")
    public ResponseEntity<List<GetReportDTO>> getReports(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        GetReportDTO report = new GetReportDTO("01-01-2025 / 31-01-2025", 120, 860.5, 750.0);
        return ResponseEntity.ok(List.of(report));
    }

    // 2.9.3 – Create admin profile
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<CreatedAdminDTO> createAdmin(@Valid @RequestBody CreateAdminDTO createAdminDTO) {

        CreatedAdminDTO response = adminService.createAdmin(createAdminDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2.2.3 - Driver registration
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/drivers/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedDriverDTO> registerDriver(
            @Valid @RequestBody CreateDriverDTO createDriverDTO) {

        CreatedDriverDTO response = adminService.registerDriver(createDriverDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2.3 - User profile
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetAdminDTO> getProfile() {
        String email = AuthUtils.getCurrentUserEmail();
        GetAdminDTO response = adminService.getAdmin(email);
        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/profile",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedAdminDTO> updateProfile(
            @Valid @RequestBody UpdateAdminDTO updateAdminDTO) {
        String email = AuthUtils.getCurrentUserEmail();
        UpdatedAdminDTO response = adminService.updateProfile(email, updateAdminDTO);
        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile (Change admin password)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/profile/password",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedPasswordDTO> updatePassword(
            @Valid @RequestBody UpdatePasswordDTO updatePasswordDTO) {
        String email = AuthUtils.getCurrentUserEmail();
        UpdatedPasswordDTO response = adminService.updatePassword(email, updatePasswordDTO);
        if (!response.getSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }



    // 2.3 - Get all pending personal change requests
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/driver-change-requests/personal")
    public ResponseEntity<Page<GetPersonalDriverChangeRequestDTO>> getPendingPersonalChangeRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<GetPersonalDriverChangeRequestDTO> response = adminService.getPendingPersonalChangeRequests(page, size);
        return ResponseEntity.ok(response);
    }

    // 2.3 - Get all pending vehicle change requests
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/driver-change-requests/vehicle",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<GetDriverVehicleChangeRequestDTO>> getPendingVehicleChangeRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<GetDriverVehicleChangeRequestDTO> response = adminService.getPendingVehicleChangeRequests(page, size);
        return ResponseEntity.ok(response);
    }

    // 2.3 - Get all pending picture change requests
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/driver-change-requests/picture",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<GetDriverAvatarChangeRequestDTO>> getPendingPictureChangeRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<GetDriverAvatarChangeRequestDTO> response = adminService.getPendingAvatarChangeRequests(page, size);
        return ResponseEntity.ok(response);
    }

    // 2.3 - Get specific personal change request
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/driver-change-requests/personal/{requestId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetPersonalDriverChangeRequestDTO> getPersonalChangeRequest(
            @PathVariable Long requestId) {
        GetPersonalDriverChangeRequestDTO response = adminService.getPersonalChangeRequest(requestId);
        return ResponseEntity.ok(response);
    }

    // 2.3 - Get specific vehicle change request
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/driver-change-requests/vehicle/{requestId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetDriverVehicleChangeRequestDTO> getVehicleChangeRequest(
            @PathVariable Long requestId) {
        GetDriverVehicleChangeRequestDTO response = adminService.getVehicleChangeRequest(requestId);
        return ResponseEntity.ok(response);
    }

    // 2.3 - Get specific picture change request
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/driver-change-requests/picture/{requestId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetDriverAvatarChangeRequestDTO> getPictureChangeRequest(
            @PathVariable Long requestId) {
        GetDriverAvatarChangeRequestDTO response = adminService.getAvatarChangeRequest(requestId);
        return ResponseEntity.ok(response);
    }

    // 2.3 - Approve personal change request
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/driver-change-requests/personal/{requestId}/approve",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AcceptDriverChangeRequestDTO> approvePersonalChangeRequest(
            @PathVariable Long requestId) {
        String email = AuthUtils.getCurrentUserEmail();
        AcceptDriverChangeRequestDTO response = adminService.approvePersonalChangeRequest(requestId, email);
        return ResponseEntity.ok(response);
    }

    // 2.3 - Approve vehicle change request
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/driver-change-requests/vehicle/{requestId}/approve",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AcceptDriverChangeRequestDTO> approveVehicleChangeRequest(
            @PathVariable Long requestId) {
        String email = AuthUtils.getCurrentUserEmail();
        AcceptDriverChangeRequestDTO response = adminService.approveVehicleChangeRequest(requestId, email);
        return ResponseEntity.ok(response);
    }

    // 2.3 - Approve picture change request
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/driver-change-requests/picture/{requestId}/approve",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AcceptDriverChangeRequestDTO> approvePictureChangeRequest(
            @PathVariable Long requestId) {
        String email = AuthUtils.getCurrentUserEmail();
        AcceptDriverChangeRequestDTO response = adminService.approveAvatarChangeRequest(requestId, email);
        return ResponseEntity.ok(response);
    }

    // 2.3 - Reject personal change request
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/driver-change-requests/personal/{requestId}/reject",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AcceptDriverChangeRequestDTO> rejectPersonalChangeRequest(
            @PathVariable Long requestId,
            @RequestBody RejectDriverChangeRequestDTO rejectDriverChangeRequestDTO) {
        String email = AuthUtils.getCurrentUserEmail();
        AcceptDriverChangeRequestDTO response = adminService.rejectPersonalChangeRequest(
                requestId, email, rejectDriverChangeRequestDTO);
        return ResponseEntity.ok(response);
    }

    // 2.3 - Reject vehicle change request
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/driver-change-requests/vehicle/{requestId}/reject",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AcceptDriverChangeRequestDTO> rejectVehicleChangeRequest(
            @PathVariable Long requestId,
            @RequestBody RejectDriverChangeRequestDTO rejectDriverChangeRequestDTO) {
        String email = AuthUtils.getCurrentUserEmail();
        AcceptDriverChangeRequestDTO response = adminService.rejectVehicleChangeRequest(
                requestId, email, rejectDriverChangeRequestDTO);
        return ResponseEntity.ok(response);
    }

    // 2.3 - Reject picture change request
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/driver-change-requests/picture/{requestId}/reject",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AcceptDriverChangeRequestDTO> rejectPictureChangeRequest(
            @PathVariable Long requestId,
            @RequestBody RejectDriverChangeRequestDTO rejectDriverChangeRequestDTO) {
        String email = AuthUtils.getCurrentUserEmail();
        AcceptDriverChangeRequestDTO response = adminService.rejectAvatarChangeRequest(
                requestId, email, rejectDriverChangeRequestDTO);
        return ResponseEntity.ok(response);
    }

    // 2.9.1 - Get passenger rides
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/rides/passenger", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<GetRideDTO>> getPassengerRides(
            @RequestParam String email,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {

        Page<GetRideDTO> rides = adminService.getPassengerRides(email, startDate, page, size, sort, direction);
        return ResponseEntity.ok(rides);
    }

    // 2.9.2 - Get single passenger ride by id
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/rides/passenger/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetReorderRideDTO> getPassengerRideById(
            @RequestParam String email,
            @PathVariable Long id) {
        GetReorderRideDTO ride = adminService.getPassengerRideById(email, id);
        return ResponseEntity.ok(ride);
    }

    // Get driver rides
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/rides/driver", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<GetRideDTO>> getDriverRides(
            @RequestParam String email,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {

        Page<GetRideDTO> rides = adminService.getDriverRides(email, startDate, page, size, sort, direction);
        return ResponseEntity.ok(rides);
    }

    // Get single driver ride by id
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/rides/driver/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetReorderRideDTO> getDriverRideById(
            @RequestParam String email,
            @PathVariable Long id) {
        GetReorderRideDTO ride = adminService.getDriverRideById(email, id);
        return ResponseEntity.ok(ride);
    }
}