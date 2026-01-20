import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavBarComponent } from '../../layout/nav-bar/nav-bar.component';
import { MapComponent } from '../../layout/map/map.component';
import { RideService, GetDriverActiveRideDTO, UpdatedRideDTO } from '../../service/ride/ride.service';

@Component({
  selector: 'app-driver-start-ride',
  standalone: true,
  imports: [CommonModule, NavBarComponent, MapComponent],
  templateUrl: './driver-start-ride.html',
  styleUrls: ['./driver-start-ride.css']
})
export class DriverStartRide implements OnInit {
  activeRide: GetDriverActiveRideDTO | null = null;
  isLoading = false;
  isStarting = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  constructor(private rideService: RideService) {}

  ngOnInit() {
    this.loadActiveRide();
  }

  loadActiveRide() {
    this.isLoading = true;
    this.errorMessage = null;

    this.rideService.getDriverActiveRide().subscribe({
      next: (ride) => {
        this.activeRide = ride;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load active ride:', err);
        this.errorMessage = 'Failed to load ride information';
        this.isLoading = false;
      }
    });
  }

  startRide() {
    if (!this.activeRide) return;

    this.isStarting = true;
    this.errorMessage = null;
    this.successMessage = null;

    this.rideService.startRide(this.activeRide.rideId).subscribe({
      next: (response: UpdatedRideDTO) => {
        console.log('Ride started successfully:', response);
        this.successMessage = 'Ride started successfully!';
        this.isStarting = false;

        // Clear active ride after starting
        setTimeout(() => {
          this.activeRide = null;
          this.successMessage = null;
        }, 2000);
      },
      error: (err) => {
        console.error('Failed to start ride:', err);
        this.errorMessage = err.error?.message || 'Failed to start ride';
        this.isStarting = false;
      }
    });
  }
}
