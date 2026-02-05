import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../env/environment';
import { PanicAlertDTO } from '../../model/panic/panic-alert.model';

@Injectable({
  providedIn: 'root'
})
export class PanicService {
  private apiUrl = `${environment.apiHost}/api/panic`;

  constructor(private http: HttpClient) {}

  getUnreadPanics(): Observable<PanicAlertDTO[]> {
    return this.http.get<PanicAlertDTO[]>(`${this.apiUrl}/admin/unread`);
  }

  markRead(panicId: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/admin/read/${panicId}`, {});
  }

  markAllRead(): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/admin/read-all`, {});
  }
}
