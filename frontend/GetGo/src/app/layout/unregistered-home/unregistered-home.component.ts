import { Component, ElementRef, ViewChild, AfterViewInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { FormsModule, NgModel } from '@angular/forms';
import { MapComponent } from '../map/map.component';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import {UnregisteredNavBarComponent} from '../unregistered-nav-bar/unregistered-nav-bar.component';

@Component({
  selector: 'app-unregistered-home',
  templateUrl: './unregistered-home.component.html',
  standalone: true,
  imports: [CommonModule, FormsModule, MapComponent, UnregisteredNavBarComponent],
  styleUrls: ['./unregistered-home.component.css']
})
export class UnregisteredHomeComponent implements AfterViewInit, OnDestroy {
  origin = '';
  destination = '';

  // Store actual coordinates
  private originCoords: { lat: number; lng: number } | null = null;
  private destinationCoords: { lat: number; lng: number } | null = null;

  formVisible = false;
  isLoading = false;
  estimateMinutes: number | null = null;
  serverError: string | null = null;

  private mapClickListener?: (ev: Event) => void;
  protected activeInput: ActiveInput = null;

  @ViewChild('appMap', { read: ElementRef, static: false }) private mapEl?: ElementRef<HTMLElement>;

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  openForm() {
    this.formVisible = true;
    console.log('Open route form');
  }

  closeForm() {
    this.formVisible = false;
    this.activeInput = null;
    this.estimateMinutes = null;
    this.serverError = null;
    this.originCoords = null;
    this.destinationCoords = null;
    console.log('Close route form, cleared activeInput, estimate and coordinates');

    // Notify map that no input is active
    if (this.mapEl?.nativeElement) {
      const event = new CustomEvent<{ input: ActiveInput }>('set-active-input', {
        detail: { input: null },
        bubbles: true
      });
      this.mapEl.nativeElement.dispatchEvent(event);
    }
  }

  setActive(input: ActiveInput) {
    this.activeInput = input;
    console.log('Active input set to', input);

    // Notify map component which input is active
    if (this.mapEl?.nativeElement) {
      const event = new CustomEvent<{ input: ActiveInput }>('set-active-input', {
        detail: { input: input },
        bubbles: true
      });
      this.mapEl.nativeElement.dispatchEvent(event);
      console.log('Dispatched set-active-input event to map', event.detail);
    }
  }

  ngAfterViewInit(): void {
    // attach map listener after view init
    this.mapClickListener = (ev: Event) => this.handleMapClick(ev);
    if (this.mapEl?.nativeElement && this.mapClickListener) {
      this.mapEl.nativeElement.addEventListener('map-click', this.mapClickListener as EventListener);
      console.log('Attached map-click listener to map element');
    } else {
      console.log('Map element not available in ngAfterViewInit');
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
    const detail = ce.detail;

    if (!detail) return;

    if (detail.inputType && detail.inputType !== this.activeInput) return;

    if (this.activeInput === 'origin') {
      this.originCoords = { lat: detail.lat, lng: detail.lng };
      this.origin = detail.address || `${detail.lat.toFixed(5)}, ${detail.lng.toFixed(5)}`;
      console.log('Origin set to:', this.origin, 'coords:', this.originCoords);

      // Force change detection immediately
      this.cdr.detectChanges();
    } else if (this.activeInput === 'destination') {
      this.destinationCoords = { lat: detail.lat, lng: detail.lng };
      this.destination = detail.address || `${detail.lat.toFixed(5)}, ${detail.lng.toFixed(5)}`;
      console.log('Destination set to:', this.destination, 'coords:', this.destinationCoords);

      // Force change detection immediately
      this.cdr.detectChanges();
    }

    // Reset active input after a small delay to ensure UI updates first
    setTimeout(() => {
      this.activeInput = null;

      if (this.mapEl?.nativeElement) {
        const event = new CustomEvent<{ input: ActiveInput }>('set-active-input', {
          detail: { input: null },
          bubbles: true
        });
        this.mapEl.nativeElement.dispatchEvent(event);
      }
    }, 50);
  }

  calculateTime() {
    // kept for compatibility; simply open the form
    this.openForm();
  }

  submitForm(originModel: NgModel, destinationModel: NgModel) {
    this.serverError = null;

    // Validate
    if (!this.originCoords) { originModel.control.markAsTouched(); return; }
    if (!this.destinationCoords) { destinationModel.control.markAsTouched(); return; }

    const payload = {
      coordinates: [
        { lat: this.originCoords.lat, lng: this.originCoords.lng },
        { lat: this.destinationCoords.lat, lng: this.destinationCoords.lng }
      ]
    };

    console.log('Sending estimate request with payload', payload);
    this.isLoading = true;

    this.http.post<EstimateResponse>('/api/rides/estimate', payload).subscribe({
      next: res => {
        console.log('Received estimate response', res);
        this.estimateMinutes = res.durationMinutes;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: err => {
        console.error('Estimate request failed', err);
        this.serverError = 'Failed to get estimate';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }
}

interface EstimateResponse {
  price: number;
  durationMinutes: number;
  distanceKm: number;
}

type ActiveInput = 'origin' | 'destination' | null;

interface LocationPick {
  lat: number;
  lng: number;
  address?: string;
  inputType?: 'origin' | 'destination'; // added
}
