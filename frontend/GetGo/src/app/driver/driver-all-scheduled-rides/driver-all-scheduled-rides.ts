import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { RideService, GetActiveRideDTO } from '../service/ride.service';
import { DriverNavBarComponent } from '../../layout/driver-nav-bar/driver-nav-bar.component';

@Component({
  selector: 'app-driver-all-scheduled-rides',
  standalone: true,
  imports: [CommonModule, RouterModule, DriverNavBarComponent],
  templateUrl: './driver-all-scheduled-rides.html',
  styleUrl: './driver-all-scheduled-rides.css',
})
export class DriverAllScheduledRides implements OnInit {
  scheduledRides = signal<GetActiveRideDTO[]>([]);
  isLoading = signal(false);

  constructor(private rideService: RideService) {}

  ngOnInit(): void {
    this.loadScheduledRides();
  }

  loadScheduledRides(): void {
    this.isLoading.set(true);
    this.rideService.getAllScheduledRides().subscribe({
      next: (rides) => {
        this.scheduledRides.set(rides);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error loading scheduled rides:', err);
        this.isLoading.set(false);
      }
    });
  }
}
