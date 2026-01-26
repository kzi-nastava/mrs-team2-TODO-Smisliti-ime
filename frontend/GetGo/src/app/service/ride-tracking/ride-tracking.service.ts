import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { rxResource } from '@angular/core/rxjs-interop';
import { environment } from '../../../env/environment';
import { RideTracking } from '../../model/ride-tracking.model';
import { Observable, tap, throwError } from 'rxjs';
import { CreateInconsistencyReportDTO, CreatedInconsistencyReportDTO } from '../../model/inconsistency-report.model';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class RideTrackingService {
  private readonly http = inject(HttpClient);

  private rideId = signal<number | null>(null);
  // novi signal: status voznje (iz glavnog ride DTO, ne iz RideTracking)
  private rideStatus = signal<string | null>(null);

  trackingResource = rxResource({
    params: () => {
      const id = this.rideId();
      return id ? { id } : null;
    },
    stream: ({ params }) => {
      const token = this.getAuthToken();
      return this.http.get<RideTracking>(
        `${environment.apiHost}/api/rides/${params!.id}/tracking`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          }
        }
      );
    },
  });

  tracking = computed(() => this.trackingResource.value() ?? null);
  loading = computed(() => this.trackingResource.isLoading());


  startTracking(id: number, status?: string): void {
    this.rideId.set(id);
    if (status) {
      this.rideStatus.set(status);
    }
  }

  getCurrentRideStatus(): string | null {
    return this.rideStatus();
  }

  createInconsistencyReport(dto: CreateInconsistencyReportDTO): Observable<CreatedInconsistencyReportDTO> {
    const rideId = this.rideId();

    if (!rideId) throw new Error('No active ride ID set');

    const token = this.getAuthToken();
    return this.http.post<CreatedInconsistencyReportDTO>(`${environment.apiHost}/api/rides/${rideId}/inconsistencies`, dto,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        }
      }
    ).pipe(tap(_ => this.reloadTracking()));
  }

  private getAuthToken(): string {
    const token = sessionStorage.getItem('authToken') || localStorage.getItem('authToken');
    if (!token) {
      throw new Error('No auth token found');
    }
    return token;
  }


  reloadTracking(): void {
    this.trackingResource.reload();
  };

  private getUserIdFromToken(): string {
    const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    if (!token) throw new Error('No auth token found');

    try {
      const email:string = JSON.parse(atob(token.split('.')[1])).email;
      if (!email) throw new Error('userId not found in token');
      return email;
    } catch (e) {
      console.error('Failed to decode token:', e);
      throw new Error('Invalid token');
    }
  }

  createPanicAlert(): Observable<void> {
    const rideId = this.rideId();
    if (!rideId) {
      throw new Error('No active ride ID set');
    }

    const email = this.getUserIdFromToken();
    const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');

    return this.http.post<void>(
      `${environment.apiHost}/api/rides/${rideId}/panic`,
      { rideId, email },
      {
        headers: {
          Authorization: `Bearer ${token}`,
        }
      }
    );
  }

  cancelRide(reason: string): Observable<any> {
    const rideId = this.rideId();
    if (!rideId) {
      return throwError(() => new Error('No active ride to cancel'));
    }

    const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');

    return this.http.post(
      `${environment.apiHost}/api/rides/${rideId}/cancel`,
      { reason },
      {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      }
    ).pipe(
      tap(() => {
        console.log('Ride cancelled successfully');
        this.rideId.set(null);
        this.rideStatus.set(null);
        this.trackingResource.reload();
      }),
      catchError((error) => {
        console.error('Error cancelling ride:', error);
        return throwError(() => error);
      })
    );
  }
}
