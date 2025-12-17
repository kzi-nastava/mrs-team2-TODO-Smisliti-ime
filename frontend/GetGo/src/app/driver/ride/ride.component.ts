import {Component, Signal} from '@angular/core';
import { Ride } from '../model/ride.model';
import { RideService } from '../service/ride.service';

@Component({
  selector: 'app-ride',
  imports: [],
  templateUrl: './ride.component.html',
  styleUrl: './ride.component.css',
})
export class RideComponent {
  protected rides:  Signal<Ride[]>;

  constructor(private service: RideService) {
    this.rides = this.service.rides;
  }
}
