import {AfterViewInit, Component, ElementRef, OnInit} from '@angular/core'
import * as L from 'leaflet';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import 'leaflet-routing-machine';
import { DriverService, GetActiveDriverLocationDTO } from '../../service/driver/driver.service';
import { WebSocketService } from '../../service/websocket/websocket.service';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrl: './map.component.css',
})
export class MapComponent implements OnInit, AfterViewInit{
  private map: any;
  private activeInput: string | null = null;
  private activeInputIndex: number | null = null;
  private originMarker: L.Marker | null = null;
  private destinationMarker: L.Marker | null = null;
  private waypointMarkers: L.Marker[] = [];
  private routeControl: any = null;
  private panicMarkers: L.Marker[] = [];

  // Track car markers per ride ID - car will have embedded panic badge
  private rideCarMarkers = new Map<number, L.Marker>();
  // Track static driver markers by driver ID
  private driverMarkers = new Map<number, L.Marker>();

  constructor(
    private http: HttpClient,
    private driverService: DriverService,
    private elementRef: ElementRef<HTMLElement>,
    private webSocketService: WebSocketService
  ) {}

  ngOnInit(): void {
    // Map will initialize in ngAfterViewInit
  }

  ngAfterViewInit(): void {
    // Set default marker icon
    L.Marker.prototype.options.icon = L.icon({
      iconUrl: 'https://unpkg.com/leaflet@1.6.0/dist/images/marker-icon.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.6.0/dist/images/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41]
    });

    this.initMap();
    this.registerOnClick();
    this.setupEventListeners();
    this.loadDrivers();

    this.webSocketService.connect().then(() => {
        console.log('Connected to WebSocket');


        this.webSocketService.subscribeToAllDriversLocations().subscribe({
          next: (drivers: any[]) => {
            this.updateDriverMarkers(drivers);
          },
          error: (err) => console.error('WebSocket error:', err)
        });
      }).catch(err => console.error('WebSocket connection failed:', err));
  }

  private updateDriverMarkers(drivers: any | any[]): void {
    if (!drivers) return;

    if (!Array.isArray(drivers)) {
      drivers = [drivers];
    }

    const activeIds = new Set<number>();

    drivers.forEach((driver: { driverId: number, latitude: number, longitude: number, status: string, vehicleType?: string }) => {
      const lat = Number(driver.latitude);
      const lng = Number(driver.longitude);
      const driverId = driver.driverId;

      activeIds.add(driverId);

      const existingMarker = this.driverMarkers.get(driverId);

      const iconUrl = driver.status === 'IDLE'
        ? 'assets/images/green_car.svg'
        : 'assets/images/red_car.svg';

      const icon = L.icon({
        iconUrl,
        iconSize: [32, 32],
        iconAnchor: [16, 16],
        popupAnchor: [0, -16]
      });

      if (existingMarker) {
        existingMarker.setLatLng([lat, lng]);
        existingMarker.setIcon(icon);
      } else {
        const marker = L.marker([lat, lng], { icon })
          .bindPopup(`${driver.status} - ${driver.vehicleType ?? ''}`)
          .addTo(this.map);

        this.driverMarkers.set(driverId, marker);
      }
    });

    // Remove markers for drivers no longer in activeIds
    this.driverMarkers.forEach((marker, id) => {
      if (!activeIds.has(id)) {
        this.map.removeLayer(marker);
        this.driverMarkers.delete(id);
      }
    });
  }




