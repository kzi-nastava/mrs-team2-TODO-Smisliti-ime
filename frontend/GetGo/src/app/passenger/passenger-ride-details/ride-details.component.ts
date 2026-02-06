import { Component, signal, OnInit, AfterViewInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { RideService } from '../service/passenger-ride.service';
import { GetRideDTO } from '../../model/ride.model';
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

      console.log('FULL RIDE OBJECT:', currentRide);
      console.log('ROUTE FIELD:', currentRide.route);

      // Use route data if available
      if (currentRide.route) {
        console.log('Drawing route from route object');
        // Check if route is an array or single object
        const routeArray = Array.isArray(currentRide.route) ? currentRide.route : [currentRide.route];
        this.drawRouteFromRouteData(routeArray);
      } else {
        console.log('Drawing route using geocoding');
        this.geocodeAndDrawRoute(currentRide.startPoint, currentRide.endPoint);
      }
    } catch (error) {
      console.error('Error initializing map:', error);
    }
  }

  private drawRouteFromRouteData(route: any[]): void {
    if (!this.map) return;

    const allLatLngs: L.LatLngExpression[] = [];
    const colors = ['#2196F3', '#4CAF50', '#FF9800', '#E91E63', '#9C27B0'];

    route.forEach((routePoint, index) => {
      console.log('Processing route point:', routePoint);
      console.log('Encoded polyline:', routePoint.encodedPolyline);

      // Parse polyline - check if it's JSON or encoded string
      let decodedPath: L.LatLngExpression[];

      try {
        // Try parsing as JSON array first
        const jsonCoords = JSON.parse(routePoint.encodedPolyline);
        console.log('Parsed JSON coords:', jsonCoords);

        // Backend sends [longitude, latitude] but Leaflet needs [latitude, longitude]
        decodedPath = jsonCoords.map(
          (coord: number[]) => {
            if (coord.length === 2 && typeof coord[0] === 'number' && typeof coord[1] === 'number') {
              return [coord[1], coord[0]] as L.LatLngExpression; // Swap lon/lat to lat/lon
            }
            console.error('Invalid coordinate:', coord);
            return [0, 0] as L.LatLngExpression;
          }
        ).filter((coord: L.LatLngExpression) => {
          const [lat, lng] = coord as number[];
          return lat !== 0 || lng !== 0;
        });

        console.log('Decoded path (first 3):', decodedPath.slice(0, 3));
      } catch (e) {
        console.error('JSON parsing failed, trying encoded polyline:', e);
        // If JSON parsing fails, try encoded polyline
        decodedPath = this.decodePolyline(routePoint.encodedPolyline);
      }

      if (decodedPath.length === 0) {
        console.error('No valid coordinates decoded for route segment');
        return;
      }

      allLatLngs.push(...decodedPath);

      // Draw polyline for this segment
      const polyline = L.polyline(decodedPath, {
        color: colors[index % colors.length],
        weight: 4,
        opacity: 0.7
      }).addTo(this.map!);

      // Add waypoint markers
      if (routePoint.waypoints && routePoint.waypoints.length > 0) {
        console.log('Adding waypoint markers:', routePoint.waypoints);
        routePoint.waypoints.forEach((wp: any, wpIndex: number) => {
          const isFirst = wpIndex === 0;
          const isLast = wpIndex === routePoint.waypoints.length - 1;

          // Waypoints should already be in correct format {lat, lng}
          const lat = wp.latitude || wp.lat;
          const lng = wp.longitude || wp.lng;

          if (typeof lat !== 'number' || typeof lng !== 'number') {
            console.error('Invalid waypoint:', wp);
            return;
          }

          if (isFirst || isLast) {
            L.marker([lat, lng], {
              icon: L.icon({
                iconUrl: isFirst
                  ? 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png'
                  : 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
                shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
                iconSize: [25, 41],
                iconAnchor: [12, 41],
                popupAnchor: [1, -34],
                shadowSize: [41, 41]
              })
            }).addTo(this.map!).bindPopup(isFirst ? routePoint.startingPoint : routePoint.endingPoint);
          }
        });
      }
    });

    // Fit map to show entire route
    if (allLatLngs.length > 0) {
      console.log('Fitting map bounds to:', allLatLngs.length, 'coordinates');
      const bounds = L.latLngBounds(allLatLngs);
      this.map.fitBounds(bounds, { padding: [50, 50] });
    } else {
      console.error('No coordinates to fit map bounds');
    }
  }

  private decodePolyline(encoded: string): L.LatLngExpression[] {
    const poly: L.LatLngExpression[] = [];
    let index = 0, len = encoded.length;
    let lat = 0, lng = 0;

    while (index < len) {
      let b, shift = 0, result = 0;
      do {
        b = encoded.charCodeAt(index++) - 63;
        result |= (b & 0x1f) << shift;
        shift += 5;
      } while (b >= 0x20);
      const dlat = ((result & 1) ? ~(result >> 1) : (result >> 1));
      lat += dlat;

      shift = 0;
      result = 0;
      do {
        b = encoded.charCodeAt(index++) - 63;
        result |= (b & 0x1f) << shift;
        shift += 5;
      } while (b >= 0x20);
      const dlng = ((result & 1) ? ~(result >> 1) : (result >> 1));
      lng += dlng;

      poly.push([lat / 1e5, lng / 1e5]);
    }
    return poly;
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
}
