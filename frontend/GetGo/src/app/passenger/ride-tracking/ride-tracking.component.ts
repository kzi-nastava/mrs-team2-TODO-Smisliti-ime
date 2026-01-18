import { Component, inject, computed, effect } from '@angular/core';
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
export class RideTrackingComponent{

  private rideTrackingService = inject(RideTrackingService);

  showReportForm = false;
  reportText = '';


  readonly tracking = this.rideTrackingService.tracking;
  readonly loading = this.rideTrackingService.loading;


  ngOnInit(): void {
    // temporary hardcode rideId
    this.rideTrackingService.startTracking(1);

    const logRide = computed(() => {
        const ride = this.rideTrackingService.tracking();
        if (ride) console.log('Ride data:', ride);
        return ride;
      });

      logRide();

//     effect (() => {
//       const ride = this.rideTrackingService.tracking();
//       if (ride) console.log('Ride data:', ride);
//     });
  }

  progressPercent(): number {
    const ride = this.tracking();
    if (!ride) return 0;

    return 42; // temporary
  }



  submitReport() {

  if (!this.reportText.trim()) return;

  const textToSend = this.reportText;
  this.showReportForm = false;
  this.reportText = '';

  console.log("Submitting report:", this.reportText);

  this.rideTrackingService.createInconsistencyReport({ text: textToSend })
    .subscribe({
      next: (response) => {
        console.log("Report saved", response);
      },
      error: (error) => {
        console.error("Error saving report", error);
        // in case of error, we can show the form again with the text
        this.reportText = textToSend;
        this.showReportForm = true;
      }
    });
  }

  submitPanic(): void {
    this.rideTrackingService.createPanicAlert().subscribe({
      next: () => {
        console.log('PANIC sent');
      },
      error: (err) => {
        console.error('Failed to send PANIC', err);
      }
    });
  }

}