  private initMap(): void {
    this.map = L.map('map', {
      center: [45.2517, 19.8373],  // Novi Sad
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

  private setupEventListeners(): void {
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

      this.processLocationSelection(lat, lng, this.activeInput, this.activeInputIndex);
    });
  }

  // New method: Process location selection (from map click or programmatic geocoding)
  private processLocationSelection(
    lat: number,
    lng: number,
    inputType: string | null,
    index: number | null
  ): void {
    fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`)
      .then(res => res.json())
      .then(data => {
        const address = data.display_name || `${lat.toFixed(5)}, ${lng.toFixed(5)}`;
        console.log('Reverse geocoded address:', address);

        if (inputType) {
          const type = inputType as 'origin' | 'destination';

          if (type === 'origin' && this.originMarker) this.map.removeLayer(this.originMarker);
          if (type === 'destination' && this.destinationMarker) this.map.removeLayer(this.destinationMarker);

          const icon = L.divIcon({
            className: 'custom-marker',
            html: `<div style="background-color: ${type === 'origin' ? '#22c55e' : '#3b82f6'}; width: 20px; height: 20px; border-radius: 50%; border: 2px solid white;"></div>`,
            iconSize: [20, 20]
          });

          const marker = L.marker([lat, lng], { icon }).addTo(this.map!);
          if (type === 'origin') this.originMarker = marker;
          else this.destinationMarker = marker;

          const event = new CustomEvent('map-click', {
            detail: { lat, lng, address, inputType: type },
            bubbles: true
          });
          this.elementRef.nativeElement.dispatchEvent(event);
          console.log('Dispatched map-click event with address:', address, type);

        } else if (index !== null) {
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

        const address = `${lat.toFixed(5)}, ${lng.toFixed(5)}`;
        const event = new CustomEvent('map-click', {
          detail: { lat, lng, address, inputType, index: index ?? undefined },
          bubbles: true
        });
        this.elementRef.nativeElement.dispatchEvent(event);
      });
  }

  // New public method: Add marker programmatically (called from geocoding)
  public addMarkerAtLocation(lat: number, lng: number, index: number): void {
    console.log(`Adding marker at ${lat}, ${lng} for waypoint ${index}`);

    if (this.waypointMarkers[index]) {
      this.map.removeLayer(this.waypointMarkers[index]);
    }

    const color = index === 0 ? '#22c55e' : (index === this.waypointMarkers.length - 1 ? '#3b82f6' : '#f97316');
    const icon = L.divIcon({
      className: 'custom-marker',
      html: `<div style="background-color: ${color}; width: 20px; height: 20px; border-radius: 50%; border: 2px solid white;"></div>`,
      iconSize: [20, 20]
    });

    const marker = L.marker([lat, lng], { icon }).addTo(this.map!);
    this.waypointMarkers[index] = marker;

    // Also update route immediately after adding marker
    const event = new CustomEvent('update-route', {
      bubbles: true
    });
    this.elementRef.nativeElement.dispatchEvent(event);
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

  setRoute(): void {

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

    // Clear ride tracking markers
    this.rideCarMarkers.forEach(marker => this.map.removeLayer(marker));
    this.rideCarMarkers.clear();

    // Clear driver markers
    this.driverMarkers.forEach(marker => this.map.removeLayer(marker));
    this.driverMarkers.clear();

    // Reload drivers
    this.loadDrivers();
  }

  private loadDrivers(): void {
    console.log('Loading active drivers...');
    this.driverService.getActiveDriverLocations().subscribe({
      next: (drivers: GetActiveDriverLocationDTO[]) => {
        console.log('Loaded drivers:', drivers);
        if (!drivers || drivers.length === 0) return;

        // Create a feature group to hold all drivers markers
        const markers = drivers.map(driver => this.addDriverMarker(driver));
        const group = L.featureGroup(markers).addTo(this.map);

        // Automatically adjust map view to fit all markers with padding
        this.map.fitBounds(group.getBounds(), { padding: [50, 50] });
      },
      error: (err) => console.error(err)
    });
  }

  private addDriverMarker(driver: GetActiveDriverLocationDTO): L.Marker {
    const lat = Number(driver.latitude);
    const lng = Number(driver.longitude);

    const iconUrl = driver.isAvailable
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
      .bindPopup(`${driver.vehicleType} - ${driver.isAvailable ? 'Free' : 'Busy'}`);

    // Store driver marker by driver ID
    this.driverMarkers.set(driver.driverId, marker);

    return marker;
  }

  // Updated: car marker moves, with panic badge embedded in icon
  public updateCarMarker(rideId: number, lat: number, lng: number): void {
    if (!this.map) return;

    const existingMarker = this.rideCarMarkers.get(rideId);

    if (existingMarker) {
      // Just move existing marker (icon already has panic badge if needed)
      existingMarker.setLatLng([lat, lng]);
    } else {
      // When creating new tracking marker, remove any static driver marker at similar location
      // (we don't have direct rideId->driverId mapping here, so remove nearby markers)
      this.driverMarkers.forEach((driverMarker, driverId) => {
        const driverLatLng = driverMarker.getLatLng();
        const distance = this.map.distance(driverLatLng, [lat, lng]);

        // If driver marker is within 50 meters of new ride marker, remove it
        if (distance < 50) {
          this.map.removeLayer(driverMarker);
          this.driverMarkers.delete(driverId);
        }
      });

      // Create new car marker
      const icon = L.icon({
        iconUrl: 'assets/images/red_car.svg',
        iconSize: [32, 32],
        iconAnchor: [16, 16],
        popupAnchor: [0, -16]
      });

      const marker = L.marker([lat, lng], { icon })
        .bindPopup(`Ride #${rideId}`)
        .addTo(this.map);

      this.rideCarMarkers.set(rideId, marker);
    }
  }

