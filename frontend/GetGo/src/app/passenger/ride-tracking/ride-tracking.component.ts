import { Component, inject, computed, effect, OnInit, OnDestroy, ViewChild, ElementRef, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RideTrackingMapComponent } from '../../layout/ride-tracking-map/ride-tracking-map.component';
import { RideTrackingService } from '../../service/ride-tracking/ride-tracking.service';
import {
  RideService,
  GetPassengerActiveRideDTO,
  DriverLocationDTO,
  PassengerStatusUpdateDTO,
  PassengerRideFinishedDTO
} from '../../service/ride/ride.service';
import { WebSocketService } from '../../service/websocket/websocket.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-ride-tracking',
  standalone: true,
  imports: [CommonModule, RideTrackingMapComponent, FormsModule],
  templateUrl: './ride-tracking.component.html',
  styleUrl: './ride-tracking.component.css',
})
export class RideTrackingComponent implements OnInit, OnDestroy {
  private rideTrackingService = inject(RideTrackingService);
  private rideService = inject(RideService);
  private webSocketService = inject(WebSocketService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  showReportForm = false;
  reportText = '';
  showCancelForm = false;

  // Ride data
  activeRide: GetPassengerActiveRideDTO | null = null;
  driverLocation: { lat: number; lng: number } | null = null;
  rideCompletion: PassengerRideFinishedDTO | null = null;

  isLoading = true;
  errorMessage: string | null = null;
  statusMessage: string = '';

  // WebSocket subscriptions
  private locationSubscription?: Subscription;
  private statusSubscription?: Subscription;
  private completionSubscription?: Subscription;

  @ViewChild(RideTrackingMapComponent, { read: ElementRef, static: false })
  private mapComponent?: ElementRef<HTMLElement>;

  // Keep for backward compatibility with template
  readonly tracking = this.rideTrackingService.tracking;
  readonly loading = this.rideTrackingService.loading;

  async ngOnInit() {
    try {
      // Connect to WebSocket
      await this.webSocketService.connect();
      console.log('WebSocket connected');

      // Load active ride
      this.loadActiveRide();

    } catch (error) {
      console.error('Failed to connect to WebSocket:', error);
      this.errorMessage = 'Failed to connect to real-time updates';
      this.isLoading = false;
    }
  }

  ngOnDestroy() {
    // Clean up subscriptions
    if (this.locationSubscription) this.locationSubscription.unsubscribe();
    if (this.statusSubscription) this.statusSubscription.unsubscribe();
    if (this.completionSubscription) this.completionSubscription.unsubscribe();

    this.webSocketService.disconnect();
  }

  loadActiveRide() {
    console.log('Loading active ride...');
    this.isLoading = true;
    this.errorMessage = null;

    this.rideService.getPassengerActiveRide().subscribe({
      next: (ride) => {
        if (ride) {
          console.log('Active ride found:', ride);
          this.activeRide = ride;
          this.updateStatusMessage(ride.status);
          this.initializeMapRoute(ride);
          this.subscribeToWebSocketUpdates(ride.rideId);
        } else {
          console.log('No active ride found');
          this.errorMessage = 'No active ride found';
        }
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load active ride:', err);
        this.errorMessage = 'Failed to load ride information';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private subscribeToWebSocketUpdates(rideId: number) {
    // Subscribe to driver location updates
    this.locationSubscription = this.webSocketService
      .subscribeToRideDriverLocation(rideId)
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
        error: (err) => console.error('Error receiving driver location:', err)
      });

    // Subscribe to status updates
    this.statusSubscription = this.webSocketService
      .subscribeToRideStatusUpdates(rideId)
      .subscribe({
        next: (update: PassengerStatusUpdateDTO) => {
          console.log('🔔 Status update received:', update);
          if (this.activeRide) {
            this.activeRide.status = update.status;
            this.statusMessage = update.message;
          }
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Error receiving status update:', err)
      });

    // Subscribe to ride completion
    this.completionSubscription = this.webSocketService
      .subscribeToPassengerRideFinished(rideId)
      .subscribe({
        next: (completion: PassengerRideFinishedDTO) => {
          console.log('🎉 Ride finished:', completion);
          this.rideCompletion = completion;
          if (this.activeRide) {
            this.activeRide.status = 'FINISHED';
          }
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Error receiving completion:', err)
      });
  }

  private initializeMapRoute(ride: GetPassengerActiveRideDTO) {
    if (!this.mapComponent?.nativeElement || !ride.latitudes || !ride.longitudes) {
      console.log('Cannot initialize map: missing data');
      return;
    }

    console.log('Initializing map route');

    const waypoints = ride.latitudes.map((lat, index) => ({
      lat: lat,
      lng: ride.longitudes[index]
    }));

    const event = new CustomEvent('update-route', {
      detail: { waypoints },
      bubbles: true
    });
    this.mapComponent.nativeElement.dispatchEvent(event);
  }

  private updateDriverMarkerOnMap(lat: number, lng: number) {
    if (!this.mapComponent?.nativeElement) return;

    const event = new CustomEvent('update-driver-position', {
      detail: { lat, lng },
      bubbles: true
    });
    this.mapComponent.nativeElement.dispatchEvent(event);
  }

  private updateStatusMessage(status: string) {
    switch (status) {
      case 'DRIVER_FINISHING_PREVIOUS_RIDE':
        this.statusMessage = 'Driver is finishing their current ride. Please wait...';
        break;
      case 'DRIVER_READY':
        this.statusMessage = 'Driver is ready! Waiting to start...';
        break;
      case 'DRIVER_INCOMING':
        this.statusMessage = 'Driver is on the way to pick you up!';
        break;
      case 'DRIVER_ARRIVED':
        this.statusMessage = 'Driver has arrived at pickup location!';
        break;
      case 'ACTIVE':
        this.statusMessage = 'Ride in progress!';
        break;
      case 'FINISHED':
        this.statusMessage = 'Ride completed!';
        break;
      default:
        this.statusMessage = '';
    }
  }

  progressPercent(): number {
    // TODO: give up on this or finish
    return 42;
  }

  submitReport() {
    if (!this.reportText.trim()) return;

    const textToSend = this.reportText;
    this.showReportForm = false;
    this.reportText = '';

    console.log("Submitting report:", textToSend);

    this.rideTrackingService.createInconsistencyReport({ text: textToSend })
      .subscribe({
        next: (response) => {
          console.log("Report saved", response);
        },
        error: (error) => {
          console.error("Error saving report", error);
          // In case of error, show the form again with the text
          this.reportText = textToSend;
          this.showReportForm = true;
        }
      });
  }

  submitPanic(): void {
    if (!this.activeRide) {
      console.warn('No active ride to trigger panic for');
      return;
    }

    this.rideTrackingService.createPanicAlert().subscribe({
      next: () => {
        console.log('PANIC alert sent');
        alert('Emergency alert sent! Help is on the way.');
      },
      error: (err) => {
        console.error('Failed to send PANIC', err);
        alert('Failed to send emergency alert. Please call emergency services.');
      }
    });
  }

  acknowledgeCompletion() {
    console.log('Acknowledging ride completion');
    this.rideCompletion = null;
    this.activeRide = null;
    this.router.navigate(['/registered-home']);
  }
  submitCancel(): void {
    if (!this.activeRide) {
      console.error('No active ride to cancel');
      return;
    }

    this.showCancelForm = false;

    console.log('Cancelling ride as passenger without explicit reason');
    this.rideService
      .cancelRideByPassenger(this.activeRide.rideId, { reason: '' })
      .subscribe({
        next: () => {
          console.log('Ride cancelled successfully');
          this.activeRide = null;
          this.rideCompletion = null;
          this.statusMessage = 'Ride has been cancelled.';
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error cancelling ride', error);
          this.cdr.detectChanges();
        }
      });
  }

  canShowCancelRide(): boolean {
    if (!this.activeRide) return false;

    const status = (this.activeRide.status || '').toUpperCase();

    if (status === 'ACTIVE' || status === 'FINISHED') {
      return false;
    }
    return true;
  }
}
