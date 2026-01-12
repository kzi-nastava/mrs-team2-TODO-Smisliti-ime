import { Injectable, signal } from '@angular/core';
import { Ride, GetRideDTO } from '../model/ride.model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../env/environment';
import { Signal } from '@angular/core';

@Injectable({
  providedIn: 'root',
})

export class RideService {
//   private allRides: Ride[] =
//     [
//       {
//         id: 1,
//         rideId: 101,
//         startDate: '2025.12.10.',
//         startTime: '08:00',
//         endTime: '08:45',
//         startLocation: 'Belgrade, Serbia',
//         endLocation: 'Novi Sad, Serbia',
//         price: 25.50,
//         panicActivated: false,
//         canceledBy: undefined,
//         status: 'FINISHED',
//         passengers: ['Ana Petrović', 'Marko Jovanović']
//       },
//       {
//         id: 2,
//         rideId: 102,
//         startDate: '2025.12.11.',
//         startTime: '09:30',
//         endTime: '10:15',
//         startLocation: 'Belgrade, Serbia',
//         endLocation: 'Smederevo, Serbia',
//         price: 18.75,
//         panicActivated: false,
//         canceledBy: undefined,
//         status: 'FINISHED',
//         passengers: ['Jelena Ilić']
//       },
//       {
//         id: 3,
//         rideId: 103,
//         startDate: '2025.12.11.',
//         startTime: '11:00',
//         endTime: '11:45',
//         startLocation: 'Novi Sad, Serbia',
//         endLocation: 'Subotica, Serbia',
//         price: 40.00,
//         panicActivated: true,
//         canceledBy: undefined,
//         status: 'FINISHED',
//         passengers: ['Ivan Kovačević', 'Sara Đorđević']
//       },
//       {
//         id: 4,
//         rideId: 104,
//         startDate: '2025.12.12.',
//         startTime: '14:00',
//         endTime: '16:00',
//         startLocation: 'Belgrade, Serbia',
//         endLocation: 'Zemun, Serbia',
//         price: 12.00,
//         panicActivated: false,
//         canceledBy: undefined,
//         status: 'IN_PROGRESS',
//         passengers: ['Petar Lukić']
//       },
//       {
//         id: 5,
//         rideId: 105,
//         startDate: '2025.12.12.',
//         startTime: '15:30',
//         endTime: '16:15',
//         startLocation: 'Novi Sad, Serbia',
//         endLocation: 'Belgrade, Serbia',
//         price: 30.00,
//         panicActivated: false,
//         canceledBy: 'PASSENGER',
//         status: 'CANCELED',
//         passengers: ['Milan Stojanović']
//       },
//       {
//         id: 6,
//         rideId: 106,
//         startDate: '2025.12.13.',
//         startTime: '08:15',
//         endTime: '09:00',
//         startLocation: 'Belgrade, Serbia',
//         endLocation: 'Novi Sad, Serbia',
//         price: 26.50,
//         panicActivated: false,
//         canceledBy: undefined,
//         status: 'FINISHED',
//         passengers: ['Ana Petrović']
//       },
//       {
//         id: 7,
//         rideId: 107,
//         startDate: '2025.12.13.',
//         startTime: '10:30',
//         endTime: '11:10',
//         startLocation: 'Smederevo, Serbia',
//         endLocation: 'Belgrade, Serbia',
//         price: 19.00,
//         panicActivated: true,
//         canceledBy: undefined,
//         status: 'FINISHED',
//         passengers: ['Marko Jovanović', 'Jelena Ilić']
//       },
//       {
//         id: 8,
//         rideId: 108,
//         startDate: '2025.12.14.',
//         startTime: '12:00',
//         endTime: '13:20',
//         startLocation: 'Belgrade, Serbia',
//         endLocation: 'Zemun, Serbia',
//         price: 12.00,
//         panicActivated: false,
//         canceledBy: undefined,
//         status: 'IN_PROGRESS',
//         passengers: ['Ivan Kovačević']
//       },
//       {
//         id: 9,
//         rideId: 109,
//         startDate: '2025.12.14.',
//         startTime: '13:15',
//         endTime: '14:00',
//         startLocation: 'Novi Sad, Serbia',
//         endLocation: 'Subotica, Serbia',
//         price: 42.00,
//         panicActivated: false,
//         canceledBy: 'DRIVER',
//         status: 'CANCELED',
//         passengers: ['Sara Đorđević', 'Petar Lukić']
//       },
//       {
//         id: 10,
//         rideId: 110,
//         startDate: '2025.12.15.',
//         startTime: '09:00',
//         endTime: '09:45',
//         startLocation: 'Belgrade, Serbia',
//         endLocation: 'Novi Sad, Serbia',
//         price: 27.50,
//         panicActivated: false,
//         canceledBy: undefined,
//         status: 'FINISHED',
//         passengers: ['Milan Stojanović']
//       },
//       {
//         id: 11,
//         rideId: 111,
//         startDate: '2025.12.15.',
//         startTime: '11:30',
//         endTime: '12:15',
//         startLocation: 'Belgrade, Serbia',
//         endLocation: 'Smederevo, Serbia',
//         price: 20.00,
//         panicActivated: true,
//         canceledBy: undefined,
//         status: 'IN_PROGRESS',
//         passengers: ['Ana Petrović', 'Jelena Ilić']
//       },
//       {
//         id: 12,
//         rideId: 112,
//         startDate: '2025.12.16.',
//         startTime: '08:45',
//         endTime: '09:30',
//         startLocation: 'Novi Sad, Serbia',
//         endLocation: 'Belgrade, Serbia',
//         price: 28.00,
//         panicActivated: false,
//         canceledBy: undefined,
//         status: 'FINISHED',
//         passengers: ['Marko Jovanović', 'Ivan Kovačević']
//       },
//     ];

//   private _rides = signal<Ride[]>(this.allRides);
  private _rides = signal<GetRideDTO[]>([]);

  rides = this._rides.asReadonly()

  constructor(private http: HttpClient) {}

  loadRides(driverId: number, startDate?: Date) {
      let url = `${environment.apiHost}/api/drivers/${driverId}/rides`;
      if (startDate) {
        const dateStr = `${startDate.getFullYear()}-${(startDate.getMonth()+1).toString().padStart(2,'0')}-${startDate.getDate().toString().padStart(2,'0')}`;
        url += `?startDate=${dateStr}`;
      }

      this.http.get<GetRideDTO[]>(url)
        .subscribe({
          next: (rides) => {
            console.log('Rides from API:', rides);
            const mappedRides = rides.map(r => ({...r,panicActivated: false  // default value
            }));
//             this._rides.set(rides);
            this._rides.set(mappedRides);
          },
          error: (err) => console.error("Error loading rides", err)
        });
    }

  addRide(ride: GetRideDTO) {
    this._rides.update((rides) => [...rides, ride])
  }

  searchRidesByDate(driverId: number, date: Date) {
    this.loadRides(driverId, date);
  }

   resetFilter(driverId: number) {
     this.loadRides(driverId);
   }


}
