import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../env/environment';

export interface GetActiveDriverLocationDTO {
  driverId: number;
  latitude: number;
  longitude: number;
  vehicleType: string;
  isAvailable: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class DriverService {
  private baseUrl = `${environment.apiHost}/api/drivers`;

  constructor(private http: HttpClient) {}

  getActiveDriverLocations(): Observable<GetActiveDriverLocationDTO[]> {
    return this.http.get<GetActiveDriverLocationDTO[]>(`${this.baseUrl}/active-locations`);
  }
}
