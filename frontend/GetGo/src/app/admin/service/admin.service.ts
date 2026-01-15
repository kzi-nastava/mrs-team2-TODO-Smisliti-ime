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
}
