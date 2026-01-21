package rs.getgo.backend.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import rs.getgo.backend.dtos.vehicle.GetVehicleDTO;
import org.springframework.stereotype.Service;
import rs.getgo.backend.model.entities.Vehicle;
import rs.getgo.backend.repositories.RatingRepository;
import rs.getgo.backend.repositories.VehicleRepository;
import rs.getgo.backend.services.VehicleService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    private final Collection<GetVehicleDTO> vehicles = new ArrayList<>();

    public VehicleServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public Collection<GetVehicleDTO> getActiveVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();

        return vehicles.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public GetVehicleDTO getVehicle(Long id) {
        for (GetVehicleDTO v : vehicles) {
            if (v.getId().equals(id)) return v;
        }
        return null;
    }

    @Override
    public GetVehicleDTO createVehicle(GetVehicleDTO vehicle) throws Exception {
        if (vehicle.getId() != null) {
            throw new Exception("ID must be null when we creating new vehicle.");
        }
        long newId = vehicles.size() + 1L;
        vehicle.setId(newId);
        vehicles.add(vehicle);
        return vehicle;
    }

    @Override
    public GetVehicleDTO updateVehicle(GetVehicleDTO vehicle) throws Exception {
        GetVehicleDTO existing = getVehicle(vehicle.getId());
        if (existing == null) {
            throw new Exception("Searched vehicle doesn't exists.");
        }
        existing.setModel(vehicle.getModel());
        existing.setType(vehicle.getType());
        existing.setLatitude(vehicle.getLatitude());
        existing.setLongitude(vehicle.getLongitude());
        existing.setIsAvailable(vehicle.getIsAvailable());
        return existing;
    }

    @Override
    public void deleteVehicle(Long id) {
        vehicles.removeIf(v -> v.getId().equals(id));
    }

    private GetVehicleDTO mapToDTO(Vehicle v) {
        Double lat = null;
        Double lon = null;

        if (v.getCurrentLocation() != null) {
            lat = v.getCurrentLocation().getLatitude();
            lon = v.getCurrentLocation().getLongitude();
        }

        return new GetVehicleDTO(
                v.getId(),
                v.getModel(),
                v.getType() != null ? v.getType().name() : null,
                lat,
                lon,
                v.getIsAvailable()
        );
    }

}
