import {Component, Signal} from '@angular/core';
import { Ride } from '../model/ride.model';
import { RideService } from '../service/ride.service';
import { FormsModule } from '@angular/forms';
// import {UnregisteredNavBarComponent} from '../../layout/unregistered-nav-bar/unregistered-nav-bar.component';

@Component({
  selector: 'app-ride',
  // imports: [FormsModule, UnregisteredNavBarComponent],
  imports: [FormsModule],
  templateUrl: './ride.component.html',
  styleUrl: './ride.component.css',
})
export class RideComponent {
  protected rides:  Signal<Ride[]>;

  constructor(private service: RideService) {
    this.rides = this.service.rides;
  }
}
