import { Component, Signal, signal, computed } from '@angular/core';
import { GetRideDTO } from '../../model/ride.model';
import { FormsModule, ReactiveFormsModule, FormGroup, FormControl } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { RideService } from '../service/passenger-ride.service';

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
    MatButtonModule,
    MatPaginatorModule
  ],
  templateUrl: './passenger-rides.component.html',
  styleUrls: ['./passenger-rides.component.css']
})
export class PassengerRidesComponent {

  protected rides: Signal<GetRideDTO[]>;

  private pagePropertiesSignal = signal({
    page: 0,
    pageSize: 5,
    totalElements: 0
  });

  page = computed(() => this.pagePropertiesSignal());

  searchRideForm = new FormGroup({
    date: new FormControl<Date | null>(null)
  });

  constructor(private rideService: RideService) {
    this.rides = this.rideService.rides;
    this.getPagedEntities();
  }

  searchRides() {
    this.pagePropertiesSignal.update(props => ({...props, page: 0}));
    this.getPagedEntities();
  }

  resetFilter() {
    this.searchRideForm.reset();
    this.pagePropertiesSignal.update(props => ({...props, page: 0}));
    this.getPagedEntities();
  }

  onPageChange(pageEvent: PageEvent) {
    this.pagePropertiesSignal.update(props => ({
      ...props,
      page: pageEvent.pageIndex,
      pageSize: pageEvent.pageSize
    }));

    this.getPagedEntities();
  }

  private getPagedEntities() {
    const props = this.pagePropertiesSignal();
    const dateValue = this.searchRideForm.value.date ?? undefined;

    console.log('Fetching rides with:', { page: props.page, size: props.pageSize, date: dateValue });

    this.rideService.loadRides(props.page, props.pageSize, dateValue)
      .subscribe({
        next: res => {
          console.log('Rides loaded successfully:', res);
          this.rideService.setRides(res.content || []);

          this.pagePropertiesSignal.update(p => ({
            ...p,
            totalElements: res.totalElements || 0
          }));
        },
        error: err => {
          console.error('Error loading rides:', err);
          console.error('Error status:', err.status);
          console.error('Error message:', err.error);
          console.error('Error URL:', err.url);

          if (err.error && err.error.message) {
            console.error('Backend error message:', err.error.message);
          }

          this.rideService.setRides([]);
        }
      });
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