  // Updated: now creates a composite icon (car + panic badge overlay)
  public updatePanicMarker(rideId: number, lat: number, lng: number, passengerCount: number, driverCount: number): void {
    if (!this.map) return;

    const hasPanic = (passengerCount > 0) || (driverCount > 0);
    const existingMarker = this.rideCarMarkers.get(rideId);

    if (!hasPanic) {
      // No panic - use regular red car icon
      if (existingMarker) {
        const icon = L.icon({
          iconUrl: 'assets/images/red_car.svg',
          iconSize: [32, 32],
          iconAnchor: [16, 16],
          popupAnchor: [0, -16]
        });
        existingMarker.setIcon(icon);
        existingMarker.setLatLng([lat, lng]);
      }
      return;
    }

    // Determine panic type (prioritize passenger)
    const type: 'passenger' | 'driver' = passengerCount > 0 ? 'passenger' : 'driver';
    const count = passengerCount > 0 ? passengerCount : driverCount;

    // Create composite icon: car + panic badge
    const icon = this.createCarWithPanicBadge(type, count);

    if (existingMarker) {
      existingMarker.setIcon(icon);
      existingMarker.setLatLng([lat, lng]);
    } else {
      const marker = L.marker([lat, lng], { icon })
        .bindPopup(`<strong>🚨 PANIC ALERT</strong><br>Ride #${rideId}<br>Type: ${type}`)
        .addTo(this.map);

      this.rideCarMarkers.set(rideId, marker);
    }
  }

  // Updated: remove panic by switching back to regular car icon
  public removePanicMarker(rideId: number): void {
    const marker = this.rideCarMarkers.get(rideId);
    if (marker && this.map) {
      // Switch back to normal red car icon
      const icon = L.icon({
        iconUrl: 'assets/images/red_car.svg',
        iconSize: [32, 32],
        iconAnchor: [16, 16],
        popupAnchor: [0, -16]
      });
      marker.setIcon(icon);
      // Keep marker on map - just remove panic state
    }
  }

  // Clear all panic markers (remove cars from map)
  public clearAllPanicMarkers(): void {
    this.rideCarMarkers.forEach(marker => {
      if (this.map) this.map.removeLayer(marker);
    });
    this.rideCarMarkers.clear();
  }

  /** @deprecated Use updatePanicMarker() instead */
  public updatePanicMarkers(markers: any[]): void {
    console.log('updatePanicMarkers (deprecated) called with:', markers);
  }

  // New: create composite icon - car SVG with panic badge overlay
  private createCarWithPanicBadge(type: 'passenger' | 'driver', count: number): L.DivIcon {
    const letter = type === 'passenger' ? 'P' : 'D';

    // Composite HTML: car image + panic badge positioned on top-right corner
    const html = `
      <div style="position: relative; width: 32px; height: 32px;">
        <img src="assets/images/red_car.svg" style="width: 32px; height: 32px;" />
        <div style="position: absolute; top: -8px; right: -8px;">
          <svg width="24" height="24" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="11" fill="#DC2626" stroke="white" stroke-width="2"/>
            <text x="12" y="16" font-size="12" font-weight="bold" fill="white" text-anchor="middle">${letter}</text>
            ${count > 1 ? `
              <circle cx="20" cy="6" r="6" fill="#FF6B00" stroke="white" stroke-width="1"/>
              <text x="20" y="9" font-size="8" font-weight="bold" fill="white" text-anchor="middle">${count}</text>
            ` : ''}
          </svg>
        </div>
      </div>
    `;

    return L.divIcon({
      className: 'car-panic-marker',
      html: html,
      iconSize: [32, 32],
      iconAnchor: [16, 16],
      popupAnchor: [0, -16]
    });
  }

  // Keep old panic icon method for reference (not used anymore)
  private createPanicIcon(type: 'passenger' | 'driver', count: number): L.DivIcon {
    const letter = type === 'passenger' ? 'P' : 'D';
    const svg = `
      <svg width="40" height="40" xmlns="http://www.w3.org/2000/svg">
        <circle cx="20" cy="20" r="18" fill="#DC2626" stroke="white" stroke-width="2"/>
        <text x="20" y="26" font-size="18" font-weight="bold" fill="white" text-anchor="middle">${letter}</text>
        ${count > 1 ? `
          <circle cx="32" cy="10" r="8" fill="#FF6B00" stroke="white" stroke-width="1.5"/>
          <text x="32" y="14" font-size="11" font-weight="bold" fill="white" text-anchor="middle">${count}</text>
        ` : ''}
      </svg>
    `;

    return L.divIcon({
      className: 'panic-marker-icon',
      html: svg,
      iconSize: [40, 40],
      iconAnchor: [20, 20],
      popupAnchor: [0, -20]
    });
  }

  public get leafletMap(): L.Map | null {
    return this.map;
  }
}
