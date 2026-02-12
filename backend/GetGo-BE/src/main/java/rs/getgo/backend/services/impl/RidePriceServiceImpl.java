package rs.getgo.backend.services.impl;

import org.springframework.stereotype.Service;
import rs.getgo.backend.dtos.ridePrice.GetRidePriceDTO;
import rs.getgo.backend.model.entities.RidePrice;
import rs.getgo.backend.model.enums.VehicleType;
import rs.getgo.backend.repositories.RidePriceRepository;
import rs.getgo.backend.services.RidePriceService;

import java.util.List;

@Service
public class RidePriceServiceImpl implements RidePriceService {
    private final RidePriceRepository ridePriceRepository;

    public RidePriceServiceImpl(RidePriceRepository ridePriceRepository) {
        this.ridePriceRepository = ridePriceRepository;
    }

    @Override
    public GetRidePriceDTO getPrices(VehicleType vehicleType) {

        return ridePriceRepository
                .findByVehicleType(vehicleType)
                .map(price -> new GetRidePriceDTO(
                        price.getPricePerKm(),
                        price.getStartPrice()
                ))
                .orElse(new GetRidePriceDTO(null, null));
    }


    @Override
    public void updatePrice(VehicleType type, Double pricePerKm, Double startPrice) {
        RidePrice price = ridePriceRepository
                .findByVehicleType(type)
                .orElse(new RidePrice(type));

        if (pricePerKm != null) {
            price.setPricePerKm(pricePerKm);
        }

        if (startPrice != null) {
            price.setStartPrice(startPrice);
        }

        ridePriceRepository.save(price);
    }

    @Override
    public List<String> getVehicleTypes() {
        return ridePriceRepository.findAll().stream()
                .map(rp -> rp.getVehicleType().name())
                .toList();
    }
}
