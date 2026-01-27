import {Component, Signal, signal, computed } from '@angular/core';
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

  private pagePropertiesSignal = signal({
    page: 0,
    pageSize: 5,
    totalElements: 0
  });

  page = computed(() => this.pagePropertiesSignal());

  searchRideForm = new FormGroup({
    date: new FormControl<Date | null>(null)
  });

  constructor(private service: RideService) {
    this.rides = this.service.rides;
    this.getPagedEntities();
  }

  searchRides() {
    this.pagePropertiesSignal.update(props => ({...props, page: 0}));
    this.getPagedEntities();
  }

  resetFilter(){
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
    this.service.loadRides(props.page, props.pageSize, this.searchRideForm.value.date ?? undefined)
      .subscribe(res => {
        this.service.setRides(res.content);
        this.pagePropertiesSignal.update(p => ({
          ...p,
          totalElements: res.totalElements
        }));
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
