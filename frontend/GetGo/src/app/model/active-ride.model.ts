export interface GetActiveRideAdminDTO {
  id: number;
  driverId: number;
  driverName: string;
  driverEmail: string;
  startPoint: string;
  endPoint: string;
  scheduledTime: string;
  actualStartTime: string;
  status: string;
  vehicleType: string;
  estimatedPrice: number;
  estimatedDurationMin: number;
}

export interface GetActiveRideAdminDetailsDTO {
  id: number;

  driverId: number;
  driverName: string;
  driverEmail: string;

  actualStartTime: string;
  scheduledTime: string;

  status: string;
  vehicleType: string;

  needsBabySeats: boolean;
  needsPetFriendly: boolean;

  estimatedPrice: number;
  estimatedDurationMin: number;

  payingPassenger: string;
  linkedPassengers: string[];

  currentAddress: string;

  currentLat: number;
  currentLng: number;
  latitudes?: number[];
  longitudes?: number[];
}
