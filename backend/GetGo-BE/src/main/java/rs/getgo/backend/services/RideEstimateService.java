package rs.getgo.backend.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Service
public class RideEstimateService {

    private static final Logger log = LoggerFactory.getLogger(RideEstimateService.class);

    private final RideEstimateRepository repository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final double ROUTE_FACTOR = 1.3;
    private static final double DEFAULT_AVG_SPEED_KMH = 30.0;
    private static final double BASE_FARE = 1.0;
    private static final double PER_KM_RATE = 0.8;

    private static final String GOOGLE_API_KEY = "135826866cd24cee716da6037095d5ac0ec0a77e2e43fbb42c349bee9176b00c";

    public RideEstimateService(RideEstimateRepository repository) {
        this.repository = repository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public CreatedRideEstimateDTO createEstimate(CreateRideEstimateDTO request) {
        String startAddress = safeStringOf(request::getOrigin);
        String destAddress = safeStringOf(request::getDestination);

        log.info("Received estimate request - Origin: '{}', Destination: '{}'", startAddress, destAddress);

        Optional<double[]> startCoords = geocode(shortenAddress(startAddress));
        Optional<double[]> destCoords = geocode(shortenAddress(destAddress));

        double startLat = startCoords.map(c -> c[0]).orElse(0.0);
        double startLon = startCoords.map(c -> c[1]).orElse(0.0);
        double destLat = destCoords.map(c -> c[0]).orElse(0.0);
        double destLon = destCoords.map(c -> c[1]).orElse(0.0);

        log.info("Geocoded - Start: ({}, {}), Dest: ({}, {})", startLat, startLon, destLat, destLon);

        double straightMeters = haversineMeters(startLat, startLon, destLat, destLon);
        double approxRoadMeters = straightMeters * ROUTE_FACTOR;

        double hours = (approxRoadMeters / 1000.0) / DEFAULT_AVG_SPEED_KMH;
        long estimatedSeconds = Math.max(1L, Math.round(hours * 3600.0));

        double km = approxRoadMeters / 1000.0;
        double price = Math.round((BASE_FARE + PER_KM_RATE * km) * 100.0) / 100.0;

        RideEstimate entity = new RideEstimate();
        entity.setStartAddress(startAddress);
        entity.setDestinationAddress(destAddress);
        entity.setStartLat(startLat);
        entity.setStartLon(startLon);
        entity.setDestLat(destLat);
        entity.setDestLon(destLon);
        entity.setDistanceMeters(approxRoadMeters);
        entity.setEstimatedTimeMinutes((int) estimatedSeconds);
        entity.setPrice(price);
        entity.setCreatedAt(LocalDateTime.now());

        repository.save(entity);

        return new CreatedRideEstimateDTO(km, (int) estimatedSeconds, price);
    }

    private String shortenAddress(String fullAddress) {
        if (fullAddress == null) return "";
        String[] parts = fullAddress.split(",");
        String street = parts.length > 0 ? parts[0].trim() : "";
        String city = parts.length > 1 ? parts[parts.length - 2].trim() : "";
        String country = parts.length > 0 ? parts[parts.length - 1].trim() : "";
        return street + ", " + city + ", " + country;
    }

    private Optional<double[]> geocode(String address) {
        try {
            if (address == null || address.isEmpty()) return Optional.empty();

            String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" +
                    URLEncoder.encode(address, StandardCharsets.UTF_8) +
                    "&key=" + GOOGLE_API_KEY;

            String resp = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(resp);
            JsonNode results = root.path("results");

            if (results.isArray() && results.size() > 0) {
                JsonNode loc = results.get(0).path("geometry").path("location");
                double lat = loc.path("lat").asDouble();
                double lon = loc.path("lng").asDouble();
                return Optional.of(new double[]{lat, lon});
            }
        } catch (Exception ex) {
            log.error("Geocoding failed for '{}': {}", address, ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    private double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    private <T> Optional<T> safeOf(SupplierWithException<T> s) {
        try { return Optional.ofNullable(s.get()); } catch (Exception e) { return Optional.empty(); }
    }

    private String safeStringOf(SupplierWithException<String> s) {
        return safeOf(s).orElse(null);
    }

    @FunctionalInterface
    private interface SupplierWithException<T> { T get() throws Exception; }
}