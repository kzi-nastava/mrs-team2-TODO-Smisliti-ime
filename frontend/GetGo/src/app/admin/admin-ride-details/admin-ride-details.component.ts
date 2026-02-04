import { Component, signal, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AdminRideService } from '../service/admin-ride.service';
import { GetRideDTO } from '../../passenger/model/ride.model';
import { CommonModule } from '@angular/common';
import { GetInconsistencyReportDTO } from '../../model/inconsistency-report.model';

@Component({
  selector: 'app-admin-ride-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-ride-details.component.html',
  styleUrl: './admin-ride-details.component.css'
})
export class AdminRideDetailsComponent implements OnInit {

  rideId!: number;
  userEmail!: string;
  userType!: 'passenger' | 'driver';

  ride = signal<GetRideDTO | undefined>(undefined);
  loadingRide = signal(true);
  reports = signal<GetInconsistencyReportDTO[]>([]);
  loadingReports = signal(true);

  constructor(
    private route: ActivatedRoute,
    private adminRideService: AdminRideService
  ) {
    // Log all available data
    console.log('Full route snapshot:', {
      params: this.route.snapshot.params,
      paramMap: Object.fromEntries(this.route.snapshot.paramMap.keys.map(k => [k, this.route.snapshot.paramMap.get(k)])),
      url: this.route.snapshot.url.map(segment => segment.path),
      queryParams: this.route.snapshot.queryParams
    });

    // Try multiple ways to get the parameters
    this.rideId = Number(this.route.snapshot.paramMap.get('id') || this.route.snapshot.params['id']);

    // Try reading userType from different sources
    let userType = this.route.snapshot.paramMap.get('userType') || this.route.snapshot.params['userType'];

    // If still null, try to extract from URL segments
    if (!userType) {
      const urlSegments = this.route.snapshot.url.map(s => s.path);
      console.log('URL segments:', urlSegments);
      // URL structure: ['admin', 'rides', 'passenger', '14']
      const userTypeIndex = urlSegments.indexOf('rides') + 1;
      if (userTypeIndex > 0 && userTypeIndex < urlSegments.length) {
        userType = urlSegments[userTypeIndex];
      }
    }

    this.userType = userType as 'passenger' | 'driver';
    this.userEmail = this.route.snapshot.queryParamMap.get('email') || '';

    console.log('AdminRideDetailsComponent initialized with:', {
      rideId: this.rideId,
      userType: this.userType,
      userEmail: this.userEmail,
      fullUrl: window.location.href
    });
  }

  ngOnInit() {
    if (!this.userEmail) {
      console.error('No email provided');
      this.loadingRide.set(false);
      return;
    }

    if (!this.userType || (this.userType !== 'passenger' && this.userType !== 'driver')) {
      console.error('Invalid userType:', this.userType);
      this.loadingRide.set(false);
      return;
    }

    console.log('Fetching ride for userType:', this.userType);

    const getRide$ = this.userType === 'passenger'
      ? this.adminRideService.getPassengerRideById(this.userEmail, this.rideId)
      : this.adminRideService.getDriverRideById(this.userEmail, this.rideId);

    getRide$.subscribe({
      next: (ride) => {
        this.ride.set(ride);
        this.loadingRide.set(false);
      },
      error: (err) => {
        console.error('Error loading ride details:', err);
        this.loadingRide.set(false);
      }
    });

    this.adminRideService.getInconsistencyReports(this.rideId).subscribe({
      next: (data) => {
        this.reports.set(data);
        this.loadingReports.set(false);
      },
      error: (err) => {
        console.error('Error loading reports:', err);
        this.loadingReports.set(false);
      }
    });
  }
}
