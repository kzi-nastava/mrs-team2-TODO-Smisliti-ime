import { Component, ElementRef, ViewChild, AfterViewInit, OnDestroy, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MapComponent } from '../../layout/map/map.component';
import { NavBarComponent } from '../../layout/nav-bar/nav-bar.component';
import { RideService, CreateRideRequestDTO, CreatedRideResponseDTO, GetFavoriteRideDTO } from '../../service/ride/ride.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-passenger-home',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MapComponent, NavBarComponent],
  templateUrl: './passenger-home.html',
  styleUrls: ['./passenger-home.css']
})
export class PassengerHome implements AfterViewInit, OnDestroy, OnInit {
  travelForm: FormGroup;
  isLoading = false;
  estimateMinutes: number | null = null;
  serverError: string | null = null;
  successMessage: string | null = null;
  activeInputIndex: number | null = null;

  favoriteRides: GetFavoriteRideDTO[] = [];
  loadingFavorites = false;
  showFavorites = false;

  // Store coordinates per destination
  private destinationCoords: Array<{ lat: number; lng: number } | null> = [null, null];

  private mapClickListener?: (ev: Event) => void;

  @ViewChild('appMap', {static: false}) private mapComponent?: MapComponent;

  constructor(
    private fb: FormBuilder,
    private rideService: RideService,
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute
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
      vehicleType: ['VAN']
    });
  }

  ngOnInit(): void {
    this.loadFavoriteRides();

    // Check for query params from rebook
    this.route.queryParams.subscribe(params => {
      if (params['from'] && params['to']) {
        // Set start and destination from query params
        this.destinations.at(0).patchValue({ name: params['from'] });
        this.destinations.at(1).patchValue({ name: params['to'] });

        // Set vehicle type
        if (params['vehicleType']) {
          this.travelForm.patchValue({
            vehicleType: params['vehicleType'] === 'ANY' ? '' : params['vehicleType']
          });
        }

        // Set baby seats
        if (params['babySeats']) {
          this.travelForm.patchValue({
            hasBaby: params['babySeats'] === 'true'
          });
        }

        // Set pet friendly
        if (params['petFriendly']) {
          this.travelForm.patchValue({
            hasPets: params['petFriendly'] === 'true'
          });
        }

        // Set passengers
        if (params['passengers']) {
          const emails = params['passengers'].split(',').filter((e: string) => e.trim());
          if (emails.length > 0) {
            this.travelForm.patchValue({ travelOption: 'friends' });
            this.friendEmails.clear();
            emails.forEach((email: string) => {
              this.friendEmails.push(this.fb.group({
                email: [email.trim(), [Validators.required, Validators.email]]
              }));
            });
          }
        }

        this.successMessage = 'Route and settings loaded from previous ride!';
        setTimeout(() => {
          this.successMessage = null;
          this.cdr.detectChanges();
        }, 3000);
      }
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
    const newDest = this.createDestination();
    this.destinations.insert(index + 1, newDest);
    this.destinationCoords.splice(index + 1, 0, null);

    // Subscribe to new destination's value changes
    newDest.get('name')?.valueChanges.subscribe((address: string) => {
      if (address && address.length > 5) {
        this.debounceGeocodeAddress(address, index + 1);
      }
    });
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

  loadFavoriteRides(): void {
    this.loadingFavorites = true;

    this.rideService.getFavoriteRides().subscribe({
      next: (favorites) => {
        this.favoriteRides = favorites;
        this.loadingFavorites = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading favorite rides:', err);
        this.loadingFavorites = false;
        this.cdr.detectChanges();
      }
    });
  }

  toggleFavorites(): void {
    this.showFavorites = !this.showFavorites;
  }

  loadFavoriteRide(favorite: GetFavoriteRideDTO): void {
    // Clear existing destinations
    this.destinations.clear();
    this.destinationCoords = [];

    // Add destinations
    favorite.addresses.forEach((address, index) => {
      this.destinations.push(this.fb.group({
        name: [address, Validators.required]
      }));
      this.destinationCoords.push({
        lat: favorite.latitudes[index],
        lng: favorite.longitudes[index]
      });
    });

    // Set vehicle type
    this.travelForm.patchValue({
      vehicleType: favorite.vehicleType === 'ANY' ? '' : favorite.vehicleType,
      hasBaby: favorite.needsBabySeats,
      hasPets: favorite.needsPetFriendly
    });

    // Set friend emails
    this.friendEmails.clear();
    if (favorite.linkedPassengerEmails && favorite.linkedPassengerEmails.length > 0) {
      this.travelForm.patchValue({ travelOption: 'friends' });
      favorite.linkedPassengerEmails.forEach(email => {
        this.friendEmails.push(this.fb.group({
          email: [email, [Validators.required, Validators.email]]
        }));
      });
    } else {
      this.travelForm.patchValue({ travelOption: 'alone' });
    }

    // Update map with route
    this.updateMapRoute();

    // Close dropdown
    this.showFavorites = false;

    this.successMessage = 'Favorite ride loaded successfully!';
    setTimeout(() => {
      this.successMessage = null;
      this.cdr.detectChanges();
    }, 2000);
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

    if (this.mapComponent) {
      const event = new CustomEvent<{ input: number | null }>('set-active-input-index', {
        detail: {input: index},
        bubbles: true
      });
      this.mapComponent['elementRef'].nativeElement.dispatchEvent(event);
    }
  }

  ngAfterViewInit(): void {
    this.mapClickListener = (ev: Event) => this.handleMapClick(ev);
    if (this.mapComponent && this.mapClickListener) {
      this.mapComponent['elementRef'].nativeElement.addEventListener('map-click', this.mapClickListener as EventListener);
    }

    // Listen for address input changes and geocode them
    this.destinations.controls.forEach((control, index) => {
      control.get('name')?.valueChanges.subscribe((address: string) => {
        if (address && address.length > 10) {
          this.debounceGeocodeAddress(address, index);
        }
      });
    });
  }

  ngOnDestroy(): void {
    if (this.mapComponent && this.mapClickListener) {
      this.mapComponent['elementRef'].nativeElement.removeEventListener('map-click', this.mapClickListener as EventListener);
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

  private debounceTimer: any;

  private debounceGeocodeAddress(address: string, index: number): void {
    // Clear previous timer
    if (this.debounceTimer) {
      clearTimeout(this.debounceTimer);
    }

    // Wait 1 second after user stops typing
    this.debounceTimer = setTimeout(() => {
      this.geocodeAddress(address, index);
    }, 1000);
  }

  private geocodeAddress(address: string, index: number): void {
    fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}`)
      .then(res => res.json())
      .then(data => {
        if (data && data.length > 0) {
          const lat = parseFloat(data[0].lat);
          const lng = parseFloat(data[0].lon);

          console.log(`Geocoded "${address}" to:`, lat, lng);

          this.destinationCoords[index] = { lat, lng };

          // Add marker using ViewChild reference
          if (this.mapComponent) {
            this.mapComponent.addMarkerAtLocation(lat, lng, index);
          }

          this.updateMapRoute();
        } else {
          console.warn(`Could not geocode address: ${address}`);
        }
      })
      .catch(err => {
        console.error('Geocoding error:', err);
      });
  }

  private updateMapRoute(): void {
    const validCoords = this.destinationCoords.filter(c => c !== null) as Array<{ lat: number; lng: number }>;

    if (this.mapComponent) {
      const event = new CustomEvent<{ waypoints: Array<{ lat: number; lng: number }> }>('update-route', {
        detail: { waypoints: validCoords },
        bubbles: true
      });
      this.mapComponent['elementRef'].nativeElement.dispatchEvent(event);
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

    // Check if all destinations have addresses
    const allAddresses = this.destinations.controls.every(c => {
      const name = c.get('name')?.value;
      return name && name.trim().length > 0;
    });

    if (!allAddresses) {
      this.serverError = 'Please enter all destination addresses';
      return;
    }

    // If coordinates are missing, try to geocode addresses first
    const missingCoords = this.destinationCoords.some((c, i) =>
      i < this.destinations.length && c === null
    );

    if (missingCoords) {
      console.log('Some coordinates missing, attempting to geocode...');
      this.serverError = 'Fetching coordinates for addresses...';

      // Geocode all missing addresses
      const geocodePromises = this.destinations.controls.map((control, index) => {
        if (this.destinationCoords[index] === null) {
          const address = control.get('name')?.value;
          return this.geocodeAddressPromise(address, index);
        }
        return Promise.resolve();
      });

      Promise.all(geocodePromises)
        .then(() => {
          // Check if all coordinates are now available
          const coords = this.destinationCoords.filter(c => c !== null);
          if (coords.length < 2) {
            this.serverError = 'Could not find coordinates for some addresses. Please check and try again.';
            return;
          }

          // All coordinates ready, proceed with order
          this.serverError = null;
          this.proceedWithOrder();
        })
        .catch(err => {
          console.error('Geocoding failed:', err);
          this.serverError = 'Failed to find coordinates for addresses. Please select locations on map.';
        });

      return;
    }

    // Coordinates already available
    const coords = this.destinationCoords.filter(c => c !== null);
    if (coords.length < 2) {
      this.serverError = 'Please select at least starting point and destination';
      return;
    }

    this.proceedWithOrder();
  }

  private geocodeAddressPromise(address: string, index: number): Promise<void> {
    return fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}`)
      .then(res => res.json())
      .then(data => {
        if (data && data.length > 0) {
          const lat = parseFloat(data[0].lat);
          const lng = parseFloat(data[0].lon);
          this.destinationCoords[index] = { lat, lng };

          // Add marker using ViewChild reference
          if (this.mapComponent) {
            this.mapComponent.addMarkerAtLocation(lat, lng, index);
          }
        } else {
          throw new Error(`Could not geocode: ${address}`);
        }
      });
  }

  private proceedWithOrder(): void {
    const coords = this.destinationCoords.filter(c => c !== null);

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

          setTimeout(() => {
            this.cancel();
          }, 3000);
        } else {
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
      destinations: [{ name: '' }, { name: '' }],
      orderTiming: 'now',
      scheduledTime: '',
      travelOption: 'alone',
      hasBaby: false,
      hasPets: false,
      vehicleType: ''
    });

    while (this.destinations.length > 2) {
      this.destinations.removeAt(2);
    }

    this.friendEmails.clear();
    this.destinationCoords = [null, null];
    this.setActive(null);
    this.serverError = null;
    this.successMessage = null;

    if (this.mapComponent) {
      const event = new CustomEvent('reset-map', { bubbles: true });
      this.mapComponent['elementRef'].nativeElement.dispatchEvent(event);
    }
  }
}
