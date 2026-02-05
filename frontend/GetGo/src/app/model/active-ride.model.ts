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
  driverName: string;
  driverEmail: string;
  status: string;
  actualStartTime: string;
  estimatedDurationMin: number;
  vehicleType: string;
  needsBabySeats: boolean;
  needsPetFriendly: boolean;
  payingPassengerEmail: string;
  linkedPassengerEmails: string[];
  currentLocationAddress: string;
}
