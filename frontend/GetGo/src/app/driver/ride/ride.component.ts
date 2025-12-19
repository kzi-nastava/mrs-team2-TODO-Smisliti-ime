import {Component, Signal} from '@angular/core';
import { Ride } from '../model/ride.model';
import { RideService } from '../service/ride.service';
import { FormsModule } from '@angular/forms';
import { RouterModule} from '@angular/router';
import { FormGroup, FormControl, Validators, ReactiveFormsModule} from '@angular/forms';


import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core'; // ili MatMomentDateModule ako koristiš moment
import { MatButtonModule } from '@angular/material/button';

// import {UnregisteredNavBarComponent} from '../../layout/unregistered-nav-bar/unregistered-nav-bar.component';

@Component({
  selector: 'app-ride',
  standalone: true,
  imports: [FormsModule, RouterModule, ReactiveFormsModule, MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule],
  templateUrl: './ride.component.html',
  styleUrl: './ride.component.css',
})
export class RideComponent {
  protected rides:  Signal<Ride[]>;

  searchRideForm = new FormGroup({
    date: new FormControl<Date | null>(null, [Validators.required, Validators.min(2000), Validators.max(new Date().getFullYear())])
  });

  constructor(private service: RideService) {
    this.rides = this.service.rides;
  }

  searchRides() {
    if (this.searchRideForm.valid && this.searchRideForm.value.date) {
      const selectedDate = this.searchRideForm.value.date as Date;
      const day = selectedDate.getDate();
      const month = selectedDate.getMonth() + 1;
      const year = selectedDate.getFullYear();

      this.service.searchRidesByDate(day, month, year);
    }
  }

  resetFilter(){
    this.searchRideForm.reset();
    this.service.resetFilter();
  }


}
