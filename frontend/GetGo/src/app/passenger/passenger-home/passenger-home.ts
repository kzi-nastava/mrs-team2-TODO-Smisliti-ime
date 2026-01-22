import { Component, ElementRef, ViewChild, AfterViewInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MapComponent } from '../../layout/map/map.component';
import { NavBarComponent } from '../../layout/nav-bar/nav-bar.component';
import { RideService, CreateRideRequestDTO, CreatedRideResponseDTO } from '../../service/ride/ride.service';

@Component({
  selector: 'app-passenger-home',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MapComponent, NavBarComponent],
  templateUrl: './passenger-home.html',
  styleUrls: ['./passenger-home.css']
})
export class PassengerHome implements AfterViewInit, OnDestroy {
  travelForm: FormGroup;
  isLoading = false;
  estimateMinutes: number | null = null;
  serverError: string | null = null;
  successMessage: string | null = null;
  activeInputIndex: number | null = null;

  // Store coordinates per destination
  private destinationCoords: Array<{ lat: number; lng: number } | null> = [null, null];

  private mapClickListener?: (ev: Event) => void;

  @ViewChild('appMap', {read: ElementRef, static: false}) private mapEl?: ElementRef<HTMLElement>;

  constructor(
    private fb: FormBuilder,
    private rideService: RideService,
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
  }

  removeDestination(index: number) {
    if (this.destinations.length > 2) {
      this.destinations.removeAt(index);
      this.destinationCoords.splice(index, 1);

      if (this.activeInputIndex === index) {
        this.setActive(null);
      } else if (this.activeInputIndex !== null && this.activeInputIndex > index) {
        this.activeInputIndex--;
      }
    }
  }

  getScheduledDateTimeDisplay(): string {
    const timeValue = this.travelForm.get('scheduledTime')?.value;
    if (!timeValue) return '';

    const [hours, minutes] = timeValue.split(':').map(Number);
    const now = new Date();
    const scheduled = new Date();
    scheduled.setHours(hours, minutes, 0, 0);

    if (scheduled < now) {
      scheduled.setDate(scheduled.getDate() + 1);
    }

    const diffMs = scheduled.getTime() - now.getTime();
    const diffHours = diffMs / (1000 * 60 * 60);

    if (diffHours > 5) {
      return 'Time must be within 5 hours from now';
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
  }

  removeFriendEmail(index: number) {
    this.friendEmails.removeAt(index);
  }

  setActive(index: number | null) {
    this.activeInputIndex = index;

    if (this.mapEl?.nativeElement) {
      const event = new CustomEvent<{ input: number | null }>('set-active-input-index', {
        detail: {input: index},
        bubbles: true
      });
      this.mapEl.nativeElement.dispatchEvent(event);
    }
  }

  ngAfterViewInit(): void {
    this.mapClickListener = (ev: Event) => this.handleMapClick(ev);
    if (this.mapEl?.nativeElement && this.mapClickListener) {
      this.mapEl.nativeElement.addEventListener('map-click', this.mapClickListener as EventListener);
    }
  }

  ngOnDestroy(): void {
    if (this.mapEl?.nativeElement && this.mapClickListener) {
      this.mapEl.nativeElement.removeEventListener('map-click', this.mapClickListener as EventListener);
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
    }

    this.setActive(null);

    // Update route on map after adding new waypoint
    this.updateMapRoute();
  }

  private updateMapRoute(): void {
    const validCoords = this.destinationCoords.filter(c => c !== null) as Array<{ lat: number; lng: number }>;

    if (this.mapEl?.nativeElement) {
      const event = new CustomEvent<{ waypoints: Array<{ lat: number; lng: number }> }>('update-route', {
        detail: { waypoints: validCoords },
        bubbles: true
      });
      this.mapEl.nativeElement.dispatchEvent(event);
    }
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
      this.serverError = 'Please select at least starting point and destination on the map';
      return;
    }

    // Build request payload
    const request: CreateRideRequestDTO = {
      latitudes: coords.map(c => c!.lat),
      longitudes: coords.map(c => c!.lng),
      addresses: this.destinations.controls.map(c => c.get('name')?.value),
      scheduledTime: this.isOrderLater ? this.travelForm.get('scheduledTime')?.value : null,
      friendEmails: this.isWithFriends
        ? this.friendEmails.controls.map(c => c.get('email')?.value)
        : [],
      hasBaby: this.travelForm.get('hasBaby')?.value || false,
      hasPets: this.travelForm.get('hasPets')?.value || false,
      vehicleType: this.travelForm.get('vehicleType')?.value || ''
    };

    console.log('Ordering ride with payload:', request);

    this.isLoading = true;
    this.serverError = null;
    this.successMessage = null;

    this.rideService.orderRide(request).subscribe({
      next: (response: CreatedRideResponseDTO) => {
        console.log('Received ride order response:', response);

        if (response.status === 'SUCCESS') {
          this.successMessage = response.message;

          // Show success message for 3 seconds, then reset form
          setTimeout(() => {
            this.cancel();
          }, 3000);
        } else {
          // Handle error cases
          this.serverError = response.message;
        }

        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to order ride:', err);
        this.serverError = err.error?.message || 'Failed to order ride from server';
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
      hasPets: false,
      vehicleType: ''
    });
    this.destinations.clear();
    this.destinations.push(this.createDestination());
    this.destinations.push(this.createDestination());
    this.friendEmails.clear();
    this.destinationCoords = [null, null];
    this.estimateMinutes = null;
    this.serverError = null;
    this.successMessage = null;
    this.activeInputIndex = null;

    // Notify map to clear active input
    if (this.mapEl?.nativeElement) {
      const event = new CustomEvent<{ input: number | null }>('set-active-input-index', {
        detail: {input: null},
        bubbles: true
      });
      this.mapEl.nativeElement.dispatchEvent(event);

      // Clear route on map
      const routeEvent = new CustomEvent<{ waypoints: Array<{ lat: number; lng: number }> }>('update-route', {
        detail: { waypoints: [] },
        bubbles: true
      });
      this.mapEl.nativeElement.dispatchEvent(routeEvent);
    }
  }
}
