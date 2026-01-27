import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../env/environment';

export interface GetPassengerDTO {
  id: number;
  email: string;
  name: string;
  surname: string;
  phone: string;
  address: string;
  profilePictureUrl: string;
}

export interface UpdatePassengerDTO {
  name: string;
  surname: string;
  phone: string;
  address: string;
}

export interface UpdatedPassengerDTO {
  id: number;
  email: string;
  name: string;
  surname: string;
  phone: string;
  address: string;
  profilePictureUrl: string;
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

export interface UpdatedProfilePictureDTO {
  pictureUrl: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class PassengerService {
  private apiUrl = `${environment.apiHost}/api/passenger`;

  constructor(private http: HttpClient) {}

  getProfile(): Observable<GetPassengerDTO> {
    return this.http.get<GetPassengerDTO>(`${this.apiUrl}/profile`);
  }

  updateProfile(updateData: UpdatePassengerDTO): Observable<UpdatedPassengerDTO> {
    return this.http.put<UpdatedPassengerDTO>(`${this.apiUrl}/profile`, updateData);
  }

  updatePassword(passwordData: UpdatePasswordDTO): Observable<UpdatedPasswordDTO> {
    return this.http.put<UpdatedPasswordDTO>(`${this.apiUrl}/profile/password`, passwordData);
  }

  uploadProfilePicture(file: File): Observable<UpdatedProfilePictureDTO> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<UpdatedProfilePictureDTO>(`${this.apiUrl}/profile/picture`, formData);
  }
}
