import { Component, ElementRef, ViewChild, AfterViewInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { MapComponent } from '../map/map.component';
import { NavBarComponent } from '../nav-bar/nav-bar.component';
import { environment } from '../../../env/environment';

@Component({
  selector: 'app-registered-home',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MapComponent, NavBarComponent],
  templateUrl: './registered-home.component.html',
  styleUrls: ['./registered-home.component.css']
})
export class RegisteredHomeComponent implements AfterViewInit, OnDestroy {
  travelForm: FormGroup;
  isLoading = false;
  estimateMinutes: number | null = null;
  serverError: string | null = null;
  activeInputIndex: number | null = null;

  // Store coordinates per destination
  private destinationCoords: Array<{ lat: number; lng: number } | null> = [null, null];

  private mapClickListener?: (ev: Event) => void;

  @ViewChild('appMap', {read: ElementRef, static: false}) private mapEl?: ElementRef<HTMLElement>;

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
      scheduledTime: [''],
      travelOption: ['alone', Validators.required],
      friendEmails: this.fb.array([]),
      hasBaby: [false],
      hasPets: [false],
      vehicleType: ['']
    });
  }

  get destinations(): FormArray {
    return this.travelForm.get('destinations') as FormArray;
  }

  get friendEmails(): FormArray {
    return this.travelForm.get('friendEmails') as FormArray;
  }

  get isOrderLater(): boolean {
    return this.travelForm.get('orderTiming')?.value === 'later';
  }

  get isWithFriends(): boolean {
    return this.travelForm.get('travelOption')?.value === 'friends';
  }

  createDestination(): FormGroup {
    return this.fb.group({
      name: ['', Validators.required]
    });
  }

  createFriendEmail(): FormGroup {
    return this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  addDestination(index: number) {
    this.destinations.insert(index + 1, this.createDestination());
    this.destinationCoords.splice(index + 1, 0, null);
    console.log('Added destination at index', index + 1, 'total destinations:', this.destinations.length);
  }

  removeDestination(index: number) {
    if (this.destinations.length > 2) {
      this.destinations.removeAt(index);
      this.destinationCoords.splice(index, 1);
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

  getScheduledDateTimeDisplay(): string {
    const timeValue = this.travelForm.get('scheduledTime')?.value;
    if (!timeValue) return '';

    const [hours, minutes] = timeValue.split(':').map(Number);
    const now = new Date();
    const scheduled = new Date();
    scheduled.setHours(hours, minutes, 0, 0);

    // If selected time is before current time, it means next day
    if (scheduled < now) {
      scheduled.setDate(scheduled.getDate() + 1);
    }

    const diffMs = scheduled.getTime() - now.getTime();
    const diffHours = diffMs / (1000 * 60 * 60);

    if (diffHours > 5) {
      return '⚠️ Time must be within 5 hours from now';
    }

    const dateStr = scheduled.toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });

    return `Scheduled for: ${dateStr}`;
  }

  addFriendEmail() {
    this.friendEmails.push(this.createFriendEmail());
    console.log('Added friend email field, total:', this.friendEmails.length);
  }

  removeFriendEmail(index: number) {
    this.friendEmails.removeAt(index);
    console.log('Removed friend email at index', index, 'total:', this.friendEmails.length);
  }

  setActive(index: number | null) {
    this.activeInputIndex = index;
    console.log('Active input index set to', index);

    if (this.mapEl?.nativeElement) {
      const event = new CustomEvent<{ input: number | null }>('set-active-input-index', {
        detail: {input: index},
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
    const ce = ev as CustomEvent<{ lat: number; lng: number; address?: string; index?: number }>;
    const detail = ce.detail;

    if (!detail || this.activeInputIndex === null) return;

    const idx = this.activeInputIndex;
    this.destinationCoords[idx] = { lat: detail.lat, lng: detail.lng };

    const control = this.destinations.at(idx).get('name');
    if (control) {
      const displayValue = detail.address || `${detail.lat.toFixed(5)}, ${detail.lng.toFixed(5)}`;
      control.setValue(displayValue);
      control.markAsTouched();
      console.log(`Destination ${idx} set to:`, displayValue, 'coords:', this.destinationCoords[idx]);
    }

    // Reset active input
    this.setActive(null);
  }

  submit() {
    // Validate all fields
    Object.keys(this.travelForm.controls).forEach(key => {
      const control = this.travelForm.get(key);
      if (control) {
        control.markAsTouched();
      }
    });

    this.destinations.controls.forEach(dest => {
      dest.markAsTouched();
    });

    this.friendEmails.controls.forEach(email => {
      email.markAsTouched();
    });

    if (this.travelForm.invalid) {
      console.log('Form validation failed');
      return;
    }

    const coords = this.destinationCoords.filter(c => c !== null);
    if (coords.length < 2) {
      console.log('Need at least start and destination coordinates');
      this.serverError = 'Please select at least starting point and destination on the map';
      return;
    }

    const payload = {
      coordinates: coords,
      scheduledTime: this.isOrderLater ? this.travelForm.get('scheduledTime')?.value : null,
      friendEmails: this.isWithFriends ? this.friendEmails.controls.map(c => c.get('email')?.value) : [],
      hasBaby: this.travelForm.get('hasBaby')?.value,
      hasPets: this.travelForm.get('hasPets')?.value,
      vehicleType: this.travelForm.get('vehicleType')?.value
    };
    console.log('Sending estimate request with payload', payload);

    this.isLoading = true;
    this.serverError = null;
    this.estimateMinutes = null;

    this.http.post<EstimateResponse>(`${environment.apiHost}/api/rides/estimate`, payload).subscribe({
      next: res => {
        console.log('Received estimate response', res);
        this.estimateMinutes = res.durationMinutes;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: err => {
        console.error('Failed to get estimate', err);
        this.serverError = 'Failed to get estimate from server';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  cancel() {
    this.travelForm.reset({
      orderTiming: 'now',
      travelOption: 'alone',
      hasBaby: false,
      hasPets: false
    });
    this.destinations.clear();
    this.destinations.push(this.createDestination());
    this.destinations.push(this.createDestination());
    this.friendEmails.clear();
    this.destinationCoords = [null, null];
    this.estimateMinutes = null;
    this.serverError = null;
    this.activeInputIndex = null;
    console.log('Form cancelled and reset');

    // Notify map
    if (this.mapEl?.nativeElement) {
      const event = new CustomEvent<{ input: number | null }>('set-active-input-index', {
        detail: {input: null},
        bubbles: true
      });
      this.mapEl.nativeElement.dispatchEvent(event);
    }
  }
}

interface EstimateResponse {
  price: number;
  durationMinutes: number;
  distanceKm: number;
}
