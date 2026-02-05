import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { ActiveRideService } from '../../service/active-ride/active-ride.service';
import { GetActiveRideAdminDetailsDTO } from '../../model/active-ride.model';

@Component({
  selector: 'app-active-ride-details',
  imports: [CommonModule],
  templateUrl: './active-ride-details.component.html',
  styleUrl: './active-ride-details.component.css',
})
export class ActiveRideDetailsComponent {
  ride = signal<GetActiveRideAdminDetailsDTO | null>(null);


  constructor(private route: ActivatedRoute, private service: ActiveRideService) {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.service.getActiveRideDetails(id).subscribe(r => this.ride.set(r));
  }
}
