package rs.getgo.backend.services;

import rs.getgo.backend.dtos.ridePrice.GetRidePriceDTO;
import rs.getgo.backend.model.enums.VehicleType;

import java.util.List;

public interface RidePriceService {
    double calculateRidePrice(VehicleType vehicleType, double distanceKm);
    GetRidePriceDTO getPrices(VehicleType vehicleType);
    void updatePrice(VehicleType type, Double pricePerKm, Double startPrice);
    List<String> getVehicleTypes();
}
