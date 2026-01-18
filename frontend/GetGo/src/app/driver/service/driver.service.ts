import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../env/environment';

export interface ValidateTokenDTO {
  valid: boolean;
  email: string;
  reason?: string;
}

export interface SetPasswordDTO {
  token: string;
  password: string;
  confirmPassword: string;
}

export interface SetPasswordResponseDTO {
  success: boolean;
  message: string;
}

export interface GetDriverDTO {
  id: number;
  email: string;
  name: string;
  surname: string;
  phone: string;
  address: string;
  profilePictureUrl: string;
  vehicleModel: string;
  vehicleType: string;
  vehicleLicensePlate: string;
  vehicleSeats: number;
  vehicleHasBabySeats: boolean;
  vehicleAllowsPets: boolean;
  recentHoursWorked: number;
}

export interface UpdateDriverPersonalDTO {
  name: string;
  surname: string;
  phone: string;
  address: string;
}

export interface UpdateDriverVehicleDTO {
  vehicleModel: string;
  vehicleType: string;
  vehicleLicensePlate: string;
  vehicleSeats: number;
  vehicleHasBabySeats: boolean;
  vehicleAllowsPets: boolean;
}

export interface CreatedDriverChangeRequestDTO {
  requestId: number;
  driverId: number;
  status: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class DriverService {
  private apiUrl = `${environment.apiHost}/api/drivers`;

  constructor(private http: HttpClient) {}

  validateActivationToken(token: string): Observable<ValidateTokenDTO> {
    return this.http.get<ValidateTokenDTO>(`${this.apiUrl}/activate/${token}`);
  }

  setDriverPassword(data: SetPasswordDTO): Observable<SetPasswordResponseDTO> {
    return this.http.post<SetPasswordResponseDTO>(`${this.apiUrl}/activate`, data);
  }

  getProfile(): Observable<GetDriverDTO> {
      return this.http.get<GetDriverDTO>(`${this.apiUrl}/profile`);
  }

  requestPersonalInfoChange(data: UpdateDriverPersonalDTO): Observable<CreatedDriverChangeRequestDTO> {
    return this.http.post<CreatedDriverChangeRequestDTO>(`${this.apiUrl}/profile/change-requests/personal`, data);
  }

  requestVehicleInfoChange(data: UpdateDriverVehicleDTO): Observable<CreatedDriverChangeRequestDTO> {
    return this.http.post<CreatedDriverChangeRequestDTO>(`${this.apiUrl}/profile/change-requests/vehicle`, data);
  }

  requestProfilePictureChange(file: File): Observable<CreatedDriverChangeRequestDTO> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<CreatedDriverChangeRequestDTO>(`${this.apiUrl}/profile/change-requests/picture`, formData);
  }

}
