import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GetRatingDTO } from '../../model/rating.model';
import { environment } from '../../../env/environment';

@Injectable({
  providedIn: 'root',
})
export class RatingService {

  constructor(private http: HttpClient) { }

  getRatingsByRide(rideId: number): Observable<GetRatingDTO[]> {
    return this.http.get<GetRatingDTO[]>(`${environment.apiHost}/api/ratings/ride/${rideId}`);
  }

  createRating(rating: {driverRating: number, vehicleRating: number, comment: string, rideId: number}) {
    return this.http.post<GetRatingDTO>(`${environment.apiHost}/api/ratings?rideId=${rating.rideId}`, rating);
  }


}
