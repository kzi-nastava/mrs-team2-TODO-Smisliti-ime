import {Component, Signal} from '@angular/core';
import { Ride } from '../model/ride.model';
import { RideService } from '../service/ride.service';
import { FormsModule } from '@angular/forms';
import { RouterModule} from '@angular/router';
import { FormGroup, FormControl, Validators, ReactiveFormsModule} from '@angular/forms';


// import {UnregisteredNavBarComponent} from '../../layout/unregistered-nav-bar/unregistered-nav-bar.component';

@Component({
  selector: 'app-ride',
  standalone: true,
  imports: [FormsModule, RouterModule, ReactiveFormsModule],
  templateUrl: './ride.component.html',
  styleUrl: './ride.component.css',
})
export class RideComponent {
  protected rides:  Signal<Ride[]>;

  searchRideForm = new FormGroup({
    day: new FormControl('', [Validators.required, Validators.min(1), Validators.max(31)]),
    month: new FormControl('', [Validators.required, Validators.min(1), Validators.max(12)]),
    year: new FormControl('', [Validators.required, Validators.min(2000), Validators.max(new Date().getFullYear())])
  });

  constructor(private service: RideService) {
    this.rides = this.service.rides;
  }

  searchRides() {
    if (this.searchRideForm.valid) {
      const day = Number(this.searchRideForm.value.day);
      const month = Number(this.searchRideForm.value.month);
      const year = Number(this.searchRideForm.value.year);

      this.service.searchRidesByDate(day, month, year);
    }
  }

  resetFilter(){
    this.searchRideForm.reset();
    this.service.resetFilter();
  }


}
