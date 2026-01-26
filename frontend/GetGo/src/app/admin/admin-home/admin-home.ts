import { Component, AfterViewInit, OnDestroy, ElementRef, ViewChild } from '@angular/core';
import { AdminNavBarComponent } from '../../layout/admin-nav-bar/admin-nav-bar.component';
import { MapComponent } from '../../layout/map/map.component';

@Component({
  selector: 'app-admin-home',
  standalone: true,
  imports: [AdminNavBarComponent, MapComponent],
  templateUrl: './admin-home.html',
  styleUrl: './admin-home.css',
})
export class AdminHome implements AfterViewInit, OnDestroy {
  @ViewChild('adminMap', { read: ElementRef, static: false })
  private mapEl?: ElementRef<HTMLElement>;

  private mapClickListener?: (ev: Event) => void;

  ngAfterViewInit(): void {
    this.mapClickListener = (ev: Event) => this.handleMapClick(ev);
    if (this.mapEl?.nativeElement && this.mapClickListener) {
      this.mapEl.nativeElement.addEventListener('map-click', this.mapClickListener as EventListener);
    }
  }

  ngOnDestroy(): void {
    if (this.mapEl?.nativeElement && this.mapClickListener) {
      this.mapEl.nativeElement.removeEventListener('map-click', this.mapClickListener as EventListener);
    }
  }

  private handleMapClick(ev: Event): void {
  }
}
