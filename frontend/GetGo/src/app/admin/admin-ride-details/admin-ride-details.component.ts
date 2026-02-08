import { Component, signal, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AdminRideService } from '../service/admin-ride.service';
import { GetRideDTO } from '../../model/ride.model';
import { CommonModule } from '@angular/common';
import { GetInconsistencyReportDTO } from '../../model/inconsistency-report.model';
import { GetRatingDTO } from '../../model/rating.model';
import { GetDriverDTO, GetPassengerDTO } from '../../model/user.model';
import { forkJoin, of, Subscription } from 'rxjs';
import { catchError } from 'rxjs/operators';
import * as L from 'leaflet';
import { RideHistoryMapHelper } from '../../helpers/ride-history.map.drawing';

@Component({
  selector: 'app-admin-ride-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-ride-details.component.html',
  styleUrl: './admin-ride-details.component.css'
})
export class AdminRideDetailsComponent implements OnInit, AfterViewInit, OnDestroy {

  rideId!: number;
  userEmail!: string;
  userType!: 'passenger' | 'driver';

  ride = signal<GetRideDTO | undefined>(undefined);
  loadingRide = signal(true);
  reports = signal<GetInconsistencyReportDTO[]>([]);
  loadingReports = signal(true);
  ratings = signal<GetRatingDTO[]>([]);
  loadingRatings = signal(true);
  driver = signal<GetDriverDTO | undefined>(undefined);
  passengers = signal<GetPassengerDTO[]>([]);
  loadingProfiles = signal(true);
  private map?: L.Map;

  private routeSubscription?: Subscription;

  constructor(
    private route: ActivatedRoute,
    private adminRideService: AdminRideService,
    private mapHelper: RideHistoryMapHelper
  ) {}

  ngOnInit() {
    // Subscribe to route param changes to handle navigation between different rides
    this.routeSubscription = this.route.paramMap.subscribe(paramMap => {
      this.rideId = Number(paramMap.get('id'));

      // Get userType from URL path parameter
      const userTypeFromParam = paramMap.get('userType');
      console.log('User type from param:', userTypeFromParam);

      if (userTypeFromParam === 'passenger' || userTypeFromParam === 'driver') {
        this.userType = userTypeFromParam;
      } else {
        // Fallback to localStorage if not in URL
        const savedUserType = localStorage.getItem('lastSearchedUserType');
        if (savedUserType === 'passenger' || savedUserType === 'driver') {
          this.userType = savedUserType as 'passenger' | 'driver';
        } else {
          console.error('Invalid userType, defaulting to passenger');
          this.userType = 'passenger';
        }
      }

      // Get email from query params
      this.userEmail = this.route.snapshot.queryParamMap.get('email') || '';

      if (!this.userEmail || this.userEmail.trim() === '') {
        // Fallback to localStorage
        const savedEmail = localStorage.getItem('lastSearchedEmail');
        if (savedEmail) {
          this.userEmail = savedEmail;
          console.log('Using email from localStorage:', this.userEmail);
        } else {
          console.error('No email provided and no saved email in localStorage');
          this.loadingRide.set(false);
          return;
        }
      }

      console.log('AdminRideDetailsComponent initialized:', {
        rideId: this.rideId,
        userType: this.userType,
        userEmail: this.userEmail,
        fullUrl: window.location.href
      });

      if (!this.userType || (this.userType !== 'passenger' && this.userType !== 'driver')) {
        console.error('Invalid userType:', this.userType);
        this.loadingRide.set(false);
        return;
      }

      // Reset all signals before loading new data
      this.resetComponentState();

      // Load new ride data
      this.loadRideData();
    });
  }

  ngOnDestroy() {
    // Clean up subscription
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }

    // Clean up map
    if (this.map) {
      this.map.remove();
      this.map = undefined;
    }
  }

  private resetComponentState() {
    // Reset all signals to initial state
    this.ride.set(undefined);
    this.loadingRide.set(true);
    this.reports.set([]);
    this.loadingReports.set(true);
    this.ratings.set([]);
    this.loadingRatings.set(true);
    this.driver.set(undefined);
    this.passengers.set([]);
    this.loadingProfiles.set(true);

    // Remove existing map
    if (this.map) {
      this.map.remove();
      this.map = undefined;
    }
  }

  private loadRideData() {
    console.log('Fetching ride for userType:', this.userType);

    const getRide$ = this.userType === 'passenger'
      ? this.adminRideService.getPassengerRideById(this.userEmail, this.rideId)
      : this.adminRideService.getDriverRideById(this.userEmail, this.rideId);

    // Fetch ride details
    getRide$.subscribe({
      next: (ride) => {
        this.ride.set(ride);
        this.loadingRide.set(false);

        // Fetch driver profile
        if (ride.driverId) {
          this.adminRideService.getDriverProfile(ride.driverId).subscribe({
            next: (driver) => {
              this.driver.set(driver);
            },
            error: (err) => {
              console.error('Error loading driver profile:', err);
            }
          });
        }

        // Fetch all passenger profiles
        if (ride.passengers && ride.passengers.length > 0) {
          const passengerRequests = ride.passengers.map(p =>
            this.adminRideService.getPassengerProfile(p.id).pipe(
              catchError(err => {
                console.error(`Error loading passenger ${p.id} profile:`, err);
                return of(null);
              })
            )
          );

          forkJoin(passengerRequests).subscribe({
            next: (passengerProfiles) => {
              const validPassengers = passengerProfiles.filter(p => p !== null) as GetPassengerDTO[];
              this.passengers.set(validPassengers);
              this.loadingProfiles.set(false);
            },
            error: (err) => {
              console.error('Error loading passenger profiles:', err);
              this.loadingProfiles.set(false);
            }
          });
        } else {
          this.loadingProfiles.set(false);
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
        this.loadingProfiles.set(false);
      }
    });

    // Fetch reports
    this.adminRideService.getInconsistencyReports(this.rideId).subscribe({
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
    this.adminRideService.getRatingsByRide(this.rideId).subscribe({
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
    // Map will be initialized in loadRideData after data is fetched
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

  getAverageRating(): number {
    const ratingsList = this.ratings();
    if (ratingsList.length === 0) return 0;
    const sum = ratingsList.reduce((acc, r) => acc + r.driverRating, 0);
    return sum / ratingsList.length;
  }
}
