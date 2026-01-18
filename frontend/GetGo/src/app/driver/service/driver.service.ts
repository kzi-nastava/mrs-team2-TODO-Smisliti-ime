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
}
