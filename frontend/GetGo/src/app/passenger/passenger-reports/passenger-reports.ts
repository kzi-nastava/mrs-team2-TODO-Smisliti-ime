import { Component, ViewChild, ElementRef, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportService, ReportResponseDTO } from '../../service/report/report.service';
import { NavBarComponent } from '../../layout/nav-bar/nav-bar.component';
import {
  Chart,
  BarController,
  BarElement,
  CategoryScale,
  LinearScale,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';

Chart.register(BarController, BarElement, CategoryScale, LinearScale, Title, Tooltip, Legend);

@Component({
  selector: 'app-passenger-reports',
  standalone: true,
  imports: [CommonModule, FormsModule, NavBarComponent],
  templateUrl: './passenger-reports.html',
  styleUrl: './passenger-reports.css',
})
export class PassengerReports {
  startDate: string = '';
  endDate: string = '';
  report: ReportResponseDTO | null = null;
  isLoading = false;
  errorMessage: string | null = null;

  @ViewChild('ridesChart') ridesChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('kmChart') kmChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('moneyChart') moneyChartRef!: ElementRef<HTMLCanvasElement>;

  private ridesChart: Chart | null = null;
  private kmChart: Chart | null = null;
  private moneyChart: Chart | null = null;

  constructor(
    private reportService: ReportService,
    private cdr: ChangeDetectorRef
  ) {}

  generateReport(): void {
    if (!this.startDate || !this.endDate) {
      this.errorMessage = 'Please select both start and end date.';
      return;
    }

    const start = this.formatDate(this.startDate);
    const end = this.formatDate(this.endDate);

    this.errorMessage = null;
    this.isLoading = true;
    this.report = null;
    this.destroyCharts();

    this.reportService.getPassengerReport(start, end).subscribe({
      next: (data) => {
        this.report = data;
        this.isLoading = false;
        this.errorMessage = null;
        this.cdr.detectChanges();
        this.renderCharts();
      },
      error: (err) => {
        console.error('Report error:', err);
        this.isLoading = false;
        this.report = null;
        this.errorMessage = err.error?.message || err.message || 'Failed to load report. Please try again.';
        this.cdr.detectChanges();
      },
    });
  }

  private renderCharts(): void {
    if (!this.report) return;

    if (!this.ridesChartRef?.nativeElement) {
      return;
    }

    const labels = this.report.dailyStats.map(d => this.formatDisplayDate(d.date));
    const rides = this.report.dailyStats.map(d => d.numberOfRides);
    const km = this.report.dailyStats.map(d => d.totalKilometers);
    const money = this.report.dailyStats.map(d => d.totalMoney);

    this.destroyCharts();

    this.ridesChart = this.createBarChart(
      this.ridesChartRef.nativeElement, labels, rides, 'Rides per Day', '#133E87'
    );
    this.kmChart = this.createBarChart(
      this.kmChartRef.nativeElement, labels, km, 'Kilometers per Day', '#2E7D32'
    );
    this.moneyChart = this.createBarChart(
      this.moneyChartRef.nativeElement, labels, money, 'Money Spent per Day', '#E65100'
    );
  }

  private createBarChart(
    canvas: HTMLCanvasElement, labels: string[], data: number[], label: string, color: string
  ): Chart {
    return new Chart(canvas, {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          label,
          data,
          backgroundColor: color + '99',
          borderColor: color,
          borderWidth: 1,
          borderRadius: 4,
        }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          title: { display: true, text: label, color: '#133E87', font: { size: 14, weight: 'bold' } },
        },
        scales: {
          x: { ticks: { color: '#133E87', maxRotation: 45 } },
          y: { beginAtZero: true, ticks: { color: '#133E87' } },
        },
      },
    });
  }

  private destroyCharts(): void {
    this.ridesChart?.destroy();
    this.kmChart?.destroy();
    this.moneyChart?.destroy();
  }

  private formatDate(isoDate: string): string {
    const parts = isoDate.split('-');
    return `${parts[2]}-${parts[1]}-${parts[0]}`;
  }

  private formatDisplayDate(dateStr: string): string {
    const parts = dateStr.split('-');
    return `${parts[2]}.${parts[1]}.`;
  }
}
