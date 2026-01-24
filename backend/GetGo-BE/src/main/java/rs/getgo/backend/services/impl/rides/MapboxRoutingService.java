package rs.getgo.backend.services.impl.rides;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class MapboxRoutingService {

    @Value("${mapbox.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Speedup factor
    private static final int DEMO_SPEED_FACTOR = 60;

    public MapboxRoutingService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get route between two points and return detailed coordinates
     */
    public RouteResponse getRoute(double startLat, double startLng, double endLat, double endLng) {
        // Note: (lng, lat), not (lat, lng)
        String url = String.format(
                "https://api.mapbox.com/directions/v5/mapbox/driving/%f,%f;%f,%f?geometries=geojson&overview=full&access_token=%s",
                startLng, startLat, endLng, endLat, apiKey
        );

        try {
            String response = restTemplate.getForObject(url, String.class);
            return parseRouteResponse(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get route from Mapbox: " + e.getMessage(), e);
        }
    }

    private RouteResponse parseRouteResponse(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        JsonNode routes = root.get("routes");

        if (routes == null || routes.isEmpty()) {
            throw new RuntimeException("No routes found in Mapbox response");
        }

        JsonNode firstRoute = routes.get(0);

        double durationSeconds = firstRoute.get("duration").asDouble();
        double distanceMeters = firstRoute.get("distance").asDouble();

        // Get coordinates (array of [lng, lat])
        JsonNode geometry = firstRoute.get("geometry");
        JsonNode coordinates = geometry.get("coordinates");

        List<Coordinate> path = new ArrayList<>();
        for (JsonNode coord : coordinates) {
            double lng = coord.get(0).asDouble();
            double lat = coord.get(1).asDouble();
            path.add(new Coordinate(lat, lng));
        }

        // Calculate mock duration
        int mockDurationSeconds = (int) Math.ceil(durationSeconds / DEMO_SPEED_FACTOR);

        return new RouteResponse(
                path,
                durationSeconds,
                mockDurationSeconds,
                distanceMeters / 1000.0 // Convert to km
        );
    }

    /**
     * Pair (lat, long)
     */
    public record Coordinate(double latitude, double longitude) {}

    /**
     * Response from map API
     */
    public record RouteResponse(
            List<Coordinate> coordinates,
            double realDurationSeconds,
            int mockDurationSeconds,
            double distanceKm) {}
}