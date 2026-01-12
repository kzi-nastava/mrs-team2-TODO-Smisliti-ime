import { Component, ElementRef, ViewChild, AfterViewInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { MapComponent } from '../map/map.component';

@Component({
  selector: 'app-registered-home',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MapComponent],
  templateUrl: './registered-home.component.html',
  styleUrls: ['./registered-home.component.css']
})
export class RegisteredHomeComponent implements AfterViewInit, OnDestroy {
  travelForm: FormGroup;
  isLoading = false;
  estimateMinutes: number | null = null;
  serverError: string | null = null;
  activeInputIndex: number | null = null;

  private mapClickListener?: (ev: Event) => void;

  @ViewChild('appMap', { read: ElementRef, static: false }) private mapEl?: ElementRef<HTMLElement>;

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {
    this.travelForm = this.fb.group({
      destinations: this.fb.array([
        this.createDestination(),
        this.createDestination()
      ]),
      orderTiming: ['now', Validators.required],
      travelOption: ['alone', Validators.required]
    });
  }

  get destinations(): FormArray {
    return this.travelForm.get('destinations') as FormArray;
  }

  createDestination(): FormGroup {
    return this.fb.group({
      name: ['', Validators.required]
    });
  }

  addDestination(index: number) {
    this.destinations.insert(index + 1, this.createDestination());
    console.log('Added destination at index', index + 1, 'total destinations:', this.destinations.length);
  }

  removeDestination(index: number) {
    if (this.destinations.length > 2) {
      this.destinations.removeAt(index);
      console.log('Removed destination at index', index, 'total destinations:', this.destinations.length);

      // Clear active input if it was the removed one
      if (this.activeInputIndex === index) {
        this.setActive(null);
      } else if (this.activeInputIndex !== null && this.activeInputIndex > index) {
        // Adjust activeInputIndex if it was after the removed one
        this.activeInputIndex--;
      }
    } else {
      console.log('Cannot remove destination: minimum 2 destinations required');
    }
  }

  setActive(index: number | null) {
    this.activeInputIndex = index;
    console.log('Active input index set to', index);

    if (this.mapEl?.nativeElement) {
      const event = new CustomEvent<{ input: number | null }>('set-active-input-index', {
        detail: { input: index },
        bubbles: true
      });
      this.mapEl.nativeElement.dispatchEvent(event);
      console.log('Dispatched set-active-input-index event to map', event.detail);
    }
  }

  ngAfterViewInit(): void {
    this.mapClickListener = (ev: Event) => this.handleMapClick(ev);
    if (this.mapEl?.nativeElement && this.mapClickListener) {
      this.mapEl.nativeElement.addEventListener('map-click', this.mapClickListener as EventListener);
      console.log('Attached map-click listener to map element');
    }
  }

  ngOnDestroy(): void {
    if (this.mapEl?.nativeElement && this.mapClickListener) {
      this.mapEl.nativeElement.removeEventListener('map-click', this.mapClickListener as EventListener);
      console.log('Removed map-click listener from map element');
    }
  }

  private handleMapClick(ev: Event): void {
    const ce = ev as CustomEvent<LocationPick>;
    const detail = ce?.detail;

    console.log('Map-click detail:', detail, 'activeInputIndex:', this.activeInputIndex);

    if (!detail || this.activeInputIndex === null) {
      console.log('No active input or invalid detail; ignoring map pick');
      return;
    }

    const valueToInsert = detail.address?.trim().length
      ? detail.address
      : `${detail.lat.toFixed(5)}, ${detail.lng.toFixed(5)}`;

    console.log('Value to insert:', valueToInsert, 'into destination index:', this.activeInputIndex);

    const control = this.destinations.at(this.activeInputIndex).get('name');
    if (control) {
      control.setValue(valueToInsert);
      control.markAsTouched();
      console.log('Destination updated at index', this.activeInputIndex, 'to:', valueToInsert);
    }

    this.setActive(null);
    console.log('Cleared activeInputIndex after map pick');
  }

  submit() {
    this.serverError = null;

    if (this.travelForm.invalid) {
      this.travelForm.markAllAsTouched();
      console.log('Form is invalid, aborting submit');

      this.destinations.controls.forEach((control, i) => {
        if (control.invalid) {
          console.log('Validation failed: destination', i, 'is required');
        }
      });
      return;
    }

    const destinationNames = this.destinations.controls.map(c => c.get('name')?.value.trim());
    console.log('Destinations:', destinationNames);

    if (destinationNames.length < 2) {
      console.log('Need at least 2 destinations');
      return;
    }

    const payload: EstimateRequest = {
      origin: destinationNames[0],
      destination: destinationNames[destinationNames.length - 1],
      waypoints: destinationNames.slice(1, -1)
    };

    console.log('Sending estimate request to /api/rides/estimate', payload);
    this.isLoading = true;
    this.estimateMinutes = null;
    this.cdr.detectChanges();

    this.http.post<EstimateResponse>('/api/rides/estimate', payload).subscribe({
      next: (res) => {
        console.log('Received estimate response', res);
        this.estimateMinutes = res.estimatedMinutes;
        this.isLoading = false;
        this.cdr.detectChanges();

        if (this.mapEl?.nativeElement) {
          const event = new CustomEvent<RouteEventDetail>('show-route', {
            detail: {
              origin: destinationNames[0],
              destination: destinationNames[destinationNames.length - 1],
              waypoints: destinationNames.slice(1, -1)
            },
            bubbles: true
          });
          this.mapEl.nativeElement.dispatchEvent(event);
          console.log('Dispatched show-route event to map', event.detail);
        }
      },
      error: (err) => {
        console.error('Estimate request failed', err);
        this.serverError = 'Failed to get estimate from server';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }
}

interface EstimateRequest {
  origin: string;
  destination: string;
  waypoints?: string[];
}

interface EstimateResponse {
  estimatedMinutes: number;
}

interface RouteEventDetail {
  origin: string;
  destination: string;
  waypoints?: string[];
}

interface LocationPick {
  lat: number;
  lng: number;
  address?: string;
}
