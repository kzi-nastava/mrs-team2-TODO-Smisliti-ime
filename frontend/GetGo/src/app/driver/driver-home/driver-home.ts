import { Component, OnInit, OnDestroy, ViewChild, ElementRef, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavBarComponent } from '../../layout/nav-bar/nav-bar.component';
import { RideTrackingMapComponent } from '../../layout/ride-tracking-map/ride-tracking-map.component';
import {
  RideService,
  GetDriverActiveRideDTO,
  UpdatedRideDTO,
  DriverLocationDTO,
  StatusUpdateDTO,
  RideCompletionDTO
} from '../../service/ride/ride.service';
import { WebSocketService } from '../../service/websocket/websocket.service';
import { AuthService } from '../../service/auth-service/auth.service';
import { Subscription } from 'rxjs';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-driver-home',
  standalone: true,
  imports: [CommonModule, NavBarComponent, RideTrackingMapComponent, FormsModule],
  templateUrl: './driver-home.html',
  styleUrl: './driver-home.css',
})
export class DriverHome implements OnInit {
  activeRide: GetDriverActiveRideDTO | null = null;
  driverLocation: { lat: number; lng: number } | null = null;
  rideCompletion: RideCompletionDTO | null = null;

  isLoading = true;
  isStarting = false;
  isAccepting = false;
  isStopping = false;

  errorMessage: string | null = null;
  successMessage: string | null = null;

  // Cancel ride UI state
  isCancelling = false;
  showCancelForm = false;
  cancelReason = '';

  private rideSubscription?: Subscription;
  private locationSubscription?: Subscription;
  private statusSubscription?: Subscription;
  private completionSubscription?: Subscription;

  @ViewChild(RideTrackingMapComponent, { read: ElementRef, static: false })
   private mapComponent?: ElementRef<HTMLElement>;

