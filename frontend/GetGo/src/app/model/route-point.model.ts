export interface RoutePoint {
  id: number;

  startingPoint: string;
  endingPoint: string;

  estTimeMin: number;
  estDistanceKm: number;

  // Encoded polyline (from Google Maps API for drawing route on map)
  encodedPolyline: string;

  // Ordered waypoints
  waypoints: Array<{ lat: number; lng: number; timestamp: string }>;
}
