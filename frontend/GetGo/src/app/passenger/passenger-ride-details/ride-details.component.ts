import { Component, signal, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
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
export class PassengerRideDetailsComponent implements OnInit {

  rideId!: number;

  ride = signal<GetRideDTO | undefined>(undefined);
  loadingRide = signal(true);
  reports = signal<GetInconsistencyReportDTO[]>([]);
  loadingReports = signal(true);

  isFavoriting = signal(false);
  isUnfavoriting = signal(false);
  favoriteSuccess = signal(false);
  unfavoriteSuccess = signal(false);
  favoriteError = signal<string | null>(null);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private rideService: RideService
  ) {
    this.rideId = Number(this.route.snapshot.paramMap.get('id'));
  }

  ngOnInit() {
    // Fetch ride details directly from backend
    this.rideService.getRideById(this.rideId).subscribe({
      next: (ride) => {
        this.ride.set(ride);
        this.loadingRide.set(false);
      },
      error: (err) => {
        console.error('Error loading ride details:', err);
        this.loadingRide.set(false);
      }
    });

    // Fetch inconsistency reports
    this.rideService.getInconsistencyReports(this.rideId).subscribe({
      next: (data) => {
        this.reports.set(data);
        this.loadingReports.set(false);
      },
      error: (err) => {
        console.error('Error loading reports:', err);
        this.loadingReports.set(false);
      }
    });
  }

  onFavoriteRide(): void {
    this.isFavoriting.set(true);
    this.favoriteSuccess.set(false);
    this.unfavoriteSuccess.set(false);
    this.favoriteError.set(null);

    this.rideService.favoriteRide(this.rideId).subscribe({
      next: (response) => {
        console.log('Ride favorited successfully:', response);
        this.favoriteSuccess.set(true);
        this.isFavoriting.set(false);

        setTimeout(() => {
          this.favoriteSuccess.set(false);
        }, 3000);
      },
      error: (error) => {
        console.error('Error favoriting ride:', error);
        this.favoriteError.set(error.error?.message || 'Failed to favorite ride. Please try again.');
        this.isFavoriting.set(false);

        setTimeout(() => {
          this.favoriteError.set(null);
        }, 5000);
      }
    });
  }

  onUnfavoriteRide(): void {
    this.isUnfavoriting.set(true);
    this.favoriteSuccess.set(false);
    this.unfavoriteSuccess.set(false);
    this.favoriteError.set(null);

    this.rideService.unfavoriteRide(this.rideId).subscribe({
      next: () => {
        console.log('Ride unfavorited successfully');
        this.unfavoriteSuccess.set(true);
        this.isUnfavoriting.set(false);

        setTimeout(() => {
          this.unfavoriteSuccess.set(false);
        }, 3000);
      },
      error: (error) => {
        console.error('Error unfavoriting ride:', error);
        this.favoriteError.set(error.error?.message || 'Failed to unfavorite ride. Please try again.');
        this.isUnfavoriting.set(false);

        setTimeout(() => {
          this.favoriteError.set(null);
        }, 5000);
      }
    });
  }

  onRebookRide(): void {
    const currentRide = this.ride();
    if (!currentRide) return;

    this.router.navigate(['/registered-home'], {
      queryParams: {
        from: currentRide.startPoint,
        to: currentRide.endPoint
      }
    });
  }
}
