package rs.getgo.backend.controllers;

import dtos.admin.GetAdminDTO;
import dtos.admin.UpdateAdminDTO;
import dtos.admin.UpdatedAdminDTO;
import dtos.driver.*;
import dtos.report.GetReportDTO;
import dtos.user.CreatedUserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    // 2.9.3 – Block user
    @PutMapping("/users/{id}/block")
    public ResponseEntity<CreatedUserDTO> blockUser(@PathVariable Long id) {
        CreatedUserDTO response = new CreatedUserDTO(id, "blocked@getgo.com", "Blocked", "User");
        return ResponseEntity.ok(response);
    }

    // 2.9.3 – Unblock user
    @PutMapping("/users/{id}/unblock")
    public ResponseEntity<CreatedUserDTO> unblockUser(@PathVariable Long id) {
        CreatedUserDTO response = new CreatedUserDTO(id, "unblocked@getgo.com", "Active", "User");
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
        CreatedUserDTO response = new CreatedUserDTO(10L, "admin@getgo.com", "Admin", "User");
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
        GetAdminDTO response = new GetAdminDTO();
        response.setId(3L);
        response.setEmail("admin@example.com");
        response.setName("Admin");

        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile
    @PutMapping(value = "/profile",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedAdminDTO> updateProfile(
            @RequestBody UpdateAdminDTO request) {

        UpdatedAdminDTO response = new UpdatedAdminDTO();
        response.setId(3L);
        response.setName(request.getName());

        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile (reviewing driver requests)
    @GetMapping(value = "/driver-change-requests", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GetDriverChangeRequestDTO>> getPendingChangeRequests() {

        List<GetDriverChangeRequestDTO> response = new ArrayList<>();
        GetDriverChangeRequestDTO request = new GetDriverChangeRequestDTO();
        request.setRequestId(1L);
        request.setDriverId(2L);
        request.setStatus("PENDING");
        response.add(request);

        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile (reviewing driver requests)
    @GetMapping(value = "/driver-change-requests/{requestId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetDriverChangeRequestDTO> getChangeRequest(
            @PathVariable Long requestId) {

        GetDriverChangeRequestDTO response = new GetDriverChangeRequestDTO();
        response.setRequestId(requestId);
        response.setDriverId(2L);
        response.setCurrentName("Jane");
        response.setRequestedName("Janet");
        response.setStatus("PENDING");

        return ResponseEntity.ok(response);
    }

    // 2.3 - User profile (reviewing driver requests)
    @PutMapping(value = "/driver-change-requests/{requestId}/approve", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedDriverChangeRequestDTO> approveChangeRequest(
            @PathVariable Long requestId) {

        UpdatedDriverChangeRequestDTO response = new UpdatedDriverChangeRequestDTO();
        response.setRequestId(requestId);
        response.setStatus("APPROVED");

        return ResponseEntity.ok(response);
    }

    // 2.3 - Reject driver change request
    @PutMapping(value = "/driver-change-requests/{requestId}/reject",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedDriverChangeRequestDTO> rejectChangeRequest(
            @PathVariable Long requestId,
            @RequestBody RejectChangeRequestDTO request) {

        UpdatedDriverChangeRequestDTO response = new UpdatedDriverChangeRequestDTO();
        response.setRequestId(requestId);
        response.setStatus("REJECTED");
        response.setRejectionReason(request.getReason());

        return ResponseEntity.ok(response);
    }

}