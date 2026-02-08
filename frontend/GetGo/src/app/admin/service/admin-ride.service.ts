import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../env/environment';
import { Observable } from 'rxjs';
import { GetRideDTO } from '../../model/ride.model';
import { GetInconsistencyReportDTO } from '../../model/inconsistency-report.model';
import { GetRatingDTO } from '../../model/rating.model';
import { GetDriverDTO, GetPassengerDTO } from '../../model/user.model';

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
export class AdminRideService {
  private _rides = signal<GetRideDTO[]>([]);

  rides = this._rides.asReadonly();

  constructor(private http: HttpClient) {}

  private getAuthToken(): string {
    const token = sessionStorage.getItem('authToken') || localStorage.getItem('authToken');
    if (!token) {
      throw new Error('No auth token found');
    }
    return token;
  }

  loadPassengerRides(
    email: string,
    page: number = 0,
    size: number = 5,
    startDate?: Date,
    sort: string = 'startTime',
    direction: string = 'DESC'
  ): Observable<PageResponse<GetRideDTO>> {
    let params: any = {
      email,
      page: page.toString(),
      size: size.toString(),
      sort,
      direction
    };

    if (startDate) {
      const day = startDate.getDate().toString().padStart(2, '0');
      const month = (startDate.getMonth() + 1).toString().padStart(2, '0');
      const year = startDate.getFullYear();
      params.startDate = `${day}-${month}-${year}`;
    }

    const token = this.getAuthToken();
    const url = `${environment.apiHost}/api/admin/rides/passenger`;

    console.log('Loading passenger rides with URL:', url);
    console.log('Params:', params);

    return this.http.get<PageResponse<GetRideDTO>>(url, {
      params,
      headers: {
        Authorization: `Bearer ${token}`,
      }
    });
  }

  loadDriverRides(
    email: string,
    page: number = 0,
    size: number = 5,
    startDate?: Date,
    sort: string = 'startTime',
    direction: string = 'DESC'
  ): Observable<PageResponse<GetRideDTO>> {
    let params: any = {
      email,
      page: page.toString(),
      size: size.toString(),
      sort,
      direction
    };

    if (startDate) {
      const day = startDate.getDate().toString().padStart(2, '0');
      const month = (startDate.getMonth() + 1).toString().padStart(2, '0');
      const year = startDate.getFullYear();
      params.startDate = `${day}-${month}-${year}`;
    }

    const token = this.getAuthToken();
    const url = `${environment.apiHost}/api/admin/rides/driver`;

    console.log('Loading driver rides with URL:', url);
    console.log('Params:', params);

    return this.http.get<PageResponse<GetRideDTO>>(url, {
      params,
      headers: {
        Authorization: `Bearer ${token}`,
      }
    });
  }

  getPassengerRideById(email: string, rideId: number): Observable<GetRideDTO> {
    const token = this.getAuthToken();
    const url = `${environment.apiHost}/api/admin/rides/passenger/${rideId}`;

    return this.http.get<GetRideDTO>(url, {
      params: { email },
      headers: {
        Authorization: `Bearer ${token}`,
      }
    });
  }

  getDriverRideById(email: string, rideId: number): Observable<GetRideDTO> {
    const token = this.getAuthToken();
    const url = `${environment.apiHost}/api/admin/rides/driver/${rideId}`;

    return this.http.get<GetRideDTO>(url, {
      params: { email },
      headers: {
        Authorization: `Bearer ${token}`,
      }
    });
  }

  getInconsistencyReports(rideId: number): Observable<GetInconsistencyReportDTO[]> {
    const url = `${environment.apiHost}/api/completed-rides/${rideId}/inconsistencies`;
    return this.http.get<GetInconsistencyReportDTO[]>(url);
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

  getPassengerProfile(passengerId: number): Observable<GetPassengerDTO> {
    const token = this.getAuthToken();
    return this.http.get<GetPassengerDTO>(`${environment.apiHost}/api/passenger/profile/${passengerId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      }
    });
  }

  setRides(rides: GetRideDTO[]) {
    this._rides.set(rides || []);
  }
}
