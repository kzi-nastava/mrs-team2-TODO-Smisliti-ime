import { Component, Signal, signal, computed } from '@angular/core';
import { GetRideDTO } from '../../model/ride.model';
import { FormsModule, ReactiveFormsModule, FormGroup, FormControl } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { RideService } from '../service/passenger-ride.service';
import { RideHistorySummaryHelper } from '../../helpers/ride-history.summary';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatNativeDateModule } from '@angular/material/core';

@Component({
  selector: 'app-passenger-rides',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    ReactiveFormsModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSelectModule,
    MatButtonModule
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

  // add sort controls to match admin UI
  searchRideForm = new FormGroup({
    date: new FormControl<Date | null>(null),
    sortBy: new FormControl<string>('startTime'),
    sortDirection: new FormControl<string>('DESC')
  });

  constructor(
    private rideService: RideService,
    private summaryHelper: RideHistorySummaryHelper
  ) {
    this.rides = this.rideService.rides;
    this.getPagedEntities();
  }

  searchRides() {
    this.pagePropertiesSignal.update(props => ({...props, page: 0}));
    this.getPagedEntities();
  }

  resetFilter() {
    this.searchRideForm.reset();
    // reset defaults for sort
    this.searchRideForm.patchValue({ sortBy: 'startTime', sortDirection: 'DESC' });
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
    const sortBy = this.searchRideForm.value.sortBy ?? 'startTime';
    const sortDirection = this.searchRideForm.value.sortDirection ?? 'DESC';

    console.log('Fetching rides with:', { page: props.page, size: props.pageSize, date: dateValue, sort: sortBy, direction: sortDirection });

    this.rideService.loadRides(props.page, props.pageSize, dateValue, sortBy, sortDirection)
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
    return this.summaryHelper.getRideSummary(address);
  }
}