  constructor(
    private rideService: RideService,
    private webSocketService: WebSocketService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  async ngOnInit() {
    const driverEmail = this.authService.getUserEmail();
    if (!driverEmail) {
      this.errorMessage = 'Failed to get driver email';
      this.isLoading = false;
      return;
    }

    console.log('Driver Email:', driverEmail);

    try {
      await this.webSocketService.connect();
      console.log('WebSocket connected');

      this.subscribeToRideAssignments(driverEmail);
      this.subscribeToLocationUpdates(driverEmail);
      this.subscribeToStatusUpdates(driverEmail);
      this.subscribeToRideCompletion(driverEmail);

      // Load existing active ride (if any)
      this.loadActiveRide();

    } catch (error) {
      console.error('Failed to connect to WebSocket:', error);
      this.errorMessage = 'Failed to connect to real-time updates';
      this.isLoading = false;
    }
  }

  ngOnDestroy() {
    if (this.rideSubscription) this.rideSubscription.unsubscribe();
    if (this.locationSubscription) this.locationSubscription.unsubscribe();
    if (this.statusSubscription) this.statusSubscription.unsubscribe();
    if (this.completionSubscription) this.completionSubscription.unsubscribe();

    this.webSocketService.disconnect();
  }

  private subscribeToRideAssignments(driverEmail: string) {
    this.rideSubscription = this.webSocketService
      .subscribeToDriverRideAssigned(driverEmail)
      .subscribe({
        next: (ride: GetDriverActiveRideDTO) => {
          console.log('New ride assigned via WebSocket:', ride);
          this.activeRide = ride;
          this.isLoading = false;
          this.successMessage = 'New ride assigned!';

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

  private subscribeToLocationUpdates(driverEmail: string) {
    this.locationSubscription = this.webSocketService
      .subscribeToDriverLocation(driverEmail)
      .subscribe({
        next: (location: DriverLocationDTO) => {
          /*console.log('Driver location update:', location);*/
          this.driverLocation = {
            lat: location.latitude,
            lng: location.longitude
          };

          this.updateDriverMarkerOnMap(location.latitude, location.longitude);
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error receiving location update:', err);
        }
      });
  }

  private subscribeToStatusUpdates(driverEmail: string) {
    this.statusSubscription = this.webSocketService
      .subscribeToDriverStatusUpdates(driverEmail)
      .subscribe({
        next: (update: StatusUpdateDTO) => {
          console.log('Status update received:', update);
          if (this.activeRide && this.activeRide.rideId === update.rideId) {
            this.activeRide.status = update.status;
            this.cdr.detectChanges();
          }
        },
        error: (err) => console.error('Error receiving status update:', err)
      });
  }

  private subscribeToRideCompletion(driverEmail: string) {
    this.completionSubscription = this.webSocketService
      .subscribeToRideFinished(driverEmail)
      .subscribe({
        next: (completion: RideCompletionDTO) => {
          console.log('Ride finished:', completion);
          this.rideCompletion = completion;
          if (this.activeRide) {
            this.activeRide.status = 'FINISHED';
          }
          this.successMessage = null;
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Error receiving completion:', err)
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
      console.log('Cannot initialize map: missing map component or coordinates');
      return;
    }

    console.log('Initializing map route');

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

  acceptRide() {
    if (!this.activeRide) return;

    console.log('Accepting ride:', this.activeRide.rideId);
    this.isAccepting = true;
    this.errorMessage = null;

    this.rideService.acceptRide(this.activeRide.rideId).subscribe({
      next: (response: UpdatedRideDTO) => {
        console.log('Ride accepted:', response);
        this.activeRide!.status = 'DRIVER_INCOMING';
        this.isAccepting = false;
        this.successMessage = 'Ride accepted! Moving to pickup...';

        setTimeout(() => {
          this.successMessage = null;
          this.cdr.detectChanges();
        }, 3000);

        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to accept ride:', err);
        this.errorMessage = err.error?.message || 'Failed to accept ride';
        this.isAccepting = false;
        this.cdr.detectChanges();
      }
    });
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

  stopRide() {
    if (!this.activeRide || !this.driverLocation) return;

    this.isStopping = true;
    this.errorMessage = null;

    const payload = {
      latitude: this.driverLocation.lat,
      longitude: this.driverLocation.lng,
      stoppedAt: new Date().toISOString()
    };

    this.rideService.stopRide(this.activeRide.rideId, payload).subscribe({
      next: (completion) => {
        this.rideCompletion = completion;
        this.activeRide!.status = 'FINISHED';
        this.successMessage = 'Ride stopped at passenger request.';
        this.isStopping = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to stop ride.';
        this.isStopping = false;
        this.cdr.detectChanges();
      }
    });
  }

  canShowCancelButton(): boolean {
    if (!this.activeRide) return false;

    const status = (this.activeRide.status || '').toUpperCase();

    if (status === 'ACTIVE' || status === 'FINISHED') {
      return false;
    }

    // Cancel je dozvoljen u READY, INCOMING i ARRIVED fazama
    return status === 'DRIVER_INCOMING' || status === 'DRIVER_ARRIVED' || status === 'DRIVER_READY';
  }

  openCancelForm(): void {
    this.errorMessage = null;
    this.successMessage = null;

    if (!this.activeRide) {
      return;
    }

    const status = (this.activeRide.status || '').toUpperCase();

    if (status === 'DRIVER_READY') {
      if (this.isCancelling) return;

      this.isCancelling = true;
      this.rideService
        .cancelRideByDriver(this.activeRide.rideId, { reason: '' })
        .subscribe({
          next: () => {
            this.successMessage = 'Ride successfully cancelled.';
            this.activeRide = null;
            this.isCancelling = false;
            this.resetMap();
            this.cdr.detectChanges();
          },
          error: (err) => {
            this.errorMessage = err.error?.message || 'Failed to cancel ride.';
            this.isCancelling = false;
            this.cdr.detectChanges();
          }
        });

      return;
    }

    this.cancelReason = '';
    this.showCancelForm = true;
  }

  closeCancelForm(): void {
    if (this.isCancelling) return;
    this.showCancelForm = false;
    this.cancelReason = '';
  }

  confirmCancelRide(): void {
    if (!this.activeRide) {
      return;
    }

    const status = (this.activeRide.status || '').toUpperCase();

    // Za INCOMING/ARRIVED zahtevamo reason kao i do sada
    if (status === 'DRIVER_INCOMING' || status === 'DRIVER_ARRIVED') {
      if (!this.cancelReason.trim()) {
        this.errorMessage = 'Cancellation reason is required.';
        this.cdr.detectChanges();
        return;
      }
    }

    this.isCancelling = true;
    this.errorMessage = null;

    this.rideService
      .cancelRideByDriver(this.activeRide.rideId, { reason: this.cancelReason.trim() || '' })
      .subscribe({
        next: () => {
          this.successMessage = 'Ride successfully cancelled.';
          this.activeRide = null;
          this.showCancelForm = false;
          this.isCancelling = false;
          this.resetMap();
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Failed to cancel ride.';
          this.isCancelling = false;
          this.cdr.detectChanges();
        }
      });
  }

  acknowledgeCompletion() {
    console.log('Acknowledging ride completion');
    this.rideCompletion = null;
    this.activeRide = null;
    this.resetMap();
    this.loadActiveRide(); // Check for next ride
  }

  private resetMap(): void {
    if (!this.mapComponent?.nativeElement) {
      console.warn('Map component not available for reset');
      return;
    }

    console.log('Resetting map...');

    const event = new CustomEvent('reset-map', {
      bubbles: true
    });
    this.mapComponent.nativeElement.dispatchEvent(event);
  }
}
