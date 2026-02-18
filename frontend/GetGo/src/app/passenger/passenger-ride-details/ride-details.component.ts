import { Component, signal, OnInit, AfterViewInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { RideService } from '../service/passenger-ride.service';
import { GetRideDTO } from '../../model/ride.model';
import { CommonModule } from '@angular/common';
import { GetInconsistencyReportDTO } from '../../model/inconsistency-report.model';
import { GetRatingDTO } from '../../model/rating.model';
import { GetDriverDTO } from '../../model/user.model';
import { RideHistoryMapHelper } from '../../helpers/ride-history.map.drawing';
import * as L from 'leaflet';

@Component({
  selector: 'app-passenger-ride-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ride-details.component.html',
  styleUrl: './ride-details.component.css'
})
export class PassengerRideDetailsComponent implements OnInit, AfterViewInit {

  rideId!: number;

  ride = signal<GetRideDTO | undefined>(undefined);
  loadingRide = signal(true);
  reports = signal<GetInconsistencyReportDTO[]>([]);
  loadingReports = signal(true);
  ratings = signal<GetRatingDTO[]>([]);
  loadingRatings = signal(true);
  driver = signal<GetDriverDTO | undefined>(undefined);
  loadingDriver = signal(true);

  isFavoriting = signal(false);
  isUnfavoriting = signal(false);
  favoriteSuccess = signal(false);
  unfavoriteSuccess = signal(false);
  favoriteError = signal<string | null>(null);

  private map?: L.Map;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private rideService: RideService,
    private mapHelper: RideHistoryMapHelper
  ) {
    this.rideId = Number(this.route.snapshot.paramMap.get('id'));
  }

  ngOnInit() {
    // Fetch ride details directly from backend
    this.rideService.getRideById(this.rideId).subscribe({
      next: (ride) => {
        this.ride.set(ride);
        this.loadingRide.set(false);

        // Fetch driver profile
        if (ride.driverId) {
          this.rideService.getDriverProfile(ride.driverId).subscribe({
            next: (driver) => {
              this.driver.set(driver);
              this.loadingDriver.set(false);
            },
            error: (err) => {
              console.error('Error loading driver profile:', err);
              this.loadingDriver.set(false);
            }
          });
        }

        // Initialize map after ride data is loaded
        setTimeout(() => {
          if (this.ride()) {
            this.initializeMap();
          }
        }, 500);
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

    // Fetch ratings
    this.rideService.getRatingsByRide(this.rideId).subscribe({
      next: (data) => {
        this.ratings.set(data);
        this.loadingRatings.set(false);
      },
      error: (err) => {
        console.error('Error loading ratings:', err);
        this.loadingRatings.set(false);
      }
    });
  }

  ngAfterViewInit() {
    // Map will be initialized in ngOnInit after data is fetched
  }

  private initializeMap(): void {
    const currentRide = this.ride();
    if (!currentRide) return;

    const mapElement = document.getElementById('map');
    if (!mapElement) {
      console.error('Map element not found');
      return;
    }

    try {
      // Remove existing map if it exists
      if (this.map) {
        this.map.remove();
        this.map = undefined;
      }

      // Initialize map centered on Novi Sad
      this.map = L.map('map').setView([45.2671, 19.8335], 13);

      // Add OpenStreetMap tiles
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors'
      }).addTo(this.map);

      // Use helper to draw route
      this.mapHelper.initializeMap(this.map, currentRide);
    } catch (error) {
      console.error('Error initializing map:', error);
    }
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

    // Extract passenger emails from the ride
    const passengerEmails = currentRide.passengers
      ? currentRide.passengers.map(p => p.email).join(',')
      : '';

    this.router.navigate(['/registered-home'], {
      queryParams: {
        from: currentRide.startPoint,
        to: currentRide.endPoint,
        vehicleType: currentRide.vehicleType,
        babySeats: currentRide.needsBabySeats,
        petFriendly: currentRide.needsPetFriendly,
        passengers: passengerEmails
      }
    });
  }

  getAverageRating(): number {
    const ratingsList = this.ratings();
    if (ratingsList.length === 0) return 0;
    const sum = ratingsList.reduce((acc, r) => acc + r.driverRating, 0);
    return sum / ratingsList.length;
  }

  onRateRide(): void {
    const currentRide = this.ride();
    if (!currentRide) return;

    this.router.navigate(['/rides', currentRide.id, 'rate']);
  }

}
