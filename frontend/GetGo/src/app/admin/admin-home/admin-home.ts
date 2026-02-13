import { Component, AfterViewInit, OnDestroy, ViewChild, OnInit, ChangeDetectorRef } from '@angular/core';
import { AdminNavBarComponent } from '../../layout/admin-nav-bar/admin-nav-bar.component';
import { MapComponent } from '../../layout/map/map.component';
import { WebSocketService } from '../../service/websocket/websocket.service';
import { SnackBarService } from '../../service/snackBar/snackBar.service';
import { PanicService } from '../service/panic.service';
import { PanicAlertDTO } from '../../model/panic/panic-alert.model';
import { Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';
import { RideService } from '../../service/ride/ride.service';

export interface PanicMarker {
  rideId: number;
  lat: number;
  lng: number;
  type: 'passenger' | 'driver';
  count: number;
}

@Component({
  selector: 'app-admin-home',
  standalone: true,
  imports: [AdminNavBarComponent, MapComponent, CommonModule],
  templateUrl: './admin-home.html',
  styleUrl: './admin-home.css',
})
export class AdminHome implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild(MapComponent) mapComponent?: MapComponent;

  private panicSubscription?: Subscription;

  allPanicAlerts: PanicAlertDTO[] = [];
  isPanelOpen = false;
  unreadCount = 0;
  panicMarkers: PanicMarker[] = [];
  private rideLocationSubscriptions = new Map<number, Subscription>();
  // No longer need separate rideCarMarkers - handled by MapComponent

  constructor(
    private webSocketService: WebSocketService,
    private snackBarService: SnackBarService,
    private panicService: PanicService,
    private rideService: RideService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.panicSubscription = this.webSocketService
      .subscribeToAdminPanic()
      .subscribe((payload: PanicAlertDTO) => {
        setTimeout(() => {
          this.allPanicAlerts.unshift(payload);
          this.updateUnreadCount();
          this.subscribeToRideLocation(payload.rideId);
          this.snackBarService.show(
            `PANIC: Ride #${payload.rideId} triggered by user #${payload.triggeredByUserId}`
          );
          this.cdr.detectChanges();
        }, 0);
      });

    this.loadAllPanics();
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.updatePanicMarkers();
    }, 500);
  }

  ngOnDestroy(): void {
    if (this.panicSubscription) {
      this.panicSubscription.unsubscribe();
    }
    this.rideLocationSubscriptions.forEach(sub => sub.unsubscribe());
    this.rideLocationSubscriptions.clear();
  }

  togglePanel(): void {
    this.isPanelOpen = !this.isPanelOpen;
  }

  loadAllPanics(): void {
    this.panicService.getUnreadPanics().subscribe({
      next: (alerts) => {
        this.allPanicAlerts = alerts;
        this.updateUnreadCount();

        // Only subscribe to locations if WebSocket is connected
        if (this.webSocketService.connectionStatus) {
          this.updatePanicMarkers();
        }
      },
      error: () => {
        this.snackBarService.show('Failed to load panic alerts');
      }
    });
  }

  markAsRead(alert: PanicAlertDTO, event?: Event): void {
    if (event) {
      event.stopPropagation();
    }

    if (alert.status) {
      return;
    }

    this.panicService.markRead(alert.panicId).subscribe({
      next: () => {
        setTimeout(() => {
          alert.status = true;
          this.updateUnreadCount();
          this.updatePanicMarkers();
          this.snackBarService.show('Panic alert marked as read');
          this.cdr.detectChanges();
        }, 0);
      },
      error: () => {
        this.snackBarService.show('Failed to mark as read');
      }
    });
  }

  markAllAsRead(): void {
    this.panicService.markAllRead().subscribe({
      next: () => {
        setTimeout(() => {
          this.allPanicAlerts.forEach(a => a.status = true);
          this.updateUnreadCount();
          this.updatePanicMarkers();
          this.snackBarService.show('All panic alerts marked as read');
          this.cdr.detectChanges();
        }, 0);
      },
      error: () => {
        this.snackBarService.show('Failed to mark all as read');
      }
    });
  }

  private updateUnreadCount(): void {
    this.unreadCount = this.allPanicAlerts.filter(a => !a.status).length;
  }

  private subscribeToRideLocation(rideId: number): void {
    if (this.rideLocationSubscriptions.has(rideId)) {
      return;
    }

    if (!this.webSocketService.connectionStatus) {
      console.warn(`Cannot subscribe to ride ${rideId}: WebSocket not connected`);
      return;
    }

    const sub = this.webSocketService
      .subscribeToRideDriverLocation(rideId)
      .subscribe({
        next: (location: any) => {
          // First update car position (car marker moves)
          if (this.mapComponent) {
            this.mapComponent.updateCarMarker(rideId, location.latitude, location.longitude);
          }

          // Then update panic badge on that car (badge follows car automatically)
          this.updatePanicMarkerLocation(rideId, location.latitude, location.longitude);
        },
        error: (err) => {
          console.error(`Failed to track ride ${rideId}:`, err);
        }
      });

    this.rideLocationSubscriptions.set(rideId, sub);
  }

  private updatePanicMarkerLocation(rideId: number, lat: number, lng: number): void {
    const unreadAlerts = this.allPanicAlerts.filter(a => !a.status && a.rideId === rideId);

    if (unreadAlerts.length === 0) {
      // Remove panic badge (switch car back to normal icon)
      if (this.mapComponent) {
        this.mapComponent.removePanicMarker(rideId);
      }
      return;
    }

    const passengerCount = unreadAlerts.filter(a => {
      const role = a.role || (a.triggeredByUserId % 2 === 0 ? 'passenger' : 'driver');
      return role === 'passenger';
    }).length;

    const driverCount = unreadAlerts.filter(a => {
      const role = a.role || (a.triggeredByUserId % 2 === 0 ? 'passenger' : 'driver');
      return role === 'driver';
    }).length;

    // Update car with panic badge overlay
    if (this.mapComponent) {
      this.mapComponent.updatePanicMarker(rideId, lat, lng, passengerCount, driverCount);
    }
  }

  private updatePanicMarkers(): void {
    const unreadAlerts = this.allPanicAlerts.filter(a => !a.status);

    if (unreadAlerts.length === 0) {
      // Remove all panic markers
      if (this.mapComponent) {
        this.mapComponent.clearAllPanicMarkers();
      }

      this.rideLocationSubscriptions.forEach(sub => sub.unsubscribe());
      this.rideLocationSubscriptions.clear();
      return;
    }

    const uniqueRideIds = [...new Set(unreadAlerts.map(a => a.rideId))];
    uniqueRideIds.forEach(rideId => this.subscribeToRideLocation(rideId));

    const obsoleteRides = Array.from(this.rideLocationSubscriptions.keys())
      .filter(rideId => !uniqueRideIds.includes(rideId));

    obsoleteRides.forEach(rideId => {
      this.rideLocationSubscriptions.get(rideId)?.unsubscribe();
      this.rideLocationSubscriptions.delete(rideId);
      if (this.mapComponent) {
        this.mapComponent.removePanicMarker(rideId);
      }
    });
  }

  formatTime(timestamp: string): string {
    return new Date(timestamp).toLocaleString();
  }
}
