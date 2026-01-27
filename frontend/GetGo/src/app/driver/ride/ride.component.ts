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
import { PageEvent } from '@angular/material/paginator';
import { MatPaginatorModule } from '@angular/material/paginator';

@Component({
  selector: 'app-ride',
  standalone: true,
  imports: [FormsModule, RouterModule, ReactiveFormsModule, MatFormFieldModule,
    MatInputModule, MatDatepickerModule, MatNativeDateModule, MatButtonModule, CommonModule
    , MatPaginatorModule],
  templateUrl: './ride.component.html',
  styleUrl: './ride.component.css',
})
export class RideComponent {
  protected rides:  Signal<GetRideDTO[]>;

  pageIndex = 0;
  pageSize = 5;

  searchRideForm = new FormGroup({
    date: new FormControl<Date | null>(null, [Validators.required])
  });

  constructor(private service: RideService) {
    this.rides = this.service.rides;
//     this.service.loadRides(this.driverId);
    this.service.loadRides(this.pageIndex, this.pageSize);
  }

  searchRides() {
    this.pageIndex = 0;
    this.service.loadRides(this.pageIndex, this.pageSize, this.searchRideForm.value.date!);
  }

  resetFilter(){
    this.searchRideForm.reset();
    this.pageIndex = 0;
    this.service.loadRides(this.pageIndex, this.pageSize);
  }

  onPageChange(event: PageEvent) {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;

    this.service.loadRides(
      this.pageIndex,
      this.pageSize,
      this.searchRideForm.value.date ?? undefined
    );
  }

  get totalElements() {
    return this.service.totalElements;
  }

  getRideSummary(address: string): string {
    if (!address) return '';

    const parts = address.split(',').map(p => p.trim());

    const firstPart = parts[0] || '';
    const secondPart = parts[1] || '';

    let cityOrMunicipality = parts.find(p => p.startsWith('Град '));

    if (!cityOrMunicipality) {
      cityOrMunicipality = parts.find(p => p.startsWith('Општина '));
    }

    const name = cityOrMunicipality
      ? cityOrMunicipality.replace(/^Град |^Општина /, '')
      : '';

    return `${firstPart}, ${secondPart}${name ? ', ' + name : ''}`;
  }


}
