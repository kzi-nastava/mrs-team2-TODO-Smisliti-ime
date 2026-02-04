import { Component, Signal, signal, computed } from '@angular/core';
import { GetRideDTO } from '../../passenger/model/ride.model';
import { FormsModule, ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { AdminRideService } from '../service/admin-ride.service';

@Component({
  selector: 'app-admin-ride-history',
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
    MatPaginatorModule,
    MatSelectModule
  ],
  templateUrl: './admin-ride-history.component.html',
  styleUrl: './admin-ride-history.component.css'
})
export class AdminRideHistoryComponent {

  protected rides: Signal<GetRideDTO[]>;

  // Add property to track current search context
  currentUserType: 'passenger' | 'driver' = 'passenger';

  private pagePropertiesSignal = signal({
    page: 0,
    pageSize: 5,
    totalElements: 0
  });

  page = computed(() => this.pagePropertiesSignal());

  searchRideForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    userType: new FormControl<'passenger' | 'driver'>('passenger', Validators.required),
    date: new FormControl<Date | null>(null)
  });

  constructor(private adminRideService: AdminRideService) {
    this.rides = this.adminRideService.rides;
  }

  searchRides() {
    if (this.searchRideForm.invalid) {
      this.searchRideForm.markAllAsTouched();
      return;
    }

    // Save the current user type from the search
    this.currentUserType = this.searchRideForm.value.userType || 'passenger';

    this.pagePropertiesSignal.update(props => ({ ...props, page: 0 }));
    this.getPagedEntities();
  }

  resetFilter() {
    this.searchRideForm.reset({
      userType: 'passenger',
      email: '',
      date: null
    });
    this.currentUserType = 'passenger';
    this.pagePropertiesSignal.update(props => ({ ...props, page: 0 }));
    this.adminRideService.setRides([]);
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
    const email = this.searchRideForm.value.email;
    const userType = this.searchRideForm.value.userType;
    const dateValue = this.searchRideForm.value.date ?? undefined;

    if (!email || !userType) {
      return;
    }

    const loadRides$ = userType === 'passenger'
      ? this.adminRideService.loadPassengerRides(email, props.page, props.pageSize, dateValue)
      : this.adminRideService.loadDriverRides(email, props.page, props.pageSize, dateValue);

    loadRides$.subscribe({
      next: res => {
        this.adminRideService.setRides(res.content || []);

        this.pagePropertiesSignal.update(p => ({
          ...p,
          totalElements: res.totalElements || 0
        }));
      },
      error: err => {
        console.error('Error loading rides:', err);
        this.adminRideService.setRides([]);
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
