import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../env/environment';
import { GetActiveRideAdminDTO } from '../../model/active-ride.model';
import { GetActiveRideAdminDetailsDTO } from '../../model/active-ride.model';

@Injectable({
  providedIn: 'root',
})
export class ActiveRideService {
   private apiUrl = `${environment.apiHost}/api/admin/active-rides`;
   private _activeRides = signal<GetActiveRideAdminDTO[]>([]);

   activeRides = this._activeRides.asReadonly();

   constructor(private http: HttpClient) {}

   loadActiveRides() {
     this.http.get<GetActiveRideAdminDTO[]>(this.apiUrl)
      .subscribe(rides => this._activeRides.set(rides));
   }

   getActiveRideDetails(id: number) {
      return this.http.get<GetActiveRideAdminDetailsDTO>(`${this.apiUrl}/${id}`);
   }

}
