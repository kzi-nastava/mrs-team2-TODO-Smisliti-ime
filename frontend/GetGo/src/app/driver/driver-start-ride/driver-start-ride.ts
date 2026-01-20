import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
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
  isLoading = true;  // ✅ Start with true, not false
  isStarting = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  // ✅ FIX: Constructor syntax was wrong
  constructor(
    private rideService: RideService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadActiveRide();
  }

  loadActiveRide() {
    console.log('🔄 Loading active ride...');
    this.isLoading = true;  // ✅ Set to true when loading starts
    this.errorMessage = null;

    this.rideService.getDriverActiveRide().subscribe({
      next: (ride) => {
        console.log('✅ Received ride data:', ride);
        this.activeRide = ride;
        this.isLoading = false;
        this.cdr.detectChanges();  // Force change detection
      },
      error: (err) => {
        console.error('❌ Failed to load active ride:', err);
        this.errorMessage = 'Failed to load ride information';
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      complete: () => {
        console.log('🏁 Request completed');
        // Safety: ensure loading is false
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  startRide() {
    if (!this.activeRide) return;

    console.log('🚀 Starting ride:', this.activeRide.rideId);
    this.isStarting = true;
    this.errorMessage = null;
    this.successMessage = null;

    this.rideService.startRide(this.activeRide.rideId).subscribe({
      next: (response: UpdatedRideDTO) => {
        console.log('✅ Ride started successfully:', response);
        this.successMessage = 'Ride started successfully!';
        this.isStarting = false;
        this.cdr.detectChanges();

        // Clear active ride after starting
        setTimeout(() => {
          this.activeRide = null;
          this.successMessage = null;
          this.cdr.detectChanges();
        }, 2000);
      },
      error: (err) => {
        console.error('❌ Failed to start ride:', err);
        this.errorMessage = err.error?.message || 'Failed to start ride';
        this.isStarting = false;
        this.cdr.detectChanges();
      }
    });
  }
}
