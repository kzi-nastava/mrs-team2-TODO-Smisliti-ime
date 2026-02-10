package rs.getgo.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.getgo.backend.model.enums.VehicleType;
import rs.getgo.backend.services.RidePriceService;

@RestController
@RequestMapping("/api/ride-price")
@PreAuthorize("hasRole('ADMIN')")
public class RidePriceController {
    private final RidePriceService ridePriceService;

    public RidePriceController(RidePriceService ridePriceService) {
        this.ridePriceService = ridePriceService;
    }

    @GetMapping("/prices/{vehicleType}")
    public ResponseEntity<Double> getPrice(@PathVariable VehicleType vehicleType) {
        return ResponseEntity.ok(ridePriceService.getPrice(vehicleType));
    }

    @PutMapping("/prices/{vehicleType}")
    public ResponseEntity<Void> updatePrice(
            @PathVariable VehicleType vehicleType,
            @RequestBody UpdateRidePriceDTO dto) {

        ridePriceService.updatePrice(vehicleType, dto.getPricePerKm());
        return ResponseEntity.ok().build();
    }
}
