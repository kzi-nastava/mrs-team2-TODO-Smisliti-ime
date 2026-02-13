import { Component, OnInit, inject, Signal, signal, computed, WritableSignal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { GetActiveRideAdminDetailsDTO } from '../../model/active-ride.model';
import { ActiveRideService } from '../../service/active-ride/active-ride.service';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { RideTrackingMapComponent } from '../../layout/ride-tracking-map/ride-tracking-map.component';
import { WebSocketService } from '../../service/websocket/websocket.service';
import { OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { ViewChild, ElementRef } from '@angular/core';

@Component({
  selector: 'app-active-ride-details',
  imports: [CommonModule, MatCardModule, MatIconModule, MatDividerModule, RideTrackingMapComponent],
  templateUrl: './active-ride-details.component.html',
  styleUrl: './active-ride-details.component.css',
})

export class ActiveRideDetailsComponent implements OnInit, OnDestroy {
  @ViewChild(RideTrackingMapComponent, { read: ElementRef, static: false })
  private mapComponent?: ElementRef<HTMLElement>;

  rideId!: number;
  ride: WritableSignal<GetActiveRideAdminDetailsDTO | null> = signal(null);
  private locationSubscription?: Subscription;

  private route = inject(ActivatedRoute);
  private rideService = inject(ActiveRideService);
  private websocketService = inject(WebSocketService);
  private cdr = inject(ChangeDetectorRef);

  async ngOnInit() {
    this.rideId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadRideDetails();

    try {
      this.locationSubscription = this.websocketService
        .subscribeToRideDriverLocation(this.rideId)
        .subscribe((pos: { latitude: number, longitude: number }) => {
          console.log('WS update:', pos);
          const currentRide = this.ride();
          if (currentRide) {
            this.ride.set({
              ...currentRide,
              currentLat: pos.latitude,
              currentLng: pos.longitude
            });
          }

          if (this.mapComponent?.nativeElement) {
            console.log('Dispatching update-driver-position event');
            const event = new CustomEvent('update-driver-position', {
              detail: { lat: pos.latitude, lng: pos.longitude },
              bubbles: true
            });
            this.mapComponent.nativeElement.dispatchEvent(event);
          }
            this.cdr.detectChanges();
    });

        } catch (err) {
          console.error('Failed to connect WebSocket for admin', err);
        }
  }

  loadRideDetails() {
    this.rideService.getActiveRideDetails(this.rideId).subscribe({
      next: (data: GetActiveRideAdminDetailsDTO) => {
        this.ride.set({
          ...data,
          currentLat: data.currentLat ?? data.latitudes?.[0] ?? 0,
          currentLng: data.currentLng ?? data.longitudes?.[0] ?? 0
        });
      },
      error: (err: any) => console.error('Error loading ride details', err),
    });
  }

  // Computed for map waypoints
  rideWaypoints = computed(() => {
    const r = this.ride();
    if (!r || !r.latitudes || !r.longitudes) return [];
    const wps = r.latitudes.map((lat, i) => ({ lat, lng: r.longitudes![i] }));
    console.log('Ride waypoints:', wps);
    return wps;
  });

  driverPosition = computed(() => {
    const r = this.ride();
    if (!r) return null;
    console.log('Driver position:', { lat: r.currentLat, lng: r.currentLng });
    return { lat: r.currentLat, lng: r.currentLng };
  });

  ngOnDestroy() {
    if (this.locationSubscription) {
      this.locationSubscription.unsubscribe();
    }
  }
}
