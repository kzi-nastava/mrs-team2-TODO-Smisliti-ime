import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from "../../../env/environment"

export interface GetAdminDTO {
  id: number;
  email: string;
  name: string;
  surname: string;
  phone: string;
  address: string;
}
export interface UpdateAdminDTO {
  name: string;
  surname: string;
  phone: string;
  address: string;
}

export interface UpdatedAdminDTO {
  id: number;
  email: string;
  name: string;
  surname: string;
  phone: string;
  address: string;
}

export interface UpdatePasswordDTO {
  oldPassword: string;
  password: string;
  confirmPassword: string;
}

export interface UpdatedPasswordDTO {
  success: boolean;
  message: string;
}

export interface CreateDriverDTO {
  email: string;
  name: string;
  surname: string;
  phone: string;
  address: string;
  vehicleModel: string;
  vehicleType: string;
  vehicleLicensePlate: string;
  vehicleSeats: number;
  vehicleHasBabySeats: boolean;
  vehicleAllowsPets: boolean;
}

export interface CreatedDriverDTO {
  id: number;
  email: string;
  name: string;
  surname: string;
  address: string;
}

export interface GetPersonalChangeRequestDTO {
  requestId: number;
  driverId: number;
  driverEmail: string;
  driverName: string;
  currentName: string;
  currentSurname: string;
  currentPhone: string;
  currentAddress: string;
  requestedName: string;
  requestedSurname: string;
  requestedPhone: string;
  requestedAddress: string;
  status: string;
  createdAt: string;
}

export interface GetVehicleChangeRequestDTO {
  requestId: number;
  driverId: number;
  driverEmail: string;
  driverName: string;
  currentVehicleModel: string;
  currentVehicleType: string;
  currentVehicleLicensePlate: string;
  currentVehicleSeats: number;
  currentVehicleHasBabySeats: boolean;
  currentVehicleAllowsPets: boolean;
  requestedVehicleModel: string;
  requestedVehicleType: string;
  requestedVehicleLicensePlate: string;
  requestedVehicleSeats: number;
  requestedVehicleHasBabySeats: boolean;
  requestedVehicleAllowsPets: boolean;
  status: string;
  createdAt: string;
}

export interface GetAvatarChangeRequestDTO {
  requestId: number;
  driverId: number;
  driverEmail: string;
  driverName: string;
  currentProfilePictureUrl: string;
  requestedProfilePictureUrl: string;
  status: string;
  createdAt: string;
}

export interface RejectRequestDTO {
  reason: string;
}

export interface ApproveRejectResponseDTO {
  requestId: number;
  driverId: number;
  status: string;
  reviewedBy: number;
  reviewedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = `${environment.apiHost}/api/admin`;

  constructor(private http: HttpClient) {}

  getProfile(): Observable<GetAdminDTO> {
    return this.http.get<GetAdminDTO>(`${this.apiUrl}/profile`);
  }

  updateProfile(updateData: UpdateAdminDTO): Observable<UpdatedAdminDTO> {
    return this.http.put<UpdatedAdminDTO>(`${this.apiUrl}/profile`, updateData);
  }

  updatePassword(passwordData: UpdatePasswordDTO): Observable<UpdatedPasswordDTO> {
    return this.http.put<UpdatedPasswordDTO>(`${this.apiUrl}/profile/password`, passwordData);
  }

  registerDriver(driverData: CreateDriverDTO): Observable<CreatedDriverDTO> {
    return this.http.post<CreatedDriverDTO>(`${this.apiUrl}/drivers/register`, driverData);
  }

  getPendingPersonalChangeRequests(): Observable<GetPersonalChangeRequestDTO[]> {
    return this.http.get<GetPersonalChangeRequestDTO[]>(`${this.apiUrl}/driver-change-requests/personal`);
  }

  getPendingVehicleChangeRequests(): Observable<GetVehicleChangeRequestDTO[]> {
    return this.http.get<GetVehicleChangeRequestDTO[]>(`${this.apiUrl}/driver-change-requests/vehicle`);
  }

  getPendingAvatarChangeRequests(): Observable<GetAvatarChangeRequestDTO[]> {
    return this.http.get<GetAvatarChangeRequestDTO[]>(`${this.apiUrl}/driver-change-requests/picture`);
  }

  approvePersonalChangeRequest(requestId: number): Observable<ApproveRejectResponseDTO> {
    return this.http.put<ApproveRejectResponseDTO>(`${this.apiUrl}/driver-change-requests/personal/${requestId}/approve`, {});
  }

  approveVehicleChangeRequest(requestId: number): Observable<ApproveRejectResponseDTO> {
    return this.http.put<ApproveRejectResponseDTO>(`${this.apiUrl}/driver-change-requests/vehicle/${requestId}/approve`, {});
  }

  approveAvatarChangeRequest(requestId: number): Observable<ApproveRejectResponseDTO> {
    return this.http.put<ApproveRejectResponseDTO>(`${this.apiUrl}/driver-change-requests/picture/${requestId}/approve`, {});
  }

  rejectPersonalChangeRequest(requestId: number, reason: string): Observable<ApproveRejectResponseDTO> {
    return this.http.put<ApproveRejectResponseDTO>(
      `${this.apiUrl}/driver-change-requests/personal/${requestId}/reject`,
      { reason }
    );
  }

  rejectVehicleChangeRequest(requestId: number, reason: string): Observable<ApproveRejectResponseDTO> {
    return this.http.put<ApproveRejectResponseDTO>(
      `${this.apiUrl}/driver-change-requests/vehicle/${requestId}/reject`,
      { reason }
    );
  }

  rejectAvatarChangeRequest(requestId: number, reason: string): Observable<ApproveRejectResponseDTO> {
    return this.http.put<ApproveRejectResponseDTO>(
      `${this.apiUrl}/driver-change-requests/picture/${requestId}/reject`,
      { reason }
    );
  }

}
