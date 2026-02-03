import { Component, Signal, computed, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { RideService, GetActiveRideDTO } from '../service/ride.service';
import { CommonModule } from '@angular/common';
import { DriverNavBarComponent } from '../../layout/driver-nav-bar/driver-nav-bar.component';

@Component({
  selector: 'app-scheduled-ride-details',
  standalone: true,
  imports: [CommonModule, DriverNavBarComponent],
  templateUrl: './scheduled-ride-details.html',
  styleUrl: './scheduled-ride-details.css',
})
export class ScheduledRideDetails implements OnInit {
  rideId!: number;
  ride: Signal<GetActiveRideDTO | undefined>;

  constructor(
    private route: ActivatedRoute,
    private rideService: RideService
  ) {
    this.rideId = Number(this.route.snapshot.paramMap.get('id'));

    this.ride = computed(() =>
      this.rideService.scheduledRides().find(r => r.id === this.rideId)
    );
  }

  ngOnInit(): void {
    if (this.rideService.scheduledRides().length === 0) {
      this.rideService.loadScheduledRides();
    }
  }

  getTotalPassengerCount(): number {
    const currentRide = this.ride();
    if (!currentRide) return 0;

    const linkedCount = currentRide.linkedPassengerEmails?.length || 0;
    return 1 + linkedCount;
  }
}
