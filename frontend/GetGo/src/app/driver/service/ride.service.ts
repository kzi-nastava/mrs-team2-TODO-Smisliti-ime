import { Injectable, signal } from '@angular/core';
import { Ride, GetRideDTO } from '../model/ride.model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../env/environment';
import { Signal } from '@angular/core';
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

//   loadRides(driverId: number, startDate?: Date) {
//       let url = `${environment.apiHost}/api/drivers/${driverId}/rides`;
//       if (startDate) {
//         const dateStr = `${startDate.getFullYear()}-${(startDate.getMonth()+1).toString().padStart(2,'0')}-${startDate.getDate().toString().padStart(2,'0')}`;
//         url += `?startDate=${dateStr}`;
//       }
//
//       const token = localStorage.getItem('authToken');
//       this.http.get<GetRideDTO[]>(url,
//         {
//           headers: {
//             Authorization: `Bearer ${token}`,
//           }
//         })
//         .subscribe({
//           next: (rides) => {
//             console.log('Rides from API:', rides);
// //             const mappedRides = rides.map(r => ({...r,panicActivated: false  // default value
// //             }));
//             this._rides.set(rides);
// //             this._rides.set(mappedRides);
//           },
//           error: (err) => console.error("Error loading rides", err)
//         });
//     }

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

  loadRides(startDate?: Date) {
    let url = `${environment.apiHost}/api/drivers/rides`;

    if (startDate) {
      const day = startDate.getDate().toString().padStart(2,'0');
      const month = (startDate.getMonth() + 1).toString().padStart(2,'0');
      const year = startDate.getFullYear();

      const dateStr = `${day}-${month}-${year}`;
      url += `?startDate=${dateStr}`;
    }

    const token = localStorage.getItem('authToken');

    this.http.get<GetRideDTO[]>(url, {
      headers: {
        Authorization: `Bearer ${token}`,
      }
    }).subscribe({
      next: rides => this._rides.set(rides),
      error: err => console.error('Error loading rides', err)
    });
  }


  addRide(ride: GetRideDTO) {
    this._rides.update((rides) => [...rides, ride])
  }

  searchRidesByDate(date: Date) {
//     this.loadRides(driverId, date);
    this.loadRides(date);
  }

   resetFilter() {
//      this.loadRides(driverId);
     this.loadRides();
   }
}
