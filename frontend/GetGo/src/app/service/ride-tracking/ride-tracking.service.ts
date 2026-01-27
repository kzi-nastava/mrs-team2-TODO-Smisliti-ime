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

  createPanicAlert(): Observable<void> {
    const rideId = this.rideId();
    if (!rideId) {
      throw new Error('No active ride ID set');
    }

    const token = this.getAuthToken();

    return this.http.post<void>(
      `${environment.apiHost}/api/rides/${rideId}/panic`,
      {},
      {
        headers: {
          Authorization: `Bearer ${token}`,
        }
      }
    );
  }
}
