package rs.getgo.backend.services.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rs.getgo.backend.dtos.rideEstimate.CreateRideEstimateDTO;
import rs.getgo.backend.dtos.rideEstimate.CreatedRideEstimateDTO;
import rs.getgo.backend.model.entities.RideEstimate;
import rs.getgo.backend.repositories.RideEstimateRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.web.client.HttpClientErrorException;
import rs.getgo.backend.services.RideEstimateService;
import rs.getgo.backend.services.CoordinateMappingService;

@Service
public class RideEstimateServiceImpl implements RideEstimateService {

    private static final Logger log = LoggerFactory.getLogger(RideEstimateServiceImpl.class);

    private final RideEstimateRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CoordinateMappingService coordinateMappingService;

    private static final double ROUTE_FACTOR = 1.3;
    private static final double DEFAULT_AVG_SPEED_KMH = 30.0;
    private static final double BASE_FARE = 1.0;
    private static final double PER_KM_RATE = 0.8;

    // accept CoordinateMappingService via constructor (Spring will inject the implementation)
    public RideEstimateServiceImpl(RideEstimateRepository repository, CoordinateMappingService coordinateMappingService) {
        this.repository = repository;
        this.coordinateMappingService = coordinateMappingService;
    }

    @Override
    public CreatedRideEstimateDTO createEstimate(CreateRideEstimateDTO request) {

        String startAddress = requireNotBlank(request.getOrigin(), "origin");
        String destAddress  = requireNotBlank(request.getDestination(), "destination");

        log.info("Received estimate request");
        log.info("  Origin full='{}'", startAddress);
        log.info("  Destination full='{}'", destAddress);

        double[] start = geocodeOrFail(startAddress, "origin");
        double[] dest  = geocodeOrFail(destAddress, "destination");

        log.info("Geocoded coordinates");
        log.info("  Start=({}, {})", start[0], start[1]);
        log.info("  Dest =({}, {})", dest[0], dest[1]);

        double straightMeters = haversineMeters(start[0], start[1], dest[0], dest[1]);
        double approxRoadMeters = straightMeters * ROUTE_FACTOR;

        double hours = (approxRoadMeters / 1000.0) / DEFAULT_AVG_SPEED_KMH;
        long estimatedSeconds = Math.max(60L, Math.round(hours * 3600.0));
        int estimatedMinutes = (int) (estimatedSeconds / 60);

        double km = approxRoadMeters / 1000.0;
        double price = Math.round((BASE_FARE + PER_KM_RATE * km) * 100.0) / 100.0;

        RideEstimate entity = new RideEstimate();
        entity.setStartAddress(startAddress);
        entity.setDestinationAddress(destAddress);
        entity.setStartLat(start[0]);
        entity.setStartLon(start[1]);
        entity.setDestLat(dest[0]);
        entity.setDestLon(dest[1]);
        entity.setDistanceMeters(approxRoadMeters);
        entity.setEstimatedTimeMinutes(estimatedMinutes);
        entity.setPrice(price);
        entity.setCreatedAt(LocalDateTime.now());

        repository.save(entity);

        log.info("Ride estimate saved ({} km, {} min, {} EUR)",
                String.format("%.2f", km), estimatedMinutes, price);

        return new CreatedRideEstimateDTO(km, estimatedMinutes, price);
    }

    private double[] geocodeOrFail(String address, String label) {
        return geocode(address)
                .orElseThrow(() -> new IllegalStateException(
                        "Failed to geocode " + label + " address: '" + address + "'"));
    }

    @Override
    public Optional<double[]> geocode(String fullAddress) {
        if (fullAddress == null || fullAddress.isBlank()) {
            log.error("Geocode skipped – address is blank");
            return Optional.empty();
        }

        try {
            log.info("Geocoding with cleaned structured address");
            log.info("  Full address='{}'", fullAddress);

            // Use free-text 'q' parameter: more robust for localized / messy addresses
            String q = URLEncoder.encode(fullAddress, StandardCharsets.UTF_8);
            String url = "https://nominatim.openstreetmap.org/search?format=json&limit=1"
                    + "&q=" + q
                    + "&addressdetails=1"
                    + "&accept-language=sr";

            // delegate to shared helper (which now includes proper headers and HTTP error handling)
            return coordinateMappingService.call(url, "q");
        } catch (Exception e) {
            log.error("Nominatim geocoding exception for '{}': {}", fullAddress, e.getMessage());
            return Optional.empty();
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

