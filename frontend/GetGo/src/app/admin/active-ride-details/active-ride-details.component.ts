import { Component, OnInit, inject, Signal, signal, computed, WritableSignal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { GetActiveRideAdminDetailsDTO } from '../../model/active-ride.model';
import { ActiveRideService } from '../../service/active-ride/active-ride.service';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { RideTrackingMapComponent } from '../../layout/ride-tracking-map/ride-tracking-map.component';

@Component({
  selector: 'app-active-ride-details',
  imports: [CommonModule, MatCardModule, MatIconModule, MatDividerModule, RideTrackingMapComponent],
  templateUrl: './active-ride-details.component.html',
  styleUrl: './active-ride-details.component.css',
})

export class ActiveRideDetailsComponent implements OnInit {
  rideId!: number;
  ride: WritableSignal<GetActiveRideAdminDetailsDTO | null> = signal(null);

  private route = inject(ActivatedRoute);
  private rideService = inject(ActiveRideService);

  ngOnInit(): void {
    this.rideId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadRideDetails();
  }

  loadRideDetails() {
    this.rideService.getActiveRideDetails(this.rideId).subscribe({
      next: (data: GetActiveRideAdminDetailsDTO) => this.ride.set(data),
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
}
