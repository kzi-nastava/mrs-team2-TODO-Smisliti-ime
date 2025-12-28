package rs.getgo.backend.controllers;

import dtos.report.GetReportDTO;
import dtos.user.CreatedUserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}