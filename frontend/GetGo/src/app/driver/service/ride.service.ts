import { Injectable, signal } from '@angular/core';
import { Ride, GetRideDTO } from '../model/ride.model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../env/environment';
import { Signal } from '@angular/core';
import { GetInconsistencyReportDTO } from '../../model/inconsistency-report.model';

@Injectable({
  providedIn: 'root',
})

export class RideService {
  private _rides = signal<GetRideDTO[]>([]);

  rides = this._rides.asReadonly()

  constructor(private http: HttpClient) {}

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

  loadRides(startDate?: Date) {
    let url = `${environment.apiHost}/api/drivers/rides`;

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
//     this.loadRides(driverId, date);
    this.loadRides(date);
  }

   resetFilter() {
//      this.loadRides(driverId);
     this.loadRides();
   }

  getInconsistencyReports(rideId: number) {
    return this.http.get<GetInconsistencyReportDTO[]>(`${environment.apiHost}/completed-rides/${rideId}/inconsistencies`);
  }
}
