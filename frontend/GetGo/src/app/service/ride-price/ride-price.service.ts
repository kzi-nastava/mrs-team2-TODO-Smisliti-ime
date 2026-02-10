import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../env/environment';

@Injectable({
  providedIn: 'root',
})
export class RidePriceService {
  private apiUrl = `${environment.apiHost}/api/ride-price`;

  constructor(private http: HttpClient) {}

  updatePrice(vehicleType: string, price: number) {
    return this.http.put(`${this.apiUrl}/prices/${vehicleType}`,{ pricePerKm: price });
  }

  getPrice(vehicleType: string) {
    return this.http.get<number>(`${this.apiUrl}/prices/${vehicleType}`);
  }

}
