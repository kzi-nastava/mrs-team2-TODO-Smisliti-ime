import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RideTrackingMapComponent } from '../../layout/ride-tracking-map/ride-tracking-map.component';
import { RideTrackingService } from '../../service/ride-tracking/ride-tracking.service';

@Component({
  selector: 'app-ride-tracking',
  imports: [CommonModule, RideTrackingMapComponent, FormsModule],
  templateUrl: './ride-tracking.component.html',
  styleUrl: './ride-tracking.component.css',
})
export class RideTrackingComponent {

  private rideTrackingService = inject(RideTrackingService);

  showReportForm = false;
  reportText = '';
  showCancelForm = false;
  cancelReason = '';

  readonly tracking = this.rideTrackingService.tracking;
  readonly loading = this.rideTrackingService.loading;

  ngOnInit(): void {
    const logRide = computed(() => {
      const ride = this.rideTrackingService.tracking();
      if (ride) console.log('Ride data:', ride);
      return ride;
    });

    logRide();
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

  submitCancel(): void {
    if (!this.cancelReason.trim()) {
      console.error('Cancel reason is required');
      return;
    }

    const reasonToSend = this.cancelReason;
    this.showCancelForm = false;
    this.cancelReason = '';

    console.log('Cancelling ride with reason:', reasonToSend);

    this.rideTrackingService.cancelRide(reasonToSend).subscribe({
      next: (response) => {
        console.log('Ride cancelled successfully', response);
      },
      error: (error) => {
        console.error('Error cancelling ride', error);
        // in case of error, show the form again with the reason
        this.cancelReason = reasonToSend;
        this.showCancelForm = true;
      }
    });
  }

  canShowCancelRide(): boolean {
    const status = this.rideTrackingService.getCurrentRideStatus();
    if (!status) return false;

    const upper = status.toUpperCase();
    const cancellableStatuses = ['SCHEDULED', 'CREATED', 'PENDING'];
    return cancellableStatuses.includes(upper);
  }
}
