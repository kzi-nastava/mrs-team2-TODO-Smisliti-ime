export interface RideTracking {
  rideId: number;
  vehicleLatitude: number;
  vehicleLongitude: number;
  startAddress: string;
  destinationAddress: string;
  estimatedTime: number;
  completedDistance?: number;
  totalDistance?: number;
}
