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
}
