import {Component, Signal} from '@angular/core';
import { Ride, GetRideDTO } from '../model/ride.model';
import { RideService } from '../service/ride.service';
import { FormsModule } from '@angular/forms';
import { RouterModule} from '@angular/router';
import { FormGroup, FormControl, Validators, ReactiveFormsModule} from '@angular/forms';
import { CommonModule } from '@angular/common';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-ride',
  standalone: true,
  imports: [FormsModule, RouterModule, ReactiveFormsModule, MatFormFieldModule,
    MatInputModule, MatDatepickerModule, MatNativeDateModule, MatButtonModule, CommonModule],
  templateUrl: './ride.component.html',
  styleUrl: './ride.component.css',
})
export class RideComponent {
  protected rides:  Signal<GetRideDTO[]>;

  searchRideForm = new FormGroup({
    date: new FormControl<Date | null>(null, [Validators.required, Validators.min(2000), Validators.max(new Date().getFullYear())])
  });

  driverId = 11;

  constructor(private service: RideService) {
    this.rides = this.service.rides;
    this.service.loadRides(this.driverId);
  }

  searchRides() {
    const selectedDate = this.searchRideForm.value.date;
    if (selectedDate) {
      this.service.searchRidesByDate(this.driverId, selectedDate);
    }
  }

  resetFilter(){
    this.searchRideForm.reset();
    this.service.resetFilter(this.driverId);
  }


}
