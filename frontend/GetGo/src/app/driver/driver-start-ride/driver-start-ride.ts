import { Component, OnInit, OnDestroy, ViewChild, ElementRef, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavBarComponent } from '../../layout/nav-bar/nav-bar.component';
import { RideTrackingMapComponent } from '../../layout/ride-tracking-map/ride-tracking-map.component';
import { RideService, GetDriverActiveRideDTO, UpdatedRideDTO, DriverLocationDTO } from '../../service/ride/ride.service';
import { WebSocketService } from '../../service/websocket/websocket.service';
import { AuthService } from '../../service/auth-service/auth.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-driver-start-ride',
  standalone: true,
  imports: [CommonModule, NavBarComponent, RideTrackingMapComponent],
  templateUrl: './driver-start-ride.html',
  styleUrls: ['./driver-start-ride.css']
})
export class DriverStartRide implements OnInit {
  activeRide: GetDriverActiveRideDTO | null = null;
  driverLocation: { lat: number; lng: number } | null = null;
  isLoading = true;
  isStarting = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  private rideSubscription?: Subscription;
  private locationSubscription?: Subscription;
  private driverId: number | null = null;

  @ViewChild(RideTrackingMapComponent, { read: ElementRef, static: false })
   private mapComponent?: ElementRef<HTMLElement>;

  constructor(
    private rideService: RideService,
    private webSocketService: WebSocketService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  async ngOnInit() {
    // Get driver ID from auth
    this.driverId = this.authService.getUserId();

    if (!this.driverId) {
      this.errorMessage = 'Failed to get driver ID';
      this.isLoading = false;
      return;
    }

    console.log('Driver ID:', this.driverId);

    try {
      await this.webSocketService.connect();
      console.log('WebSocket connected');

      // Subscribe to ride assignments
      this.subscribeToRideAssignments();

      // Subscribe to location updates
      this.subscribeToLocationUpdates();

      // Load existing active ride (if any)
      this.loadActiveRide();

    } catch (error) {
      console.error('Failed to connect to WebSocket:', error);
      this.errorMessage = 'Failed to connect to real-time updates';
      this.isLoading = false;
    }
  }

  ngOnDestroy() {
    if (this.rideSubscription) {
      this.rideSubscription.unsubscribe();
    }
    if (this.locationSubscription) {
      this.locationSubscription.unsubscribe();
    }

    this.webSocketService.disconnect();
  }

  private subscribeToRideAssignments() {
    if (!this.driverId) return;

    this.rideSubscription = this.webSocketService
      .subscribeToDriverRideAssigned(this.driverId)
      .subscribe({
        next: (ride: GetDriverActiveRideDTO) => {
          console.log('New ride assigned via WebSocket:', ride);
          this.activeRide = ride;
          this.isLoading = false;
          this.successMessage = 'New ride assigned!';

          // Initialize map with route
          this.initializeMapRoute(ride);

          // Clear success message after 3 seconds
          setTimeout(() => {
            this.successMessage = null;
            this.cdr.detectChanges();
          }, 3000);

          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error receiving ride assignment:', err);
        }
      });
  }

  private subscribeToLocationUpdates() {
    if (!this.driverId) return;

    this.locationSubscription = this.webSocketService
      .subscribeToDriverLocation(this.driverId)
      .subscribe({
        next: (location: DriverLocationDTO) => {
          console.log('Driver location update:', location);
          this.driverLocation = {
            lat: location.latitude,
            lng: location.longitude
          };

          // Update map marker
          this.updateDriverMarkerOnMap(location.latitude, location.longitude);

          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error receiving location update:', err);
        }
      });
  }

  // Load active ride (from before web sockets, maybe not needed anymore but acts as fallback right now)
  loadActiveRide() {
    console.log('Loading active ride via REST API...');
    this.isLoading = true;
    this.errorMessage = null;

    this.rideService.getDriverActiveRide().subscribe({
      next: (ride) => {
        if (ride) {
          console.log('Existing ride found:', ride);
          this.activeRide = ride;
          this.initializeMapRoute(ride);
        } else {
          console.log('ℹNo active ride found');
        }
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load active ride:', err);
        if (err.status !== 404) {
          this.errorMessage = 'Failed to load ride information';
        }
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private initializeMapRoute(ride: GetDriverActiveRideDTO) {
    if (!this.mapComponent?.nativeElement || !ride.latitudes || !ride.longitudes) {
      console.log('⚠️ Cannot initialize map: missing map component or coordinates');
      return;
    }

    console.log('🗺️ Initializing map route');

    const waypoints = ride.latitudes.map((lat, index) => ({
      lat: lat,
      lng: ride.longitudes![index]
    }));

    const event = new CustomEvent('update-route', {
      detail: { waypoints },
      bubbles: true
    });
    this.mapComponent.nativeElement.dispatchEvent(event);
  }

  private updateDriverMarkerOnMap(lat: number, lng: number) {
    if (!this.mapComponent?.nativeElement) {
      return;
    }

    const event = new CustomEvent('update-driver-position', {
      detail: { lat, lng },
      bubbles: true
    });
    this.mapComponent.nativeElement.dispatchEvent(event);
  }

  startRide() {
    if (!this.activeRide) return;

    console.log('Starting ride:', this.activeRide.rideId);
    this.isStarting = true;
    this.errorMessage = null;
    this.successMessage = null;

    this.rideService.startRide(this.activeRide.rideId).subscribe({
      next: (response: UpdatedRideDTO) => {
        console.log('Ride started successfully:', response);
        this.successMessage = 'Ride started successfully!';
        this.isStarting = false;

        // Update ride status
        if (this.activeRide) {
          this.activeRide.status = 'ACTIVE';
        }

        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to start ride:', err);
        this.errorMessage = err.error?.message || 'Failed to start ride. Driver must be at pickup location.';
        this.isStarting = false;
        this.cdr.detectChanges();
      }
    });
  }
}
