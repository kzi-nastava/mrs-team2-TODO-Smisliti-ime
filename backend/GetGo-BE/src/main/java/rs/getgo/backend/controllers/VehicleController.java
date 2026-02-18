package rs.getgo.backend.controllers;

import rs.getgo.backend.dtos.vehicle.GetVehicleDTO;
import rs.getgo.backend.dtos.vehicle.GetVehicleTypeDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.getgo.backend.services.RidePriceService;
import rs.getgo.backend.services.VehicleService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final RidePriceService ridePriceService;

    public VehicleController(
            VehicleService vehicleService,
            RidePriceService ridePriceService) {
        this.vehicleService = vehicleService;
        this.ridePriceService = ridePriceService;
    }

    // 2.1.1 Display information
    @GetMapping(value = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<GetVehicleDTO>> getActiveVehicles(){
        Collection<GetVehicleDTO> activeVehicles = vehicleService.getActiveVehicles();

        return new ResponseEntity<Collection<GetVehicleDTO>>(activeVehicles, HttpStatus.OK);
    }

    @GetMapping(value = "/types", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getVehicleTypes() {
        return ResponseEntity.ok(ridePriceService.getVehicleTypes());
    }
}
