import { Injectable } from '@angular/core';
import * as L from 'leaflet';
import { GetRideDTO } from '../model/ride.model';

@Injectable({
  providedIn: 'root'
})
export class RideHistoryMapHelper {

  public initializeMap(map: L.Map, ride: GetRideDTO): void {
    if (!ride) return;

    try {
      console.log('FULL RIDE OBJECT:', ride);
      console.log('ROUTE FIELD:', ride.route);

      // Use route data if available
      if (ride.route) {
        console.log('Drawing route from route object');
        const routeArray = Array.isArray(ride.route) ? ride.route : [ride.route];
        this.drawRouteFromRouteData(map, routeArray);
      } else {
        console.log('Drawing route using geocoding');
        this.geocodeAndDrawRoute(map, ride.startPoint, ride.endPoint);
      }
    } catch (error) {
      console.error('Error initializing map:', error);
    }
  }

  private drawRouteFromRouteData(map: L.Map, route: any[]): void {
    if (!map) return;

    const allLatLngs: L.LatLngExpression[] = [];
    const colors = ['#2196F3', '#4CAF50', '#FF9800', '#E91E63', '#9C27B0'];

    route.forEach((routePoint, index) => {
      console.log('Processing route point:', routePoint);
      console.log('Encoded polyline:', routePoint.encodedPolyline);

      // Parse polyline - check if it's JSON or encoded string
      let decodedPath: L.LatLngExpression[];

      try {
        // Try parsing as JSON array first
        const jsonCoords = JSON.parse(routePoint.encodedPolyline);
        console.log('Parsed JSON coords:', jsonCoords);

        // Backend sends [longitude, latitude] but Leaflet needs [latitude, longitude]
        decodedPath = jsonCoords.map(
          (coord: number[]) => {
            if (coord.length === 2 && typeof coord[0] === 'number' && typeof coord[1] === 'number') {
              return [coord[1], coord[0]] as L.LatLngExpression; // Swap lon/lat to lat/lon
            }
            console.error('Invalid coordinate:', coord);
            return [0, 0] as L.LatLngExpression;
          }
        ).filter((coord: L.LatLngExpression) => {
          const [lat, lng] = coord as number[];
          return lat !== 0 || lng !== 0;
        });

        console.log('Decoded path (first 3):', decodedPath.slice(0, 3));
      } catch (e) {
        console.error('JSON parsing failed, trying encoded polyline:', e);
        // If JSON parsing fails, try encoded polyline
        decodedPath = this.decodePolyline(routePoint.encodedPolyline);
      }

      if (decodedPath.length === 0) {
        console.error('No valid coordinates decoded for route segment');
        return;
      }

      allLatLngs.push(...decodedPath);

      // Draw polyline for this segment
      const polyline = L.polyline(decodedPath, {
        color: colors[index % colors.length],
        weight: 4,
        opacity: 0.7
      }).addTo(map);

      // Add waypoint markers
      if (routePoint.waypoints && routePoint.waypoints.length > 0) {
        console.log('Adding waypoint markers:', routePoint.waypoints);
        routePoint.waypoints.forEach((wp: any, wpIndex: number) => {
          const isFirst = wpIndex === 0;
          const isLast = wpIndex === routePoint.waypoints.length - 1;

          // Waypoints should already be in correct format {lat, lng}
          const lat = wp.latitude || wp.lat;
          const lng = wp.longitude || wp.lng;

          if (typeof lat !== 'number' || typeof lng !== 'number') {
            console.error('Invalid waypoint:', wp);
            return;
          }

          if (isFirst || isLast) {
            L.marker([lat, lng], {
              icon: L.icon({
                iconUrl: isFirst
                  ? 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png'
                  : 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
                shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
                iconSize: [25, 41],
                iconAnchor: [12, 41],
                popupAnchor: [1, -34],
                shadowSize: [41, 41]
              })
            }).addTo(map).bindPopup(isFirst ? routePoint.startingPoint : routePoint.endingPoint);
          }
        });
      }
    });

    // Fit map to show entire route
    if (allLatLngs.length > 0) {
      console.log('Fitting map bounds to:', allLatLngs.length, 'coordinates');
      const bounds = L.latLngBounds(allLatLngs);
      map.fitBounds(bounds, { padding: [50, 50] });
    } else {
      console.error('No coordinates to fit map bounds');
    }
  }

