import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../env/environment';

export interface DailyReportDTO {
  date: string;
  numberOfRides: number;
  totalKilometers: number;
  totalMoney: number;
}

export interface ReportResponseDTO {
  dailyStats: DailyReportDTO[];
  totalRides: number;
  totalKilometers: number;
  totalMoney: number;
  avgRidesPerDay: number;
  avgKilometersPerDay: number;
  avgMoneyPerDay: number;
}

@Injectable({
  providedIn: 'root',
})
export class ReportService {
  private apiUrl = `${environment.apiHost}/api/reports`;

  constructor(private http: HttpClient) {}

  private buildParams(startDate: string, endDate: string): HttpParams {
    return new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
  }

  getDriverReport(startDate: string, endDate: string): Observable<ReportResponseDTO> {
    return this.http.get<ReportResponseDTO>(`${this.apiUrl}/driver`, {
      params: this.buildParams(startDate, endDate),
    });
  }

  getPassengerReport(startDate: string, endDate: string): Observable<ReportResponseDTO> {
    return this.http.get<ReportResponseDTO>(`${this.apiUrl}/passenger`, {
      params: this.buildParams(startDate, endDate),
    });
  }

  getAdminDriversReport(startDate: string, endDate: string): Observable<ReportResponseDTO> {
    return this.http.get<ReportResponseDTO>(`${this.apiUrl}/admin/drivers`, {
      params: this.buildParams(startDate, endDate),
    });
  }

  getAdminPassengersReport(startDate: string, endDate: string): Observable<ReportResponseDTO> {
    return this.http.get<ReportResponseDTO>(`${this.apiUrl}/admin/passengers`, {
      params: this.buildParams(startDate, endDate),
    });
  }

  getAdminDriverReport(email: string, startDate: string, endDate: string): Observable<ReportResponseDTO> {
    return this.http.get<ReportResponseDTO>(`${this.apiUrl}/admin/driver`, {
      params: this.buildParams(startDate, endDate).set('email', email),
    });
  }

  getAdminPassengerReport(email: string, startDate: string, endDate: string): Observable<ReportResponseDTO> {
    return this.http.get<ReportResponseDTO>(`${this.apiUrl}/admin/passenger`, {
      params: this.buildParams(startDate, endDate).set('email', email),
    });
  }
}
