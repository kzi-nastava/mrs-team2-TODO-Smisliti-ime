import { Component, signal, OnInit, AfterViewInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { RideService } from '../service/passenger-ride.service';
import { GetRideDTO } from '../model/ride.model';
import { CommonModule } from '@angular/common';
import { GetInconsistencyReportDTO } from '../../model/inconsistency-report.model';
import { GetRatingDTO } from '../../model/rating.model';
import { GetDriverDTO } from '../../model/user.model';
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

      // Geocode start and end points
      if (currentRide.waypoints && currentRide.waypoints.length > 0) {
        this.drawRouteFromWaypoints(currentRide.waypoints);
      } else {
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

    // Simple geocoding using Nominatim
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

        const latLngs: L.LatLngExpression[] = [[startLat, startLng], [endLat, endLng]];
        const polyline = L.polyline(latLngs, {
          color: '#2196F3',
          weight: 4,
          opacity: 0.7,
          dashArray: '10, 10'
        }).addTo(this.map!);

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

  getAverageRating(): number {
    const ratingsList = this.ratings();
    if (ratingsList.length === 0) return 0;
    const sum = ratingsList.reduce((acc, r) => acc + r.driverRating, 0);
    return sum / ratingsList.length;
  }
}
