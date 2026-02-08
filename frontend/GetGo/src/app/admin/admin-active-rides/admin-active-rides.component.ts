import { Component, OnInit, computed, Signal, signal } from '@angular/core';
import { FormGroup, FormControl, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { ActiveRideService } from '../../service/active-ride/active-ride.service';
import { GetActiveRideAdminDTO } from '../../model/active-ride.model';

@Component({
  selector: 'app-admin-active-rides',
  imports: [CommonModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule,
              MatButtonModule, MatIconModule, MatMenuModule, RouterModule, FormsModule],
  templateUrl: './admin-active-rides.component.html',
  styleUrl: './admin-active-rides.component.css',
})
export class AdminActiveRidesComponent implements OnInit {
  searchForm: FormGroup;
  activeRides!: Signal<GetActiveRideAdminDTO[]>;

  selectedRide: GetActiveRideAdminDTO | null = null;
  searchText = signal('');

  constructor(private activeRideService: ActiveRideService) {
    this.searchForm = new FormGroup({
      driverName: new FormControl('')
    });
  }

  ngOnInit(): void {
    this.activeRides = this.activeRideService.activeRides;
    this.activeRideService.loadActiveRides();

    this.searchForm.get('driverName')!.valueChanges.subscribe(value => {
      this.searchText.set(value?.toLowerCase() || '');
    });

  }

  filteredRides = computed(() => {
    const filter = this.searchText();

    if (!filter) return this.activeRides();

    return this.activeRides().filter((r: GetActiveRideAdminDTO) =>
      r.driverName.toLowerCase().includes(filter)
    );
  });


}
