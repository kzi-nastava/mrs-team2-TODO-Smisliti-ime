import { Component, Signal, computed, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { RideService } from '../service/passenger-ride.service';
import { GetRideDTO } from '../model/ride.model';
import { CommonModule } from '@angular/common';
import { GetInconsistencyReportDTO } from '../../model/inconsistency-report.model';

@Component({
  selector: 'app-passenger-ride-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ride-details.component.html',
  styleUrl: './ride-details.component.css'
})
export class PassengerRideDetailsComponent {

  rideId!: number;

  ride = signal<GetRideDTO | null>(null);
  loadingRide = signal(true);
  reports = signal<GetInconsistencyReportDTO[]>([]);
  loadingReports = signal(true);

  isFavoriting = signal(false);
  favoriteSuccess = signal(false);
  favoriteError = signal<string | null>(null);

  constructor(
    private route: ActivatedRoute,
    private rideService: RideService
  ) {
    this.rideId = Number(this.route.snapshot.paramMap.get('id'));
  }

  ngOnInit() {
    this.rideService.getRideById(this.rideId).subscribe({
      next: ride => {
        this.ride.set(ride);
        this.loadingRide.set(false);
      },
      error: () => {
        this.loadingRide.set(false);
      }
    });

    this.rideService.getInconsistencyReports(this.rideId).subscribe({
      next: data => {
        this.reports.set(data);
        this.loadingReports.set(false);
      },
      error: () => {
        this.loadingReports.set(false);
      }
    });
  }

  onFavoriteRide(): void {
    this.isFavoriting.set(true);
    this.favoriteSuccess.set(false);
    this.favoriteError.set(null);

    this.rideService.favoriteRide(this.rideId).subscribe({
      next: (response) => {
        console.log('Ride favorited successfully:', response);
        this.favoriteSuccess.set(true);
        this.isFavoriting.set(false);

        // Hide success message after 3 seconds
        setTimeout(() => {
          this.favoriteSuccess.set(false);
        }, 3000);
      },
      error: (error) => {
        console.error('Error favoriting ride:', error);
        this.favoriteError.set(error.error?.message || 'Failed to favorite ride. Please try again.');
        this.isFavoriting.set(false);

        // Hide error message after 5 seconds
        setTimeout(() => {
          this.favoriteError.set(null);
        }, 5000);
      }
    });
  }
}
