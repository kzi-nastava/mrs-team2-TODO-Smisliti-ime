import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../env/environment';

@Injectable({
providedIn: 'root',
})
export class RidePriceService {
  private apiUrl = `${environment.apiHost}/api/ride-price`;

  constructor(private http: HttpClient) {}

  getPrice(vehicleType: string) {
    return this.http.get<{ pricePerKm?: number; startPrice?: number }>(`${this.apiUrl}/prices/${vehicleType}`);
  }

  updatePrice(vehicleType: string, data: { pricePerKm: number | null; startPrice: number | null }) {
    return this.http.put(`${this.apiUrl}/prices/${vehicleType}`, data);
  }
}
