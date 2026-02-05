export interface PanicAlertDTO {
  panicId: number;
  rideId: number;
  triggeredByUserId: number;
  triggeredAt: string;
  status: boolean;
  role?: 'passenger' | 'driver'; // Add this if backend provides it
}
