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
