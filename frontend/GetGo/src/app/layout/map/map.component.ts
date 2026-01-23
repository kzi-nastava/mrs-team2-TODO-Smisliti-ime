import {AfterViewInit, Component, ElementRef} from '@angular/core'
import * as L from 'leaflet';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import 'leaflet-routing-machine';
import {VehicleService} from '../../service/vehicle-service/vehicle.service';
import {GetVehicleDTO} from '../../service/vehicle-service/get-vehicle-dto.interface';

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
  private routeControl: any = null; // Store the current route control

  constructor(
    private http: HttpClient,
    private vehicleService: VehicleService,
    private elementRef: ElementRef<HTMLElement>
  ) {}

  private initMap(): void {
    this.map = L.map('map', {
      center: [45.2517, 19.8373],  // Novi Sad city center
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

    L.Marker.prototype.options.icon = L.icon({
      iconUrl: 'https://unpkg.com/leaflet@1.6.0/dist/images/marker-icon.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.6.0/dist/images/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41]
    });
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

    // Listen for route update event (when waypoints change)
    this.elementRef.nativeElement.addEventListener('update-route', (ev: Event) => {
      const ce = ev as CustomEvent<{ waypoints: Array<{ lat: number; lng: number }> }>;
      console.log('Map received update-route event with waypoints:', ce.detail.waypoints);
      this.updateRoute(ce.detail.waypoints);
    });

    // Listen for map reset
    this.elementRef.nativeElement.addEventListener('reset-map', (ev: Event) => {
      console.log('Map received reset-map event');
      this.resetMap();
    });

    this.registerOnClick();
    // Removed setRoute() call - routes will be drawn dynamically
    this.loadVehicles();
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

      if (!this.activeInput && this.activeInputIndex === null) {
        console.log('No active input or index, ignoring map click');
        return;
      }

      fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`)
        .then(res => res.json())
        .then(data => {
          const address = data.display_name || `${lat.toFixed(5)}, ${lng.toFixed(5)}`;
          console.log('Reverse geocoded address:', address);

          if (this.activeInput) {
            const inputType = this.activeInput as 'origin' | 'destination';

            if (inputType === 'origin' && this.originMarker) this.map.removeLayer(this.originMarker);
            if (inputType === 'destination' && this.destinationMarker) this.map.removeLayer(this.destinationMarker);

            const icon = L.divIcon({
              className: 'custom-marker',
              html: `<div style="background-color: ${inputType === 'origin' ? '#22c55e' : '#3b82f6'}; width: 20px; height: 20px; border-radius: 50%; border: 2px solid white;"></div>`,
              iconSize: [20, 20]
            });

            const marker = L.marker([lat, lng], { icon }).addTo(this.map!);
            if (inputType === 'origin') this.originMarker = marker;
            else this.destinationMarker = marker;

            const event = new CustomEvent('map-click', {
              detail: { lat, lng, address, inputType },
              bubbles: true
            });
            this.elementRef.nativeElement.dispatchEvent(event);
            console.log('Dispatched map-click event with address:', address, inputType);

          } else if (this.activeInputIndex !== null) {
            const index = this.activeInputIndex;

            if (this.waypointMarkers[index]) this.map.removeLayer(this.waypointMarkers[index]);

            const color = index === 0 ? '#22c55e' : '#f97316';
            const icon = L.divIcon({
              className: 'custom-marker',
              html: `<div style="background-color: ${color}; width: 20px; height: 20px; border-radius: 50%; border: 2px solid white;"></div>`,
              iconSize: [20, 20]
            });

            const marker = L.marker([lat, lng], { icon }).addTo(this.map!);
            this.waypointMarkers[index] = marker;

            const event = new CustomEvent('map-click', {
              detail: { lat, lng, address, index },
              bubbles: true
            });
            this.elementRef.nativeElement.dispatchEvent(event);
            console.log('Dispatched map-click event for waypoint with address:', index, address);
          }
        })
        .catch(err => {
          console.error('Reverse geocoding failed:', err);

          // Fallback to coordinates only
          const address = `${lat.toFixed(5)}, ${lng.toFixed(5)}`;
          const event = new CustomEvent('map-click', {
            detail: { lat, lng, address, inputType: this.activeInput, index: this.activeInputIndex ?? undefined },
            bubbles: true
          });
          this.elementRef.nativeElement.dispatchEvent(event);
        });
    });
  }

  private updateRoute(waypoints: Array<{ lat: number; lng: number }>): void {
    // Remove existing route if present
    if (this.routeControl) {
      this.map.removeControl(this.routeControl);
      this.routeControl = null;
    }

    // Need at least 2 waypoints to draw a route
    if (waypoints.length < 2) {
      console.log('Not enough waypoints to draw route');
      return;
    }

    // Convert waypoints to Leaflet LatLng objects
    const latLngs = waypoints.map(wp => L.latLng(wp.lat, wp.lng));

    console.log('Drawing route with waypoints:', latLngs);

    // Create routing control
    this.routeControl = L.Routing.control({
      waypoints: latLngs,
      router: L.routing.mapbox('pk.eyJ1IjoibWVyaXMxMCIsImEiOiJjbWpxandnNmIwd2piM2dzYzVlc3N6NXExIn0.-OX2bzr7c8eGfjaUX-gwZw', {profile: 'mapbox/driving'}),
      routeWhileDragging: false,
      addWaypoints: false,
      fitSelectedRoutes: true,
      showAlternatives: false
    }).addTo(this.map);

    // Optional: Handle route found event
    this.routeControl.on('routesfound', (e: any) => {
      const routes = e.routes;
      const summary = routes[0].summary;
      console.log(`Route: ${summary.totalDistance / 1000} km, ${Math.round(summary.totalTime / 60)} minutes`);
    });
  }

  private resetMap(): void {
    console.log('Resetting map...');

    this.activeInput = null;
    this.activeInputIndex = null;

    // Remove origin/destination markers
    if (this.originMarker) {
      this.map.removeLayer(this.originMarker);
      this.originMarker = null;
    }
    if (this.destinationMarker) {
      this.map.removeLayer(this.destinationMarker);
      this.destinationMarker = null;
    }

    // Remove waypoint markers
    this.waypointMarkers.forEach(marker => {
      if (marker) {
        this.map.removeLayer(marker);
      }
    });
    this.waypointMarkers = [];

    // Remove route
    if (this.routeControl) {
      this.map.removeControl(this.routeControl);
      this.routeControl = null;
    }

    // Reload vehicles
    this.loadVehicles();
  }

  private addVehicleMarker(vehicle: GetVehicleDTO): L.Marker {
    const lat = Number(vehicle.latitude);
    const lng = Number(vehicle.longitude);

    const iconUrl = vehicle.isAvailable
      ? 'assets/images/green_car.svg'
      : 'assets/images/red_car.svg';

    const icon = L.icon({
      iconUrl: iconUrl,
      iconSize: [32, 32],
      iconAnchor: [16, 16],
      popupAnchor: [0, -16]
    });

    // Create the marker with the icon and bind a popup
    const marker = L.marker([lat, lng], { icon })
      .bindPopup(`${vehicle.model} - ${vehicle.isAvailable ? 'Free' : 'Busy'}`);

    return marker;
  }

  private loadVehicles(): void {
    console.log('Trying to load vehicles...');
    this.vehicleService.getActiveVehicles().subscribe({
      next: (vehicles: GetVehicleDTO[]) => {
        console.log('Loaded vehicles:', vehicles);
        if (!vehicles || vehicles.length === 0) return;

        // Create a feature group to hold all vehicle markers
        const markers = vehicles.map(vehicle => this.addVehicleMarker(vehicle));
        const group = L.featureGroup(markers).addTo(this.map);

        // Automatically adjust map view to fit all markers with padding
        this.map.fitBounds(group.getBounds(), { padding: [50, 50] });
      },
      error: (err) => console.error(err)
    });
  }
}
