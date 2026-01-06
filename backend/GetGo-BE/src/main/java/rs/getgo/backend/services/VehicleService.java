package rs.getgo.backend.services;

import rs.getgo.backend.dtos.vehicle.GetVehicleDTO;

import java.util.Collection;

public interface VehicleService {
    Collection<GetVehicleDTO> getActiveVehicles();

    GetVehicleDTO getVehicle(Long id);

    GetVehicleDTO createVehicle(GetVehicleDTO vehicle) throws Exception;

    GetVehicleDTO updateVehicle(GetVehicleDTO vehicle) throws Exception;

    void deleteVehicle(Long id);
}
