import { Injectable } from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import { GetVehicleDTO } from './get-vehicle-dto.interface';


@Injectable({
  providedIn: 'root',
})
export class VehicleService {
  private baseUrl = 'http://localhost:8080/api/vehicles';

  constructor(private http: HttpClient) { }

  getActiveVehicles(): Observable<GetVehicleDTO[]> {
    return this.http.get<GetVehicleDTO[]>(`${this.baseUrl}/active`);
  }
}
