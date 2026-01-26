import { Component, OnInit, OnDestroy } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { Router, RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSlideToggleModule, MatSlideToggleChange } from '@angular/material/slide-toggle';
import { AuthService } from '../../service/auth-service/auth.service';
import { DriverService } from '../../driver/service/driver.service';

const DRIVER_STATUS_STORAGE_KEY = 'driverActiveStatus';

@Component({
  selector: 'app-driver-nav-bar',
  imports: [
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    RouterLink,
    MatMenuModule,
    MatSlideToggleModule,
  ],
  templateUrl: './driver-nav-bar.component.html',
  styleUrl: './driver-nav-bar.component.css',
})
export class DriverNavBarComponent implements OnInit, OnDestroy {
  isActive = true;
  private isLoadingStatus = false;
  initialChecked = false;

  private storageListener = (event: StorageEvent) => {
    if (event.key === DRIVER_STATUS_STORAGE_KEY && this.auth.role() === 'DRIVER') {
      this.refreshStatusFromBackend();
    }
  };

  constructor(
    public auth: AuthService,
    private router: Router,
    private driverService: DriverService
  ) {}

  ngOnInit(): void {
    if (this.auth.role() !== 'DRIVER') {
      return;
    }

    window.addEventListener('storage', this.storageListener);

    this.refreshStatusFromBackend();
  }

  ngOnDestroy(): void {
    window.removeEventListener('storage', this.storageListener);
  }

  private refreshStatusFromBackend(): void {
    this.isLoadingStatus = true;

    this.driverService.getDriverActive().subscribe({
      next: (active) => {
        this.isActive = active;
        this.initialChecked = active;
        this.isLoadingStatus = false;
        localStorage.setItem(DRIVER_STATUS_STORAGE_KEY, String(active));
      },
      error: (err) => {
        console.error('Failed to fetch driver activity status', err);
        this.isActive = true;
        this.initialChecked = true;
        this.isLoadingStatus = false;
        localStorage.setItem(DRIVER_STATUS_STORAGE_KEY, 'true');
      }
    });
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/']);
  }

  get fullName(): string {
    return this.auth.fullName();
  }

  onToggleChange(event: MatSlideToggleChange): void {
    if (this.auth.role() !== 'DRIVER') {
      return;
    }
    if (this.isLoadingStatus) {
      event.source.checked = this.initialChecked;
      return;
    }

    const requestedState = event.checked;
    const previousState = this.isActive;

    this.isLoadingStatus = true;

    this.driverService.setDriverActive(requestedState).subscribe({
      next: () => {
        this.isActive = requestedState;
        this.initialChecked = requestedState;
        this.isLoadingStatus = false;

        localStorage.setItem(DRIVER_STATUS_STORAGE_KEY, String(requestedState));
      },
      error: (err) => {
        console.error('Failed to change driver activity', err);
        this.isActive = previousState;
        this.initialChecked = previousState;
        event.source.checked = previousState;
        this.isLoadingStatus = false;
      },
    });
  }
}
