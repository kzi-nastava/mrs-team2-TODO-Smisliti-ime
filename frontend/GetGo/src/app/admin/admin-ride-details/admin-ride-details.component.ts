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
    private adminRideService: AdminRideService
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

      console.log('FULL RIDE OBJECT:', currentRide);
      console.log('ROUTE FIELD:', currentRide.route);
      console.log('ROUTE LENGTH:', Array.isArray(currentRide.route) ? currentRide.route.length : 'Not an array');

      // Use route data if available
      if (currentRide.route) {
        console.log('Drawing route from route object');
        // Check if route is an array or single object
        const routeArray = Array.isArray(currentRide.route) ? currentRide.route : [currentRide.route];
        this.drawRouteFromRouteData(routeArray);
      } else if (currentRide.waypoints && currentRide.waypoints.length > 0) {
        console.log('Drawing route from waypoints');
        this.drawRouteFromWaypoints(currentRide.waypoints);
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
