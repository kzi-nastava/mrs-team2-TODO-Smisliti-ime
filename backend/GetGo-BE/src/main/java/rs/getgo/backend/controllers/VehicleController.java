package rs.getgo.backend.controllers;

import dtos.vehicle.GetVehicleDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

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
}
