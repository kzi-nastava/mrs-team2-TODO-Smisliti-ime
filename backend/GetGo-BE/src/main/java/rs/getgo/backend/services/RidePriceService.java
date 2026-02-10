package rs.getgo.backend.services;

import rs.getgo.backend.model.enums.VehicleType;

public interface RidePriceService {
    double getPrice(VehicleType vehicleType);
    void updatePrice(VehicleType vehicleType, double pricePerKm);
}
