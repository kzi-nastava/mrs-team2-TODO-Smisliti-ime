import { Component, ElementRef, ViewChild, AfterViewInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { FormsModule, NgModel } from '@angular/forms';
import { MapComponent } from '../map/map.component';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-unregistered-home',
  templateUrl: './unregistered-home.component.html',
  standalone: true,
  imports: [CommonModule, FormsModule, MapComponent],
  styleUrls: ['./unregistered-home.component.css']
})
export class UnregisteredHomeComponent implements AfterViewInit, OnDestroy {
  origin = '';
  destination = '';
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
    this.estimateMinutes = null; // reset estimate when closing form
    this.serverError = null; // reset error too
    console.log('Close route form, cleared activeInput and estimate');

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
    console.log('handleMapClick triggered, event type:', ev.type);

    // expect CustomEvent<LocationPick> from map component
    const ce = ev as CustomEvent<LocationPick>;
    const detail = ce && ce.detail ? ce.detail : undefined;

    console.log('Map-click detail:', detail, 'activeInput:', this.activeInput);

    if (!detail) {
      console.log('map-click received without detail; ignoring');
      return;
    }

    // Verify the inputType matches our activeInput (safety check)
    if (detail.inputType && detail.inputType !== this.activeInput) {
      console.log('inputType mismatch, expected', this.activeInput, 'got', detail.inputType);
      return;
    }

    const valueToInsert = detail.address?.trim().length ? detail.address : `${detail.lat.toFixed(5)}, ${detail.lng.toFixed(5)}`;
    console.log('Value to insert:', valueToInsert, 'into field:', this.activeInput);

    if (this.activeInput === 'origin') {
      this.origin = valueToInsert;
      console.log('Origin updated to:', this.origin);
    } else if (this.activeInput === 'destination') {
      this.destination = valueToInsert;
      console.log('Destination updated to:', this.destination);
    } else {
      console.log('No active input selected; ignoring map pick');
      return; // don't clear if nothing was set
    }

    // Clear activeInput after successfully inserting value
    this.activeInput = null;
    console.log('Cleared activeInput after map pick');

    // Notify map that no input is active anymore
    if (this.mapEl?.nativeElement) {
      const event = new CustomEvent<{ input: ActiveInput }>('set-active-input', {
        detail: { input: null },
        bubbles: true
      });
      this.mapEl.nativeElement.dispatchEvent(event);
      console.log('Dispatched set-active-input(null) after map pick');
    }
  }

  calculateTime() {
    // kept for compatibility; simply open the form
    this.openForm();
  }

  submitForm(originModel: NgModel, destinationModel: NgModel) {
    this.serverError = null;
    // Validation checks with console logs per field
    if (!this.origin || !this.origin.trim()) {
      originModel.control.markAsTouched();
      console.log('Validation failed: origin is required');
    }
    if (!this.destination || !this.destination.trim()) {
      destinationModel.control.markAsTouched();
      console.log('Validation failed: destination is required');
    }

    if (originModel.invalid || destinationModel.invalid) {
      console.log('Form is invalid, aborting submit');
      return;
    }

    const payload: EstimateRequest = {
      origin: this.origin.trim(),
      destination: this.destination.trim()
    };

    console.log('Sending estimate request to /api/rides/estimate', payload);
    this.isLoading = true;
    this.estimateMinutes = null;
    this.cdr.detectChanges();
    console.log('isLoading set to true');

    this.http.post<EstimateResponse>('/api/rides/estimate', payload).subscribe({
      next: (res) => {
        console.log('Received estimate response', res);
        this.estimateMinutes = res.estimatedMinutes;
        this.isLoading = false;
        this.cdr.detectChanges();
        console.log('isLoading set to false after success');

        // dispatch custom event so map can react and draw route
        if (this.mapEl?.nativeElement) {
          const event = new CustomEvent<EstimateRouteEventDetail>('show-route', {
            detail: { origin: this.origin, destination: this.destination },
            bubbles: true
          });
          this.mapEl.nativeElement.dispatchEvent(event);
          console.log('Dispatched show-route event to map', event.detail);
        } else {
          console.log('Map element not available to dispatch route event');
        }

        // close the form after success using setTimeout to avoid NG0100
        setTimeout(() => {
          this.closeForm();
        }, 0);
      },
      error: (err) => {
        console.error('Estimate request failed', err);
        this.serverError = 'Failed to get estimate from server';
        this.isLoading = false;
        this.cdr.detectChanges();
        console.log('isLoading set to false after error');
      }
    });
  }
}

interface EstimateRequest {
  origin: string;
  destination: string;
}

interface EstimateResponse {
  estimatedMinutes: number;
}

interface EstimateRouteEventDetail {
  origin: string;
  destination: string;
}

type ActiveInput = 'origin' | 'destination' | null;

interface LocationPick {
  lat: number;
  lng: number;
  address?: string;
  inputType?: 'origin' | 'destination'; // added
}
