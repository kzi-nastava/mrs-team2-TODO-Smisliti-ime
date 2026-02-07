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

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
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

  getPendingPersonalChangeRequests(page: number = 0, size: number = 10): Observable<Page<GetPersonalChangeRequestDTO>> {
    return this.http.get<Page<GetPersonalChangeRequestDTO>>(`${this.apiUrl}/driver-change-requests/personal?page=${page}&size=${size}`);
  }

  getPendingVehicleChangeRequests(page: number = 0, size: number = 10): Observable<Page<GetVehicleChangeRequestDTO>> {
    return this.http.get<Page<GetVehicleChangeRequestDTO>>(`${this.apiUrl}/driver-change-requests/vehicle?page=${page}&size=${size}`);
  }

  getPendingAvatarChangeRequests(page: number = 0, size: number = 10): Observable<Page<GetAvatarChangeRequestDTO>> {
    return this.http.get<Page<GetAvatarChangeRequestDTO>>(`${this.apiUrl}/driver-change-requests/picture?page=${page}&size=${size}`);
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
