import { Injectable } from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import { GetVehicleDTO } from './get-vehicle-dto.interface';
import { environment } from '../../../env/environment';

@Injectable({
  providedIn: 'root',
})
export class VehicleService {
  private baseUrl = `${environment.apiHost}/api/vehicles`;

  constructor(private http: HttpClient) { }

  getActiveVehicles(): Observable<GetVehicleDTO[]> {
    return this.http.get<GetVehicleDTO[]>(`${this.baseUrl}/active`);
  }

  getVehicleTypes() {
    return this.http.get<string[]>(`${this.baseUrl}/types`);
  }
}
