package rs.getgo.backend.services.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import rs.getgo.backend.dtos.rideEstimate.CreateRideEstimateDTO;
import rs.getgo.backend.dtos.rideEstimate.CreatedRideEstimateDTO;
import rs.getgo.backend.model.entities.RideEstimate;
import rs.getgo.backend.repositories.RideEstimateRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import rs.getgo.backend.services.RideEstimateService;

@Service
public class RideEstimateServiceImpl implements RideEstimateService {

    private static final Logger log = LoggerFactory.getLogger(RideEstimateServiceImpl.class);

    private final RideEstimateRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final double ROUTE_FACTOR = 1.3;
    private static final double DEFAULT_AVG_SPEED_KMH = 30.0;
    private static final double BASE_FARE = 1.0;
    private static final double PER_KM_RATE = 0.8;

    // accept CoordinateMappingService via constructor (Spring will inject the implementation)
    public RideEstimateServiceImpl(RideEstimateRepository repository) {
        this.repository = repository;
    }

    @Override
    public CreatedRideEstimateDTO createEstimate(CreateRideEstimateDTO request) {
        if (request.getCoordinates() == null || request.getCoordinates().size() < 2) {
            throw new IllegalArgumentException("Need at least 2 coordinates: start and destination");
        }

        var start = request.getCoordinates().get(0);
        var dest = request.getCoordinates().get(1);

        CreateRideEstimateDTO dto = new CreateRideEstimateDTO();
        dto.setCoordinates(Arrays.asList(start, dest));

        double straightMeters = haversineMeters(start.getLat(), start.getLng(), dest.getLat(), dest.getLng());
        double approxRoadMeters = straightMeters * ROUTE_FACTOR;

        double hours = (approxRoadMeters / 1000.0) / DEFAULT_AVG_SPEED_KMH;
        long estimatedSeconds = Math.max(60L, Math.round(hours * 3600.0));
        int estimatedMinutes = (int) (estimatedSeconds / 60);

        double km = approxRoadMeters / 1000.0;
        double price = Math.round((BASE_FARE + PER_KM_RATE * km) * 100.0) / 100.0;

        RideEstimate entity = new RideEstimate();
        entity.setStartLat(start.getLat());
        entity.setStartLon(start.getLng());
        entity.setDestLat(dest.getLat());
        entity.setDestLon(dest.getLng());
        entity.setDistanceMeters(approxRoadMeters);
        entity.setEstimatedTimeMinutes(estimatedMinutes);
        entity.setPrice(price);
        entity.setCreatedAt(LocalDateTime.now());

        repository.save(entity);

        return new CreatedRideEstimateDTO(km, estimatedMinutes, price);
    }

    private String requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
        return value;
    }

    private double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }
}