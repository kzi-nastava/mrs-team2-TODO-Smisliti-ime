import { Injectable, signal, inject, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, of } from 'rxjs';
import { GetRatingDTO } from '../../model/rating.model';
import { environment } from '../../../env/environment';
import {rxResource} from '@angular/core/rxjs-interop';

@Injectable({
  providedIn: 'root',
})
export class RatingService {

  private TOKEN_KEY = 'authToken';

  private readonly http = inject(HttpClient);

  private rideId = signal<number | null>(null);
  private driverId = signal<number | null>(null);
  private token: string | null = null;

  ratingsResource = rxResource({
    params: () => ({ driverId: this.driverId() }),
    stream: ({params}) => {
      if (params.driverId === null) return of([]);
      return this.http.get<GetRatingDTO[]>(
        `${environment.apiHost}/api/ratings/driver/${params.driverId}`
      );
    }
  });


//   ratingsResource = rxResource({
//       params: () => ({ rideId: this.rideId() }),
//       stream: ({params}) => {
//         return this.http.get<GetRatingDTO[]>(
//           `${environment.apiHost}/api/ratings/ride/${params.rideId}`
//         );
//       }
//     })

  ratings = computed(() => {
    const rs = this.ratingsResource.value() ?? []
    return [...rs].sort((a, b) => b.id - a.id);});

  avgVehicleRating = computed(() => {
    const rs = this.ratings();
    return rs.length
      ? rs.reduce((s, r) => s + r.vehicleRating, 0) / rs.length
      : 0;
  });

  avgDriverRating = computed(() => {
    const rs = this.ratings();
    return rs.length
      ? rs.reduce((s, r) => s + r.driverRating, 0) / rs.length
      : 0;
  });

  setDriver(id: number): void {
    this.driverId.set(id);
    this.reloadRatings();
  }

  createRating(rating: { driverRating: number, vehicleRating: number, comment: string, rideId: number }): Observable<GetRatingDTO> {
//     const token = localStorage.getItem(this.TOKEN_KEY);
    const token = sessionStorage.getItem(this.TOKEN_KEY);
    return this.http.post<GetRatingDTO>(
      `${environment.apiHost}/api/ratings?rideId=${rating.rideId}`,
      rating,
      { headers: { Authorization: `Bearer ${token}` } }
    ).pipe(tap(_ => this.reloadRatings()));
  }

  reloadRatings(): void {
    this.ratingsResource.reload();

  }
}
