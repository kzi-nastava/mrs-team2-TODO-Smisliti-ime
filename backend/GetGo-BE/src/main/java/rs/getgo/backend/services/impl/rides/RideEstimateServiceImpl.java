package rs.getgo.backend.services.impl.rides;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rs.getgo.backend.dtos.rideEstimate.CreateRideEstimateDTO;
import rs.getgo.backend.dtos.rideEstimate.CreatedRideEstimateDTO;
import rs.getgo.backend.model.entities.RideEstimate;
import rs.getgo.backend.repositories.RideEstimateRepository;

import java.time.LocalDateTime;
import java.util.List;

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

        List<?> coords = request.getCoordinates();
        int n = coords.size();

        // compute straight-line meters per segment and minutes per segment
        double totalStraightMeters = 0.0;
        StringBuilder breakdown = new StringBuilder();
        int totalMinutes = 0;

        for (int i = 0; i < n - 1; i++) {
            // assuming coordinate objects have getLat() and getLng()
            var a = request.getCoordinates().get(i);
            var b = request.getCoordinates().get(i + 1);

            double latA = ((Number) invokeGetter(a, "getLat")).doubleValue();
            double lonA = ((Number) invokeGetter(a, "getLng")).doubleValue();
            double latB = ((Number) invokeGetter(b, "getLat")).doubleValue();
            double lonB = ((Number) invokeGetter(b, "getLng")).doubleValue();

            double segMeters = haversineMeters(latA, lonA, latB, lonB);
            totalStraightMeters += segMeters;

            double segKm = Math.round((segMeters / 1000.0) * 100.0) / 100.0;
            int segMinutes = (int) Math.max(1L, Math.round((segKm / DEFAULT_AVG_SPEED_KMH) * 60.0));
            totalMinutes += segMinutes;

            if (i > 0) breakdown.append("; ");
            breakdown.append(String.format("%d->%d: %.2f km, %d min", i + 1, i + 2, segKm, segMinutes));
        }

        // approximate road meters and compute totals
        double approxRoadMeters = totalStraightMeters * ROUTE_FACTOR;
        double km = Math.round((approxRoadMeters / 1000.0) * 100.0) / 100.0;
        double price = Math.round((BASE_FARE + PER_KM_RATE * km) * 100.0) / 100.0;

        // persist entity with start = first coord and dest = last coord
        var start = request.getCoordinates().get(0);
        var dest = request.getCoordinates().get(n - 1);

        double startLat = ((Number) invokeGetter(start, "getLat")).doubleValue();
        double startLon = ((Number) invokeGetter(start, "getLng")).doubleValue();
        double destLat = ((Number) invokeGetter(dest, "getLat")).doubleValue();
        double destLon = ((Number) invokeGetter(dest, "getLng")).doubleValue();

        RideEstimate entity = new RideEstimate();
        entity.setStartLat(startLat);
        entity.setStartLon(startLon);
        entity.setDestLat(destLat);
        entity.setDestLon(destLon);
        entity.setDistanceMeters(approxRoadMeters);
        entity.setEstimatedTimeMinutes(totalMinutes);
        entity.setPrice(price);
        entity.setCreatedAt(LocalDateTime.now());

        repository.save(entity);

        // append total to breakdown
        breakdown.append(String.format("; Total: %d min", totalMinutes));

        return new CreatedRideEstimateDTO(price, totalMinutes, km);
    }

    /**
     * Helper to call getter reflectively without creating a direct dependency on DTO class.
     * Expects methodName like "getLat" or "getLng" that returns Number.
     */
    private Object invokeGetter(Object obj, String methodName) {
        try {
            return obj.getClass().getMethod(methodName).invoke(obj);
        } catch (Exception ex) {
            throw new IllegalStateException("Coordinate object must have " + methodName + "() returning Number", ex);
        }
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