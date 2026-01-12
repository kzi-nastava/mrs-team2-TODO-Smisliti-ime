import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface GetAdminDTO {
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
  private apiUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) {}

  getProfile(): Observable<GetAdminDTO> {
    return this.http.get<GetAdminDTO>(`${this.apiUrl}/profile`);
  }
}