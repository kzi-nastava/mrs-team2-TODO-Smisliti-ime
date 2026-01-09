package rs.getgo.backend.services;

import rs.getgo.backend.dtos.vehicle.GetVehicleDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class VehicleServiceImpl implements VehicleService {
    private final Collection<GetVehicleDTO> vehicles = new ArrayList<>();

    public VehicleServiceImpl() {

        vehicles.add(new GetVehicleDTO(1L, "Toyota Corolla", "Sedan", 44.8176, 20.4569, true));
        vehicles.add(new GetVehicleDTO(2L, "BMW X5", "SUV", 44.8200, 20.4600, false));
    }

    @Override
    public Collection<GetVehicleDTO> getActiveVehicles() {
        Collection<GetVehicleDTO> activeVehicles = new ArrayList<>();
        for (GetVehicleDTO v : vehicles) {
            if (Boolean.TRUE.equals(v.getIsAvailable())) {
                activeVehicles.add(v);
            }
        }
        return activeVehicles;
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
            throw new Exception("ID mora biti null prilikom kreiranja novog vozila.");
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
}
