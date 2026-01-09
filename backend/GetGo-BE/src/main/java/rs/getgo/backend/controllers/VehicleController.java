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
import rs.getgo.backend.services.VehicleService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    // 2.1.1 Display information
    @GetMapping(value = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<GetVehicleDTO>> getActiveVehicles(){
        Collection<GetVehicleDTO> activeVehicles = new ArrayList<>();

        activeVehicles.add(new GetVehicleDTO(1L, "Toyota Corolla", "Sedan", 44.8176, 20.4569, true));
        activeVehicles.add(new GetVehicleDTO(2L, "BMW X5", "SUV", 44.8200, 20.4600, true));
        return new ResponseEntity<Collection<GetVehicleDTO>>(activeVehicles, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetVehicleDTO> getVehicle(@PathVariable("id") Long id) {
        GetVehicleDTO vehicle = new GetVehicleDTO(id, "Mercedes A-Class", "Hatchback", 44.8190, 20.4570, true);

        return new ResponseEntity<GetVehicleDTO>(vehicle, HttpStatus.OK);
    }

    // 2.4.1 - Calling a ride (load all existing vehicle types into ddl)
    @GetMapping(value = "/types", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GetVehicleTypeDTO>> getVehicleTypes() {
        List<GetVehicleTypeDTO> response = new ArrayList<>();

        GetVehicleTypeDTO standard = new GetVehicleTypeDTO();
        standard.setType("STANDARD");
        response.add(standard);

        GetVehicleTypeDTO luxury = new GetVehicleTypeDTO();
        luxury.setType("LUXURY");
        response.add(luxury);

        GetVehicleTypeDTO van = new GetVehicleTypeDTO();
        van.setType("VAN");
        response.add(van);

        return ResponseEntity.ok(response);
    }
}
