import { Component, Signal, computed, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { RideService } from '../service/ride.service';
import { Ride, GetRideDTO } from '../model/ride.model';
import { CommonModule } from '@angular/common';
// import { RideService } from '../../service/ride/ride.service';
import { GetInconsistencyReportDTO } from '../../model/inconsistency-report.model';

@Component({
  selector: 'app-ride-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ride-details.component.html',
  styleUrl: './ride-details.component.css',
})
export class RideDetailsComponent {
  rideId!: number;

  ride: Signal<GetRideDTO | undefined>;
  reports = signal<GetInconsistencyReportDTO[]>([]);
  loadingReports = signal(true);

  constructor(private route: ActivatedRoute, private rideService: RideService) {
    this.rideId = Number(this.route.snapshot.paramMap.get('id'));

    this.ride = computed(() =>
      this.rideService.rides().find(r => r.id === this.rideId)
    );
  }

  ngOnInit(){
    this.rideService.getInconsistencyReports(this.rideId).subscribe({
      next: (data) => {
        this.reports.set(data);
        this.loadingReports.set(false);
      },
      error: (err) => {
        this.loadingReports.set(false);
      }
    })
  }

}
