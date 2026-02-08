import { Injectable, signal } from '@angular/core';
import { Ride, GetRideDTO } from '../../model/ride.model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../env/environment';
import { Signal } from '@angular/core';
import { Observable } from 'rxjs';
import { GetInconsistencyReportDTO } from '../../model/inconsistency-report.model';
import { GetRatingDTO } from '../../model/rating.model';
import { GetDriverDTO } from '../../model/user.model';

export interface CreatedFavoriteRideDTO {
  favoriteRideId: number;
  success: boolean;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root',
})

export class RideService {
  private _rides = signal<GetRideDTO[]>([]);

  rides = this._rides.asReadonly()

  constructor(private http: HttpClient) {}

  loadRides(
    page: number = 0,
    size: number = 5,
    startDate?: Date
  ): Observable<PageResponse<GetRideDTO>> {
    let params: any = {page, size};

    if (startDate) {
      const day = startDate.getDate().toString().padStart(2,'0');
      const month = (startDate.getMonth() + 1).toString().padStart(2,'0');
      const year = startDate.getFullYear();
      params.startDate = `${day}-${month}-${year}`;
    }

    const token = this.getAuthToken();
    const url = `${environment.apiHost}/api/passenger/rides`;

    console.log('Request URL:', url);
    console.log('Request params:', params);

    return this.http.get<PageResponse<GetRideDTO>>(url, {
      params,
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
  }

  setRides(rides: GetRideDTO[]) {
    this._rides.set(rides || []);
  }

  private getAuthToken(): string {
    const token = sessionStorage.getItem('authToken') || localStorage.getItem('authToken');
    if (!token) {
      throw new Error('No auth token found');
    }
    return token;
  }

  getInconsistencyReports(rideId: number) {
    const url = `${environment.apiHost}/api/completed-rides/${rideId}/inconsistencies`;
    return this.http.get<GetInconsistencyReportDTO[]>(url);
  }

  favoriteRide(rideId: number): Observable<CreatedFavoriteRideDTO> {
    const url = `${environment.apiHost}/api/rides/${rideId}/favorite`;
    return this.http.post<CreatedFavoriteRideDTO>(url, {}, {
      headers: {
        Authorization: `Bearer ${this.getAuthToken()}`,
      }
    });
  }

  unfavoriteRide(rideId: number): Observable<void> {
    const url = `${environment.apiHost}/api/rides/${rideId}/favorite`;
    return this.http.delete<void>(url, {
      headers: {
        Authorization: `Bearer ${this.getAuthToken()}`,
      }
    });
  }

  getRideById(rideId: number): Observable<GetRideDTO> {
    const token = this.getAuthToken();
    const url = `${environment.apiHost}/api/passenger/rides/${rideId}`;

    return this.http.get<GetRideDTO>(url, {
      headers: {
        Authorization: `Bearer ${token}`,
      }
    });
  }

  getRatingsByRide(rideId: number): Observable<GetRatingDTO[]> {
    const token = this.getAuthToken();
    return this.http.get<GetRatingDTO[]>(`${environment.apiHost}/api/ratings/ride/${rideId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      }
    });
  }

  getDriverProfile(driverId: number): Observable<GetDriverDTO> {
    const token = this.getAuthToken();
    return this.http.get<GetDriverDTO>(`${environment.apiHost}/api/drivers/profile/${driverId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      }
    });
  }
}
