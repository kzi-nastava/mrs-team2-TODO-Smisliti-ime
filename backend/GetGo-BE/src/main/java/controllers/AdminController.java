package controllers;

import dtos.responses.ReportResponseDTO;
import dtos.responses.UserResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    // User Blocking
    @PutMapping("/users/{id}/block")
    public ResponseEntity<Void> blockUser(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    // User Unblocking
    @PutMapping("/users/{id}/unblock")
    public ResponseEntity<Void> unblockUser(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    // View Reports
    @GetMapping("/reports")
    public ResponseEntity<List<ReportResponseDTO>> getReports(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        ReportResponseDTO report =
                new ReportResponseDTO("01-01-2025 / 31-01-2025",
                        120, 860.5, 750.0);

        return ResponseEntity.ok(List.of(report));
    }

    // Create Admin Profile
    @PostMapping("/create")
    public ResponseEntity<UserResponseDTO> createAdmin() {

        UserResponseDTO response =
                new UserResponseDTO(10L, "admin@getgo.com",
                        "Admin", "User");

        return ResponseEntity.status(201).body(response);
    }
}