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
}

export interface UpdatedRideDTO {
  id: number;
  status: string;
  startTime: string;
  endTime: string;
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

  startRide(rideId: number): Observable<UpdatedRideDTO> {
    return this.http.put<UpdatedRideDTO>(`${this.apiUrl}/${rideId}/start`, {});
  }

  endRide(rideId: number): Observable<UpdatedRideDTO> {
    return this.http.put<UpdatedRideDTO>(`${this.apiUrl}/${rideId}/finish`, {});
  }
}
