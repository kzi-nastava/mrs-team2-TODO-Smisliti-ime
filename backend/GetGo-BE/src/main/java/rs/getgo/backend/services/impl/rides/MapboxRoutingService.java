package rs.getgo.backend.services.impl.rides;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import rs.getgo.backend.model.entities.WayPoint;

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

    public double calculateRemainingTime(
            double currentLat,
            double currentLng,
            List<WayPoint> remainingWaypoints
    ) {
        if (remainingWaypoints.isEmpty()) {
            return 0.0;
        }

        double totalTime = 0.0;

        // From current location to first remaining waypoint
        WayPoint firstWaypoint = remainingWaypoints.getFirst();
        RouteResponse firstSegment = getRoute(
                currentLat, currentLng,
                firstWaypoint.getLatitude(), firstWaypoint.getLongitude()
        );
        totalTime += firstSegment.realDurationSeconds() / 60.0;

        // Between remaining waypoints
        for (int i = 0; i < remainingWaypoints.size() - 1; i++) {
            WayPoint from = remainingWaypoints.get(i);
            WayPoint to = remainingWaypoints.get(i + 1);

            RouteResponse segment = getRoute(
                    from.getLatitude(), from.getLongitude(),
                    to.getLatitude(), to.getLongitude()
            );
            totalTime += segment.realDurationSeconds() / 60.0;
        }

        return totalTime;
    }

    public String convertCoordinatesToJson(List<Coordinate> coordinates) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < coordinates.size(); i++) {
            Coordinate coord = coordinates.get(i);
            json.append(String.format("[%.6f,%.6f]", coord.longitude(), coord.latitude()));
            if (i < coordinates.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    public List<Coordinate> parseJsonToCoordinates(String json) {
        List<Coordinate> coords = new ArrayList<>();
        try {
            String cleaned = json.replace("[", "").replace("]", "").trim();
            if (cleaned.isEmpty()) return coords;
            String[] tokens = cleaned.split(",");
            for (int i = 0; i < tokens.length - 1; i += 2) {
                double lng = Double.parseDouble(tokens[i].trim());
                double lat = Double.parseDouble(tokens[i + 1].trim());
                coords.add(new Coordinate(lat, lng));
            }
        } catch (Exception e) {
            System.err.println("[Roaming] Failed to parse path: " + e.getMessage());
        }
        return coords;
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