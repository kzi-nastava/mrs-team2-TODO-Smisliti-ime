import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../env/environment';

export interface CreateRideRequestDTO {
  latitudes: number[];
  longitudes: number[];
  addresses: string[];
  scheduledTime: string | null;
  friendEmails: string[];
  hasBaby: boolean;
  hasPets: boolean;
  vehicleType: string;
}

export interface CreatedRideResponseDTO {
  status: string;
  message: string;
  rideId: number | null;
}

export interface EstimateResponse {
  price: number;
  durationMinutes: number;
  distanceKm: number;
}

export interface GetDriverActiveRideDTO {
  rideId: number;
  startingPoint: string;
  endingPoint: string;
  estimatedPrice: number;
  estimatedTimeMin: number;
  passengerName: string;
  passengerCount: number;
  status: string;
  latitudes?: number[];
  longitudes?: number[];
  addresses?: string[];
  scheduledTime?: string; // Null if ride not scheduled
}

export interface UpdatedRideDTO {
  id: number;
  status: string;
  startTime: string;
  endTime: string;
}

export interface DriverLocationDTO {
  driverId: number;
  rideId: number;
  latitude: number;
  longitude: number;
  status: string;
}

export interface StatusUpdateDTO {
  rideId: number;
  status: string;
  timestamp: string;
}

export interface RideCompletionDTO {
  rideId: number;
  status: string;
  price: number;
  startTime: string;
  endTime: string;
  durationMinutes: number;
}

export interface StopRideDTO {
  latitude: number;
  longitude: number;
  stoppedAt: string;
}

export interface GetPassengerActiveRideDTO {
  rideId: number;
  startingPoint: string;
  endingPoint: string;
  estimatedPrice: number;
  estimatedTimeMin: number;
  driverName?: string;
  status: string;
  latitudes: number[];
  longitudes: number[];
  addresses: string[];
}

export interface PassengerStatusUpdateDTO {
  rideId: number;
  status: string;
  message: string;
  timestamp: string;
}

export interface PassengerRideFinishedDTO {
  rideId: number;
  status: string;
  price: number;
  startTime: string;
  endTime: string;
  durationMinutes: number;
  message: string;
  timestamp: string;
}

export interface CancelRideRequestDTO {
  reason: string; // driver must provide reason; passenger sends empty string
}

export interface GetFavoriteRideDTO {
  id: number;
  addresses: string[];
  latitudes: number[];
  longitudes: number[];
  vehicleType: string;
  needsBabySeats: boolean;
  needsPetFriendly: boolean;
  linkedPassengerEmails: string[];
}

@Injectable({
  providedIn: 'root'
})
export class RideService {
  private apiUrl = `${environment.apiHost}/api/rides`;

  constructor(private http: HttpClient) {}

  orderRide(request: CreateRideRequestDTO): Observable<CreatedRideResponseDTO> {
    return this.http.post<CreatedRideResponseDTO>(`${this.apiUrl}/order`, request);
  }

  getEstimate(request: CreateRideRequestDTO): Observable<EstimateResponse> {
    return this.http.post<EstimateResponse>(`${this.apiUrl}/estimate`, request);
  }

  getDriverActiveRide(): Observable<GetDriverActiveRideDTO | null> {
    return this.http.get<GetDriverActiveRideDTO>(`${this.apiUrl}/driver/active`);
  }

  acceptRide(rideId: number): Observable<UpdatedRideDTO> {
    return this.http.put<UpdatedRideDTO>(`${this.apiUrl}/${rideId}/accept`, {});
  }

  startRide(rideId: number): Observable<UpdatedRideDTO> {
    return this.http.put<UpdatedRideDTO>(`${this.apiUrl}/${rideId}/start`, {});
  }

  endRide(rideId: number): Observable<UpdatedRideDTO> {
    return this.http.put<UpdatedRideDTO>(`${this.apiUrl}/${rideId}/finish`, {});
  }

  getPassengerActiveRide(): Observable<GetPassengerActiveRideDTO | null> {
    return this.http.get<GetPassengerActiveRideDTO>(`${this.apiUrl}/passenger/active`);
  }

  stopRide(rideId: number, payload: StopRideDTO) {
    return this.http.post<RideCompletionDTO>(`${this.apiUrl}/${rideId}/stop`, payload);
  }

  cancelRideByDriver(rideId: number, body: CancelRideRequestDTO): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${rideId}/cancel/driver`, body);
  }

  cancelRideByPassenger(rideId: number, body: CancelRideRequestDTO): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${rideId}/cancel/passenger`, body);
  }

  createPanic(rideId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${rideId}/panic`, {});
  }

  getFavoriteRides(): Observable<GetFavoriteRideDTO[]> {
    return this.http.get<GetFavoriteRideDTO[]>(`${this.apiUrl}/favorites`);
  }
}
