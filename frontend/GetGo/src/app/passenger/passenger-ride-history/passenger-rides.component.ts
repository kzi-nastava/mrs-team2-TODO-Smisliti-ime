import { Component, Signal } from '@angular/core';
import { GetRideDTO } from '../model/ride.model';
import { FormsModule, ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import {RideService} from '../service/passenger-ride.service';

@Component({
  selector: 'app-passenger-rides',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule
  ],
  templateUrl: './passenger-rides.component.html',
  styleUrl: './passenger-rides.component.css'
})
export class PassengerRidesComponent {

  rides: Signal<GetRideDTO[]>;

  searchRideForm = new FormGroup({
    date: new FormControl<Date | null>(null, Validators.required)
  });

  constructor(private rideService: RideService) {
    this.rides = this.rideService.rides;
    this.rideService.loadRides();
  }

  searchRides() {
    const date = this.searchRideForm.value.date;
    if (!date) return;
    this.rideService.searchRidesByDate(date);
  }

  resetFilter() {
    this.searchRideForm.reset();
    this.rideService.resetFilter();
  }
}
