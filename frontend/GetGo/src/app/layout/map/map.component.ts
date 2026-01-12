import {AfterViewInit, Component, ElementRef} from '@angular/core'
import * as L from 'leaflet';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import 'leaflet-routing-machine';
import { VehicleService } from '../../service/vehicle-service/vehicle.service';
import { GetVehicleDTO } from '../../service/vehicle-service/get-vehicle-dto.interface';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrl: './map.component.css',
})
export class MapComponent implements AfterViewInit{

  private map: any;
  private activeInput: string | null = null; // for unregistered (origin/destination)
  private activeInputIndex: number | null = null; // for registered (index-based)
  private originMarker: L.Marker | null = null;
  private destinationMarker: L.Marker | null = null;
  private waypointMarkers: L.Marker[] = []; // for registered home waypoints

  constructor(
    private http: HttpClient,
    private vehicleService: VehicleService,
    private elementRef: ElementRef<HTMLElement>
  ) {}

  private initMap(): void {
    this.map = L.map('map', {
      center: [45.2396, 19.8227],
      zoom: 13,
    });

    const tiles = L.tileLayer(
      'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
      {
        maxZoom: 18,
        minZoom: 3,
        attribution:
          '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      }
    );
    tiles.addTo(this.map);
  }

  ngAfterViewInit(): void {
    this.initMap();

    // Listen for set-active-input event (unregistered home)
    this.elementRef.nativeElement.addEventListener('set-active-input', (ev: Event) => {
      const ce = ev as CustomEvent<{ input: string | null }>;
      this.activeInput = ce.detail.input;
      console.log('Map received set-active-input event, activeInput now:', this.activeInput);
    });

    // Listen for set-active-input-index event (registered home)
    this.elementRef.nativeElement.addEventListener('set-active-input-index', (ev: Event) => {
      const ce = ev as CustomEvent<{ input: number | null }>;
      this.activeInputIndex = ce.detail.input;
      console.log('Map received set-active-input-index event, activeInputIndex now:', this.activeInputIndex);
    });

    this.registerOnClick();
    this.setRoute();
    //this.loadVehicles();
  }

  searchStreet(street: string): Observable<any> {
    return this.http.get(
      'https://nominatim.openstreetmap.org/search?format=json&q=' + street
    );
  }

  registerOnClick(): void {
    this.map.on('click', (e: L.LeafletMouseEvent) => {
      const { lat, lng } = e.latlng;
      console.log('Map clicked at latitude:', lat, 'longitude:', lng, 'activeInput:', this.activeInput, 'activeInputIndex:', this.activeInputIndex);

      // Check if either activeInput or activeInputIndex is set
      if (!this.activeInput && this.activeInputIndex === null) {
        console.log('No active input or index, ignoring map click');
        return;
      }

      // Reverse geocode
      fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`)
        .then(res => res.json())
        .then(data => {
          const address = data.display_name || '';
          console.log('Reverse geocoded address:', address);

          // Handle unregistered home (origin/destination)
          if (this.activeInput) {
            const inputType = this.activeInput as 'origin' | 'destination';

            // Remove old marker
            if (inputType === 'origin' && this.originMarker) {
              this.map?.removeLayer(this.originMarker);
            } else if (inputType === 'destination' && this.destinationMarker) {
              this.map?.removeLayer(this.destinationMarker);
            }

            // Create new marker with color
            const icon = L.divIcon({
              className: 'custom-marker',
              html: `<div style="background-color: ${inputType === 'origin' ? '#22c55e' : '#3b82f6'}; width: 20px; height: 20px; border-radius: 50%; border: 2px solid white;"></div>`,
              iconSize: [20, 20]
            });

            const marker = L.marker([lat, lng], { icon }).addTo(this.map!);

            if (inputType === 'origin') {
              this.originMarker = marker;
            } else {
              this.destinationMarker = marker;
            }

            console.log('Placed', inputType, 'marker at', lat, lng);

            // Dispatch event with inputType
            const event = new CustomEvent('map-click', {
              detail: { lat, lng, address, inputType },
              bubbles: true
            });
            this.elementRef.nativeElement.dispatchEvent(event);
            console.log('Dispatched map-click event with inputType:', inputType);
          }
          // Handle registered home (index-based destinations)
          else if (this.activeInputIndex !== null) {
            const index = this.activeInputIndex;

            // Remove old marker at this index if exists
            if (this.waypointMarkers[index]) {
              this.map?.removeLayer(this.waypointMarkers[index]);
            }

            // Pick color based on index (0=green start, others=orange waypoints)
            let color = '#f97316'; // orange for waypoints
            if (index === 0) color = '#22c55e'; // green for start

            const icon = L.divIcon({
              className: 'custom-marker',
              html: `<div style="background-color: ${color}; width: 20px; height: 20px; border-radius: 50%; border: 2px solid white;"></div>`,
              iconSize: [20, 20]
            });

            const marker = L.marker([lat, lng], { icon }).addTo(this.map!);
            this.waypointMarkers[index] = marker;

            console.log('Placed waypoint marker at index', index, 'lat:', lat, 'lng:', lng);

            // Dispatch event without inputType (registered home uses index)
            const event = new CustomEvent('map-click', {
              detail: { lat, lng, address },
              bubbles: true
            });
            this.elementRef.nativeElement.dispatchEvent(event);
            console.log('Dispatched map-click event for index:', index);
          }
        })
        .catch(err => {
          console.error('Reverse geocoding failed:', err);

          // Still dispatch event with coordinates only
          const event = new CustomEvent('map-click', {
            detail: { lat, lng, address: '' },
            bubbles: true
          });
          this.elementRef.nativeElement.dispatchEvent(event);
        });
    });
  }

  setRoute(): void {
  }

  /*loadVehicles(): void {
    this.vehicleService.getVehicles().subscribe({
      next: (vehicles: GetVehicleDTO[]) => {
        console.log('Vehicles loaded:', vehicles);
        // Here you would add code to display vehicles on the map
      },
      error: (err) => {
        console.error('Error loading vehicles:', err);
      }
    });
  }*/
}
