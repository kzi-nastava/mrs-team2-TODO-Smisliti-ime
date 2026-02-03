import { Injectable, signal } from '@angular/core';
import { Ride, GetRideDTO } from '../model/ride.model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../env/environment';
import { Signal } from '@angular/core';
import { GetInconsistencyReportDTO } from '../../model/inconsistency-report.model';
import { Observable } from 'rxjs';

export interface GetActiveRideDTO {
  id: number;
  startingPoint: string;
  endingPoint: string;
  waypointAddresses: string[];
  driverEmail: string;
  driverName: string;
  payingPassengerEmail: string;
  linkedPassengerEmails: string[];
  estimatedPrice: number;
  estimatedDurationMin: number;
  scheduledTime: string;
  actualStartTime: string;
  status: string;
  vehicleType: string;
  needsBabySeats: boolean;
  needsPetFriendly: boolean;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
}


@Injectable({
  providedIn: 'root',
})

export class RideService {
  private apiUrl = `${environment.apiHost}/api/rides`
  private _rides = signal<GetRideDTO[]>([]);
  private _scheduledRides = signal<GetActiveRideDTO[]>([]);

  rides = this._rides.asReadonly()
  scheduledRides = this._scheduledRides.asReadonly();

  constructor(private http: HttpClient) {}

  getAllScheduledRides(): Observable<GetActiveRideDTO[]> {
    return this.http.get<GetActiveRideDTO[]>(`${this.apiUrl}/driver/all-scheduled`);
  }

  loadScheduledRides(): void {
    const token = localStorage.getItem('authToken');
    this.http.get<GetActiveRideDTO[]>(`${this.apiUrl}/driver/all-scheduled`, {
      headers: {
        Authorization: `Bearer ${token}`,
      }
    }).subscribe({
      next: rides => this._scheduledRides.set(rides),
      error: err => console.error('Error loading scheduled rides', err)
    });
  }

  loadRides(page: number = 0, size: number = 5, startDate?: Date): Observable<PageResponse<GetRideDTO>> {
//     let url = `${environment.apiHost}/api/drivers/rides`;
    let params: any = {page, size};

    if (startDate) {
      const day = startDate.getDate().toString().padStart(2,'0');
      const month = (startDate.getMonth() + 1).toString().padStart(2,'0');
      const year = startDate.getFullYear();
      params.startDate = `${day}-${month}-${year}`;

//       const dateStr = `${day}-${month}-${year}`;
//       url += `?startDate=${dateStr}`;
    }

    const token = this.getAuthToken();

    return this.http.get<PageResponse<GetRideDTO>>(
      `${environment.apiHost}/api/drivers/rides`,
      {
        params,
        headers: {
          Authorization: `Bearer ${token}`,
        }
      }
    );
  }

  setRides(rides: GetRideDTO[]) {
    this._rides.set(rides);
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

  getInconsistencyReports(rideId: number) {
    return this.http.get<GetInconsistencyReportDTO[]>(`${environment.apiHost}/api/completed-rides/${rideId}/inconsistencies`);
  }
}
