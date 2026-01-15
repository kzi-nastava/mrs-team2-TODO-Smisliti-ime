import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { rxResource } from '@angular/core/rxjs-interop';
import { environment } from '../../../env/environment';
import { RideTracking } from '../../model/ride-tracking.model';
import { Observable } from 'rxjs';
import { CreateInconsistencyReportDTO, CreatedInconsistencyReportDTO } from '../../model/inconsistency-report.model';

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
      return this.http.get<RideTracking>(`${environment.apiHost}/api/rides/${params!.id}/tracking`);
    },
  });

  tracking = computed(() => this.trackingResource.value() ?? null);

  startTracking(id: number): void {
    this.rideId.set(id);
  }

  createInconsistencyReport(dto: CreateInconsistencyReportDTO): Observable<CreatedInconsistencyReportDTO> {
    const rideId = this.rideId();

    if (!rideId) throw new Error('No active ride ID set');

    return this.http.post<CreatedInconsistencyReportDTO>(`${environment.apiHost}/api/rides/${rideId}/inconsistencies`, dto);
  }

  // loading signal (spinner)
  loading = computed(() => this.trackingResource.isLoading());
}
