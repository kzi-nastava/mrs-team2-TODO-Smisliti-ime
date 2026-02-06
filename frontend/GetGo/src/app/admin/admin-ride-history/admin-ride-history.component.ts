import { Component, Signal, signal, computed, OnInit } from '@angular/core';
import { GetRideDTO } from '../../model/ride.model';
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
export class AdminRideHistoryComponent implements OnInit {

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
    date: new FormControl<Date | null>(null),
    sortBy:  new FormControl('startTime'),
    sortDirection: new FormControl('DESC')
  });

  constructor(private adminRideService: AdminRideService) {
    this.rides = this.adminRideService.rides;
  }

  ngOnInit() {
    // Load saved values from localStorage
    const savedEmail = localStorage.getItem('lastSearchedEmail');
    const savedUserType = localStorage.getItem('lastSearchedUserType');
    const savedSortBy = localStorage.getItem('lastSortBy');
    const savedSortDirection = localStorage.getItem('lastSortDirection');

    if (savedEmail) {
      this.searchRideForm.patchValue({
        email: savedEmail
      });
    }

    if (savedUserType === 'passenger' || savedUserType === 'driver') {
      this.currentUserType = savedUserType;
      this.searchRideForm.patchValue({
        userType: savedUserType
      });
    }

    if (savedSortBy) {
      this.searchRideForm.patchValue({
        sortBy: savedSortBy
      });
    }

    if (savedSortDirection === 'ASC' || savedSortDirection === 'DESC') {
      this.searchRideForm.patchValue({
        sortDirection: savedSortDirection
      });
    }

    // If we have saved values, automatically search
    if (savedEmail && savedUserType) {
      this.getPagedEntities();
    }
  }

  searchRides() {
    if (this.searchRideForm.invalid) {
      this.searchRideForm.markAllAsTouched();
      return;
    }

    const email = this.searchRideForm.value.email?.trim();
    if (!email) {
      console.error('Email is required');
      return;
    }

    // Save the current user type from the search
    this.currentUserType = this.searchRideForm.value.userType || 'passenger';

    // Save search parameters to localStorage
    localStorage.setItem('lastSearchedEmail', email);
    localStorage.setItem('lastSearchedUserType', this.currentUserType);
    localStorage.setItem('lastSortBy', this.searchRideForm.value.sortBy || 'startTime');
    localStorage.setItem('lastSortDirection', this.searchRideForm.value.sortDirection || 'DESC');

    this.pagePropertiesSignal.update(props => ({ ...props, page: 0 }));
    this.getPagedEntities();
  }

  resetFilter() {
    this.searchRideForm.reset({
      userType: 'passenger',
      email: '',
      date: null,
      sortBy: 'startTime',
      sortDirection: 'DESC'
    });
    this.currentUserType = 'passenger';

    // Clear localStorage
    localStorage.removeItem('lastSearchedEmail');
    localStorage.removeItem('lastSearchedUserType');
    localStorage.removeItem('lastSortBy');
    localStorage.removeItem('lastSortDirection');

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
    const sortBy = this.searchRideForm.value.sortBy || 'startTime';
    const sortDirection = this.searchRideForm.value.sortDirection || 'DESC';

    if (!email || !userType) {
      return;
    }

    console.log('Fetching rides with params:', {
      email,
      userType,
      page: props.page,
      size: props.pageSize,
      sortBy,
      sortDirection,
      dateValue
    });

    const loadRides$ = userType === 'passenger'
      ? this.adminRideService.loadPassengerRides(email, props.page, props.pageSize, dateValue, sortBy, sortDirection)
      : this.adminRideService.loadDriverRides(email, props.page, props.pageSize, dateValue, sortBy, sortDirection);

    loadRides$.subscribe({
      next: res => {
        console.log('Rides loaded successfully:', res);
        this.adminRideService.setRides(res.content || []);

        this.pagePropertiesSignal.update(p => ({
          ...p,
          totalElements: res.totalElements || 0
        }));
      },
      error: err => {
        console.error('Error loading rides:', err);
        console.error('Error status:', err.status);
        console.error('Error message:', err.message);
        console.error('Error URL:', err.url);

        if (err.status === 404) {
          console.warn(`No rides found or endpoint does not exist for ${userType}: ${email}`);
          // Don't show alert, just display empty state
        }

        this.adminRideService.setRides([]);
        this.pagePropertiesSignal.update(p => ({
          ...p,
          totalElements: 0
        }));
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
