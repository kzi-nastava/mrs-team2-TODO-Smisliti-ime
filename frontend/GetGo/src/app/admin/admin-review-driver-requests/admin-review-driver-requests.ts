import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminNavBarComponent } from '../../layout/admin-nav-bar/admin-nav-bar.component';

@Component({
  selector: 'app-admin-review-driver-requests',
  standalone: true,
  imports: [CommonModule, AdminNavBarComponent],
  templateUrl: './admin-review-driver-requests.html',
  styleUrl: './admin-review-driver-requests.css',
})
export class AdminReviewDriverRequests {
  activeFilter: string = 'all';

  setFilter(filter: string): void {
    this.activeFilter = filter;
    console.log('Filter set to:', filter);
    // Will filter requests when connected to backend
  }
}
