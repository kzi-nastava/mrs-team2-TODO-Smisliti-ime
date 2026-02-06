import {VehicleType} from './vehicle.model';
import {RoutePoint} from './route-point.model';

export interface Ride {
  id: number;
  startDate: string
  startTime: string;
  endTime: string | null;
  startLocation: string;
  endLocation: string;
  price: number;
  rideId: number;
  panicActivated: boolean;
  canceledBy?: 'DRIVER' | 'PASSENGER' | 'ADMIN';
  status: 'CREATED' | 'IN_PROGRESS' | 'FINISHED' | 'CANCELED';
  passengers: string[];
}

export interface GetRidePassengerDTO {
  id: number;
  email: string;
  name: string;
  surname: string;
}

export interface GetRideDTO {
  id: number;
  driverId: number;

  passengers: GetRidePassengerDTO[];

  startPoint: string;
  endPoint: string;

  startingTime: string;   // LocalDateTime → string
  finishedTime: string;

  duration: number;
  isCancelled: boolean;
  isFavourite: boolean;
  status: 'ACTIVE' | 'FINISHED' | 'CANCELLED' | 'SCHEDULED';
  price: number;
  panicActivated?: boolean;
  waypoints?: Array<{ lat: number; lng: number; timestamp: string }>;
  vehicleType: VehicleType;
  needsBabySeats: boolean;
  needsPetFriendly: boolean;
  route: RoutePoint[];
}

export interface GetInconsistencyReportDTO {
  id: number;
  createdAt: string;
  passengerEmail: string;
  text: string;
}
