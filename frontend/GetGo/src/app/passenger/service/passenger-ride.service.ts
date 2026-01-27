import { Injectable, signal } from '@angular/core';
import { Ride, GetRideDTO } from '../model/ride.model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../env/environment';
import { Signal } from '@angular/core';
import { Observable } from 'rxjs';
import { GetInconsistencyReportDTO } from '../../model/inconsistency-report.model';

export interface CreatedFavoriteDTO {
  userId: number;
  rideId: number;
  createdAt: string;
}

@Injectable({
  providedIn: 'root',
})

export class RideService {
  private _rides = signal<GetRideDTO[]>([]);

  rides = this._rides.asReadonly()

  constructor(private http: HttpClient) {}

  loadRides(startDate?: Date) {
    let url = `${environment.apiHost}/api/passenger/rides`;

    if (startDate) {
      const day = startDate.getDate().toString().padStart(2,'0');
      const month = (startDate.getMonth() + 1).toString().padStart(2,'0');
      const year = startDate.getFullYear();

      const dateStr = `${day}-${month}-${year}`;
      url += `?startDate=${dateStr}`;
    }

    const token = this.getAuthToken();

    this.http.get<GetRideDTO[]>(url, {
      headers: {
        Authorization: `Bearer ${token}`,
      }
    }).subscribe({
      next: rides => this._rides.set(rides),
      error: err => console.error('Error loading rides', err)
    });
  }

  private getAuthToken(): string {
    const token = sessionStorage.getItem('authToken') || localStorage.getItem('authToken');
    if (!token) {
      throw new Error('No auth token found');
    }
    return token;
  }

  addRide(ride: GetRideDTO) {
    this._rides.update((rides) => [...rides, ride])
  }

  searchRidesByDate(date: Date) {
    this.loadRides(date);
  }

   resetFilter() {
     this.loadRides();
   }

  getRideById(id: number) {
    return this.http.get<GetRideDTO>(
      `${environment.apiHost}/api/passenger/rides/${id}`,
      {
        headers: {
          Authorization: `Bearer ${this.getAuthToken()}`
        }
      }
    );
  }

  getInconsistencyReports(rideId: number) {
    return this.http.get<GetInconsistencyReportDTO[]>(`${environment.apiHost}/api/completed-rides/${rideId}/inconsistencies`);
  }

  favoriteRide(rideId: number): Observable<CreatedFavoriteDTO> {
    return this.http.post<CreatedFavoriteDTO>(
      `${environment.apiHost}/api/rides/${rideId}/favorite`,
      {},
      {
        headers: {
          Authorization: `Bearer ${this.getAuthToken()}`,
        }
      }
    );
  }
}
