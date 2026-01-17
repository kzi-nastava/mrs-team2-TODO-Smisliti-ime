import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { rxResource } from '@angular/core/rxjs-interop';
import { environment } from '../../../env/environment';
import { RideTracking } from '../../model/ride-tracking.model';
import { Observable, tap } from 'rxjs';
import { CreateInconsistencyReportDTO, CreatedInconsistencyReportDTO } from '../../model/inconsistency-report.model';
import {User} from '../../model/user.model';

@Injectable({
  providedIn: 'root',
})
export class RideTrackingService {
  private readonly http = inject(HttpClient);

  private rideId = signal<number | null> (null);

  // resource koji se automatski refetchuje kad se rideId promeni
  trackingResource = rxResource({
    params: () => {
      const id = this.rideId();
       return id ? { id } : null;
    },
    stream: ({ params }) => {
      const token = localStorage.getItem('authToken');
      return this.http.get<RideTracking>(`${environment.apiHost}/api/rides/${params!.id}/tracking`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          }
        }
        );
    },
  });

  tracking = computed(() => this.trackingResource.value() ?? null);
  // loading signal (spinner)
  loading = computed(() => this.trackingResource.isLoading());

  startTracking(id: number): void {
    this.rideId.set(id);
  }

  createInconsistencyReport(dto: CreateInconsistencyReportDTO): Observable<CreatedInconsistencyReportDTO> {
    const rideId = this.rideId();

    if (!rideId) throw new Error('No active ride ID set');

    const token = localStorage.getItem('authToken');
    return this.http.post<CreatedInconsistencyReportDTO>(`${environment.apiHost}/api/rides/${rideId}/inconsistencies`, dto,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        }
      }
    ).pipe(tap(_ => this.reloadTracking()));
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
}
