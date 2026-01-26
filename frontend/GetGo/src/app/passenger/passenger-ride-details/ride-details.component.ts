import { Component, Signal, computed, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { RideService } from '../service/passenger-ride.service';
import { GetRideDTO } from '../model/ride.model';
import { CommonModule } from '@angular/common';
import { GetInconsistencyReportDTO } from '../../model/inconsistency-report.model';

@Component({
  selector: 'app-passenger-ride-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ride-details.component.html',
  styleUrl: './ride-details.component.css'
})
export class PassengerRideDetailsComponent {

  rideId!: number;

  ride = signal<GetRideDTO | null>(null);
  loadingRide = signal(true);
  reports = signal<GetInconsistencyReportDTO[]>([]);
  loadingReports = signal(true);

  constructor(
    private route: ActivatedRoute,
    private rideService: RideService
  ) {
    this.rideId = Number(this.route.snapshot.paramMap.get('id'));
  }

  ngOnInit() {
    this.rideService.getRideById(this.rideId).subscribe({
      next: ride => {
        this.ride.set(ride);
        this.loadingRide.set(false);
      },
      error: () => {
        this.loadingRide.set(false);
      }
    });

    this.rideService.getInconsistencyReports(this.rideId).subscribe({
      next: data => {
        this.reports.set(data);
        this.loadingReports.set(false);
      },
      error: () => {
        this.loadingReports.set(false);
      }
    });
  }
}
