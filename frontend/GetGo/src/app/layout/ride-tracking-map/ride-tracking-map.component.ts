import { Component } from '@angular/core';
import { AfterViewInit } from '@angular/core';
import * as L from 'leaflet';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import 'leaflet-routing-machine';
import { VehicleService } from '../../service/vehicle-service/vehicle.service';
import { GetVehicleDTO } from '../../service/vehicle-service/get-vehicle-dto.interface';

@Component({
  selector: 'app-ride-tracking-map',
  imports: [],
  templateUrl: './ride-tracking-map.component.html',
  styleUrl: './ride-tracking-map.component.css',
})
export class RideTrackingMapComponent implements AfterViewInit{
  private map: any;

    constructor(private http: HttpClient, private vehicleService: VehicleService) {}

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
      let DefaultIcon = L.icon({
        iconUrl: 'https://unpkg.com/leaflet@1.6.0/dist/images/marker-icon.png',
      });

      L.Marker.prototype.options.icon = DefaultIcon;
      this.initMap();
      this.registerOnClick()
      this.setRoute()
      this.search()
      this.loadVehicles();
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
