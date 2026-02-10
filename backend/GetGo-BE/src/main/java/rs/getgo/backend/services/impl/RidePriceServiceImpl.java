package rs.getgo.backend.services.impl;

import org.springframework.stereotype.Service;
import rs.getgo.backend.model.entities.RidePrice;
import rs.getgo.backend.model.enums.VehicleType;
import rs.getgo.backend.repositories.RidePriceRepository;
import rs.getgo.backend.services.RidePriceService;

@Service
public class RidePriceServiceImpl implements RidePriceService {
    private final RidePriceRepository ridePriceRepository;

    public RidePriceServiceImpl(RidePriceRepository ridePriceRepository) {
        this.ridePriceRepository = ridePriceRepository;
    }

    @Override
    public double getPrice(VehicleType vehicleType) {
        return ridePriceRepository
                .findByVehicleType(vehicleType)
                .orElseThrow(() -> new RuntimeException("Price not defined"))
                .getPricePerKm();
    }

    @Override
    public void updatePrice(VehicleType vehicleType, double pricePerKm) {
        RidePrice price = ridePriceRepository
                .findByVehicleType(vehicleType)
                .orElse(new RidePrice(null, vehicleType, pricePerKm));

        price.setPricePerKm(pricePerKm);
        ridePriceRepository.save(price);
    }
}
