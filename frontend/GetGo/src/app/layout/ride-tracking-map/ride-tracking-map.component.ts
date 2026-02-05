import { Component, AfterViewInit, ElementRef, EventEmitter, Output, Input } from '@angular/core';
import * as L from 'leaflet';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import 'leaflet-routing-machine';
import { DriverService, GetActiveDriverLocationDTO } from '../../service/driver/driver.service';

@Component({
  selector: 'app-ride-tracking-map',
  imports: [],
  templateUrl: './ride-tracking-map.component.html',
  styleUrl: './ride-tracking-map.component.css',
})
export class RideTrackingMapComponent implements AfterViewInit{
  @Input() driverPosition!: { lat: number; lng: number } | null;
  @Input() waypoints: { lat: number; lng: number }[] = [];

  @Output() estimatedTimeChange = new EventEmitter<number>();
  private map: any;
  private driverMarker: L.Marker | null = null;
  private routeControl: any = null;
  initialEstimatedMinutes: number = 0;

  constructor(
      private http: HttpClient,
      private driverService: DriverService,
      private elementRef: ElementRef<HTMLElement>
    ) {}

  ngAfterViewInit(): void {
    // Set default marker icon
    L.Marker.prototype.options.icon = L.icon({
      iconUrl: 'https://unpkg.com/leaflet@1.6.0/dist/images/marker-icon.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.6.0/dist/images/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41]
    });

    this.initMap();
    this.setupEventListeners();
  }

  private initMap(): void {
    this.map = L.map('map', {
      center: [45.2517, 19.8373], // Novi Sad
      zoom: 13
    });

    const tiles = L.tileLayer(
      'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
      {
        maxZoom: 18,
        minZoom: 3,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
      }
    );
    tiles.addTo(this.map);
  }

  private setupEventListeners(): void {
    // Listen for update route (draw route on map)
    this.elementRef.nativeElement.addEventListener('update-route', (ev: Event) => {
      const ce = ev as CustomEvent<{ waypoints: Array<{ lat: number; lng: number }> }>;
      console.log('RideTrackingMap received update-route:', ce.detail.waypoints);
      this.drawRoute(ce.detail.waypoints);
    });

    // Listen for update driver position (move driver marker)
    this.elementRef.nativeElement.addEventListener('update-driver-position', (ev: Event) => {
      const ce = ev as CustomEvent<{ lat: number; lng: number }>;
      /*console.log('RideTrackingMap received update-driver-position:', ce.detail);*/
      this.updateDriverMarker(ce.detail.lat, ce.detail.lng);
    });

    // Listen for map reset
    this.elementRef.nativeElement.addEventListener('reset-map', (ev: Event) => {
        console.log('RideTrackingMap received reset-map');
        this.resetMap();
      });
  }

  private drawRoute(waypoints: Array<{ lat: number; lng: number }>): void {
    // Remove existing route
    if (this.routeControl) {
      this.map.removeControl(this.routeControl);
      this.routeControl = null;
    }

    if (waypoints.length < 2) {
      console.warn('Need at least 2 waypoints to draw route');
      return;
    }

    const latLngs = waypoints.map(wp => L.latLng(wp.lat, wp.lng));

    console.log('Drawing route with waypoints:', latLngs);

    this.routeControl = L.Routing.control({
      waypoints: latLngs,
      router: L.routing.mapbox(
        'pk.eyJ1IjoibWVyaXMxMCIsImEiOiJjbWpxandnNmIwd2piM2dzYzVlc3N6NXExIn0.-OX2bzr7c8eGfjaUX-gwZw',
        { profile: 'mapbox/driving' }
      ),
      routeWhileDragging: false,
      addWaypoints: false,
      fitSelectedRoutes: true,
      showAlternatives: false,
      lineOptions: {
        styles: [{ color: '#3b82f6', opacity: 0.8, weight: 6 }],
        extendToWaypoints: true,
        missingRouteTolerance: 0
      }
    }).addTo(this.map);

    this.routeControl.on('routesfound', (e: any) => {
      const routes = e.routes;
      const summary = routes[0].summary;

      const estimatedMinutes = Math.round(summary.totalTime / 60);
      this.initialEstimatedMinutes = estimatedMinutes;
       this.estimatedTimeChange.emit(estimatedMinutes);

      console.log(`Route: ${summary.totalDistance / 1000} km, ${estimatedMinutes} minutes`);

      const event = new CustomEvent('route-estimated-time', {
          detail: { estimatedTimeMinutes: estimatedMinutes },
          bubbles: true
      });
      this.elementRef.nativeElement.dispatchEvent(event);
    });
  }

  private updateDriverMarker(lat: number, lng: number): void {
    const icon = L.icon({
      iconUrl: 'assets/images/blue_car.svg',
      iconSize: [32, 32],
      iconAnchor: [16, 16],
      popupAnchor: [0, -16]
    });

    if (!this.driverMarker) {
      // Create marker first time
      this.driverMarker = L.marker([lat, lng], { icon })
        .bindPopup('Your Location')
        .addTo(this.map);

      // Center map on driver
      this.map.setView([lat, lng], 15);
    } else {
      // Update existing marker position
      this.driverMarker.setLatLng([lat, lng]);
    }
  }

  private resetMap(): void {
    console.log('Resetting map...');

    // Remove driver marker
    if (this.driverMarker) {
      this.map.removeLayer(this.driverMarker);
      this.driverMarker = null;
    }

    // Remove route
    if (this.routeControl) {
      this.map.removeControl(this.routeControl);
      this.routeControl = null;
    }
  }

  searchStreet(street: string): Observable<any> {
    return this.http.get(
      'https://nominatim.openstreetmap.org/search?format=json&q=' + street
    );
  }

  search(): void {
    this.searchStreet('Strazilovska 19, Novi Sad').subscribe({
      next: (result) => {
        console.log(result);
        L.marker([result[0].lat, result[0].lon])
          .addTo(this.map)
          .bindPopup('Pozdrav iz Strazilovske 19.')
          .openPopup();
      },
      error: () => {},
    });
  }

  registerOnClick(): void {
    this.map.on('click', (e: any) => {
      const coord = e.latlng;
      const lat = coord.lat;
      const lng = coord.lng;
      this.reverseSearch(lat, lng).subscribe((res) => {
        console.log(res.display_name);
      });
      console.log(
        'You clicked the map at latitude: ' + lat + ' and longitude: ' + lng
      );
      const mp = new L.Marker([lat, lng]).addTo(this.map);
      alert(mp.getLatLng());
    });
  }

  reverseSearch(lat: number, lon: number): Observable<any> {
    return this.http.get(
      `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}&<params>`
    );
  }

  setRoute(): void {
    const routeControl = L.Routing.control({
      waypoints: [L.latLng(57.74, 11.94), L.latLng(57.6792, 11.949)],
      router: L.routing.mapbox('pk.eyJ1IjoibWVyaXMxMCIsImEiOiJjbWpxandnNmIwd2piM2dzYzVlc3N6NXExIn0.-OX2bzr7c8eGfjaUX-gwZw', {profile: 'mapbox/driving'})
//         router: L.routing.mapbox('DODATI SVOJ API KEY', {profile: 'mapbox/driving'})
    }).addTo(this.map);

    routeControl.on('routesfound', function(e : any) {
      var routes = e.routes;
      var summary = routes[0].summary;
      alert('Total distance is ' + summary.totalDistance / 1000 + ' km and total time is ' + Math.round(summary.totalTime % 3600 / 60) + ' minutes');
    });
  }



}
