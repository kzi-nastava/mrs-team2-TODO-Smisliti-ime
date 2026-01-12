import { Component, inject, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {RideTrackingMapComponent} from '../../layout/ride-tracking-map/ride-tracking-map.component';
import { RideTrackingService } from '../../service/ride-tracking/ride-tracking.service';

@Component({
  selector: 'app-ride-tracking',
  imports: [CommonModule, RideTrackingMapComponent, FormsModule],
  templateUrl: './ride-tracking.component.html',
  styleUrl: './ride-tracking.component.css',
})
export class RideTrackingComponent implements OnInit{

  private rideTrackingService = inject(RideTrackingService);

  showReportForm = false;
  reportText = '';

//   tracking = computed(() => ({
//     startAddress: 'Zlatne grede 4',
//     destinationAddress: 'Trg Dositeja Obradovića 7',
//     estimatedTime: 39,
//     completedDistance: 10,
//     totalDistance: 50
//   }));

  readonly tracking = computed(() => this.rideTrackingService.trackingResource.value());
  readonly loading = computed(() => this.rideTrackingService.trackingResource.isLoading());


  ngOnInit(): void {
    // privremeno hardcode rideId
    this.rideTrackingService.startTracking(1);

    const logRide = computed(() => {
        const ride = this.rideTrackingService.tracking();
        if (ride) console.log('Ride data:', ride);
        return ride;
      });

      // da obezbedimo da se computed pokrene
      logRide();
  }

//   tracking() {
//     return this.rideTrackingService.tracking();
//   }
//
//   loading() {
//     return this.rideTrackingService.loading();
//   }

  progressPercent(): number {
    const ride = this.tracking();
    if (!ride) return 0;

    return 42; // temporary
  }



  submitReport() {
//     this.showReportForm = false;
//     this.reportText = '';

  if (!this.reportText.trim()) return;

  console.log("Submitting report:", this.reportText);

  this.rideTrackingService.createInconsistencyReport({ text: this.reportText })
    .subscribe({
      next: (response) => {
        console.log("Report saved", response);
        this.showReportForm = false;
        this.reportText = '';
      },
      error: (error) => {
        console.error("Error saving report", error);
      }
    });

  }
}
