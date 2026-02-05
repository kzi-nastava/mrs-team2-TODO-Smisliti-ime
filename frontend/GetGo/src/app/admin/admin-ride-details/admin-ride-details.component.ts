import { Component, signal, OnInit, AfterViewInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AdminRideService } from '../service/admin-ride.service';
import { GetRideDTO } from '../../passenger/model/ride.model';
import { CommonModule } from '@angular/common';
import { GetInconsistencyReportDTO } from '../../model/inconsistency-report.model';
import { GetRatingDTO } from '../../model/rating.model';
import { GetDriverDTO, GetPassengerDTO } from '../../model/user.model';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import * as L from 'leaflet';

@Component({
  selector: 'app-admin-ride-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-ride-details.component.html',
  styleUrl: './admin-ride-details.component.css'
})
export class AdminRideDetailsComponent implements OnInit, AfterViewInit {

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

  constructor(
    private route: ActivatedRoute,
    private adminRideService: AdminRideService
  ) {
    // Log all available data
    console.log('Full route snapshot:', {
      params: this.route.snapshot.params,
      paramMap: Object.fromEntries(this.route.snapshot.paramMap.keys.map(k => [k, this.route.snapshot.paramMap.get(k)])),
      url: this.route.snapshot.url.map(segment => segment.path),
      queryParams: this.route.snapshot.queryParams
    });

    // Try multiple ways to get the parameters
    this.rideId = Number(this.route.snapshot.paramMap.get('id') || this.route.snapshot.params['id']);

    // Try reading userType from different sources
    let userType = this.route.snapshot.paramMap.get('userType') || this.route.snapshot.params['userType'];

    // If still null, try to extract from URL segments
    if (!userType) {
      const urlSegments = this.route.snapshot.url.map(s => s.path);
      console.log('URL segments:', urlSegments);
      // URL structure: ['admin', 'rides', 'passenger', '14']
      const userTypeIndex = urlSegments.indexOf('rides') + 1;
      if (userTypeIndex > 0 && userTypeIndex < urlSegments.length) {
        userType = urlSegments[userTypeIndex];
      }
    }

    this.userType = userType as 'passenger' | 'driver';
    this.userEmail = this.route.snapshot.queryParamMap.get('email') || '';

    console.log('AdminRideDetailsComponent initialized with:', {
      rideId: this.rideId,
      userType: this.userType,
      userEmail: this.userEmail,
      fullUrl: window.location.href
    });
  }

  ngOnInit() {
    if (!this.userEmail) {
      console.error('No email provided');
      this.loadingRide.set(false);
      return;
    }

    if (!this.userType || (this.userType !== 'passenger' && this.userType !== 'driver')) {
      console.error('Invalid userType:', this.userType);
      this.loadingRide.set(false);
      return;
    }

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
    // Wait for the DOM to be ready and ride data to load
    setTimeout(() => {
      const mapElement = document.getElementById('map');
      if (mapElement && this.ride() && this.ride()!.startPoint && this.ride()!.endPoint) {
        this.initializeMap();
      }
    }, 1000);
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
      // Initialize map centered on Novi Sad
      this.map = L.map('map').setView([45.2671, 19.8335], 13);

      // Add OpenStreetMap tiles
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors'
      }).addTo(this.map);

      // Geocode start and end points (simplified - you might want to use a geocoding service)
      // For now, we'll use waypoints if available, or show markers for start/end
      if (currentRide.waypoints && currentRide.waypoints.length > 0) {
        this.drawRouteFromWaypoints(currentRide.waypoints);
      } else {
        // If no waypoints, just show start and end markers
        this.geocodeAndDrawRoute(currentRide.startPoint, currentRide.endPoint);
      }
    } catch (error) {
      console.error('Error initializing map:', error);
    }
  }

  private drawRouteFromWaypoints(waypoints: Array<{ lat: number; lng: number; timestamp: string }>): void {
    if (!this.map) return;

    const latLngs: L.LatLngExpression[] = waypoints.map(wp => [wp.lat, wp.lng]);

    // Draw polyline
    const polyline = L.polyline(latLngs, {
      color: '#2196F3',
      weight: 4,
      opacity: 0.7
    }).addTo(this.map);

    // Add start marker
    L.marker(latLngs[0], {
      icon: L.icon({
        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
        iconSize: [25, 41],
        iconAnchor: [12, 41],
        popupAnchor: [1, -34],
        shadowSize: [41, 41]
      })
    }).addTo(this.map).bindPopup('Start');

    // Add end marker
    L.marker(latLngs[latLngs.length - 1], {
      icon: L.icon({
        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
        iconSize: [25, 41],
        iconAnchor: [12, 41],
        popupAnchor: [1, -34],
        shadowSize: [41, 41]
      })
    }).addTo(this.map).bindPopup('End');

    // Fit map to show entire route
    this.map.fitBounds(polyline.getBounds(), { padding: [50, 50] });
  }

  private geocodeAndDrawRoute(startPoint: string, endPoint: string): void {
    if (!this.map) return;

    // Simple geocoding using Nominatim (OpenStreetMap)
    const geocodeUrl = (address: string) =>
      `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address + ', Novi Sad, Serbia')}`;

    Promise.all([
      fetch(geocodeUrl(startPoint)).then(r => r.json()),
      fetch(geocodeUrl(endPoint)).then(r => r.json())
    ]).then(([startResults, endResults]) => {
      if (startResults.length > 0 && endResults.length > 0) {
        const startLat = parseFloat(startResults[0].lat);
        const startLng = parseFloat(startResults[0].lon);
        const endLat = parseFloat(endResults[0].lat);
        const endLng = parseFloat(endResults[0].lon);

        // Draw line between start and end
        const latLngs: L.LatLngExpression[] = [[startLat, startLng], [endLat, endLng]];
        const polyline = L.polyline(latLngs, {
          color: '#2196F3',
          weight: 4,
          opacity: 0.7,
          dashArray: '10, 10'
        }).addTo(this.map!);

        // Add markers
        L.marker([startLat, startLng], {
          icon: L.icon({
            iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
            shadowSize: [41, 41]
          })
        }).addTo(this.map!).bindPopup(`Start: ${startPoint}`);

        L.marker([endLat, endLng], {
          icon: L.icon({
            iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
            shadowSize: [41, 41]
          })
        }).addTo(this.map!).bindPopup(`End: ${endPoint}`);

        this.map!.fitBounds(polyline.getBounds(), { padding: [50, 50] });
      }
    }).catch(err => {
      console.error('Error geocoding addresses:', err);
    });
  }

  getAverageRating(): number {
    const ratingsList = this.ratings();
    if (ratingsList.length === 0) return 0;
    const sum = ratingsList.reduce((acc, r) => acc + r.driverRating, 0);
    return sum / ratingsList.length;
  }
}
