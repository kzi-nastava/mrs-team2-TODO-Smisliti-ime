package rs.getgo.backend.services;

import rs.getgo.backend.model.entities.WayPoint;
import rs.getgo.backend.services.impl.rides.MapboxRoutingService;

import java.util.List;

public interface RoutingService {
    MapboxRoutingService.RouteResponse getRoute(double startLat, double startLng, double endLat, double endLng);
    double calculateRemainingTime(double currentLat, double currentLng, List<WayPoint> remainingWaypoints);
    String convertCoordinatesToJson(List<MapboxRoutingService.Coordinate> coordinates);
    List<MapboxRoutingService.Coordinate> parseJsonToCoordinates(String json);
}
