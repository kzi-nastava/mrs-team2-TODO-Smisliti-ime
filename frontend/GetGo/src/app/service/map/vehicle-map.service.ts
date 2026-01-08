import { Injectable } from '@angular/core';
import * as L from 'leaflet';
import { VehicleService } from '../vehicle-service/vehicle.service';
import { GetVehicleDTO } from '../vehicle-service/get-vehicle-dto.interface';

@Injectable({
  providedIn: 'root',
})
export class VehicleMapService {
  constructor(private vehicleService: VehicleService) {}

  // Creates Leaflet marker for a vehicle
  createVehicleMarker(vehicle: GetVehicleDTO): L.Marker {
    const icon = L.icon({
      iconUrl: vehicle.isAvailable
        ? 'assets/images/green_car.svg'
        : 'assets/images/red_car.svg',
      iconSize: [32, 32],
      iconAnchor: [16, 16],
      popupAnchor: [0, -16],
    });

    return L.marker([vehicle.latitude, vehicle.longitude], { icon })
      .bindPopup(`${vehicle.model} - ${vehicle.isAvailable ? 'Free' : 'Busy'}`);
  }

  // Loads vehicles and adds them to the map
  loadVehicles(map: L.Map): void {
    this.vehicleService.getActiveVehicles().subscribe({
      next: vehicles => {
        if (!vehicles || vehicles.length === 0) return;

        const markers = vehicles.map(v => this.createVehicleMarker(v));
        const group = L.featureGroup(markers).addTo(map);

        map.fitBounds(group.getBounds(), { padding: [50, 50] });
      },
      error: err => console.error(err)
    });
  }
}