  private decodePolyline(encoded: string): L.LatLngExpression[] {
    const poly: L.LatLngExpression[] = [];
    let index = 0, len = encoded.length;
    let lat = 0, lng = 0;

    while (index < len) {
      let b, shift = 0, result = 0;
      do {
        b = encoded.charCodeAt(index++) - 63;
        result |= (b & 0x1f) << shift;
        shift += 5;
      } while (b >= 0x20);
      const dlat = ((result & 1) ? ~(result >> 1) : (result >> 1));
      lat += dlat;

      shift = 0;
      result = 0;
      do {
        b = encoded.charCodeAt(index++) - 63;
        result |= (b & 0x1f) << shift;
        shift += 5;
      } while (b >= 0x20);
      const dlng = ((result & 1) ? ~(result >> 1) : (result >> 1));
      lng += dlng;

      poly.push([lat / 1e5, lng / 1e5]);
    }
    return poly;
  }

  private drawRouteFromWaypoints(map: L.Map, waypoints: Array<{ lat: number; lng: number; timestamp: string }>): void {
    if (!map) return;

    const latLngs: L.LatLngExpression[] = waypoints.map(wp => [wp.lat, wp.lng]);

    // Draw polyline
    const polyline = L.polyline(latLngs, {
      color: '#2196F3',
      weight: 4,
      opacity: 0.7
    }).addTo(map);

    // Add start marker
    L.marker(latLngs[0], {
      icon: L.icon({
        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
        iconSize: [25, 41],
        iconAnchor: [12, 41],
        popupAnchor: [1, -34],
        shadowSize: [41, 41]
      })
    }).addTo(map).bindPopup('Start');

    // Add end marker
    L.marker(latLngs[latLngs.length - 1], {
      icon: L.icon({
        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
        iconSize: [25, 41],
        iconAnchor: [12, 41],
        popupAnchor: [1, -34],
        shadowSize: [41, 41]
      })
    }).addTo(map).bindPopup('End');

    // Fit map to show entire route
    map.fitBounds(polyline.getBounds(), { padding: [50, 50] });
  }

  private geocodeAndDrawRoute(map: L.Map, startPoint: string, endPoint: string): void {
    if (!map) return;

    // Simple geocoding using Nominatim (OpenStreetMap)
    const geocodeUrl = (address: string) =>
      `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address + ', Novi Sad, Serbia')}`;

    Promise.all([
      fetch(geocodeUrl(startPoint)).then(r => r.json()),
      fetch(geocodeUrl(endPoint)).then(r => r.json())
    ]).then(([startResults, endResults]) => {
      if (startResults.length > 0 && endResults.length > 0) {
        const startLat = parseFloat(startResults[0].lat);
        const startLng = parseFloat(startResults[0].lon);
        const endLat = parseFloat(endResults[0].lat);
        const endLng = parseFloat(endResults[0].lon);

        // Draw line between start and end
        const latLngs: L.LatLngExpression[] = [[startLat, startLng], [endLat, endLng]];
        const polyline = L.polyline(latLngs, {
          color: '#2196F3',
          weight: 4,
          opacity: 0.7,
          dashArray: '10, 10'
        }).addTo(map);

        // Add markers
        L.marker([startLat, startLng], {
          icon: L.icon({
            iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
            shadowSize: [41, 41]
          })
        }).addTo(map).bindPopup(`Start: ${startPoint}`);

        L.marker([endLat, endLng], {
          icon: L.icon({
            iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
            shadowSize: [41, 41]
          })
        }).addTo(map).bindPopup(`End: ${endPoint}`);

        map.fitBounds(polyline.getBounds(), { padding: [50, 50] });
      }
    }).catch(err => {
      console.error('Error geocoding addresses:', err);
    });
  }
}
