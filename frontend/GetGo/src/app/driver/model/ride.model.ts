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
  username: string;
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
}
