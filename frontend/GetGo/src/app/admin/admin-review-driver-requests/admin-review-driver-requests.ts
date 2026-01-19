import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminNavBarComponent } from '../../layout/admin-nav-bar/admin-nav-bar.component';
import { AdminService, GetPersonalChangeRequestDTO, GetVehicleChangeRequestDTO, GetAvatarChangeRequestDTO } from '../service/admin.service';
import { forkJoin } from 'rxjs';
import { environment } from '../../../env/environment';

interface Change {
  label: string;
  oldValue: string;
  newValue: string;
}

interface UnifiedRequest {
  id: number;
  type: 'Personal Info' | 'Vehicle Info' | 'Profile Picture';
  date: string;
  driverName: string;
  driverEmail: string;
  changes?: Change[];
  currentPicture?: string;
  newPicture?: string;
}

@Component({
  selector: 'app-admin-review-driver-requests',
  standalone: true,
  imports: [CommonModule, AdminNavBarComponent],
  templateUrl: './admin-review-driver-requests.html',
  styleUrl: './admin-review-driver-requests.css',
})
export class AdminReviewDriverRequests implements OnInit {
  allRequests: UnifiedRequest[] = [];
  paginatedRequests: UnifiedRequest[] = [];
  isLoading: boolean = true;

  // Pagination
  currentPage: number = 1;
  itemsPerPage: number = 3;
  totalRequests: number = 0;
  totalPages: number = 0;
  startIndex: number = 0;
  endIndex: number = 0;

  constructor(
    private adminService: AdminService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadAllRequests();
  }

  loadAllRequests(): void {
    this.isLoading = true;

    forkJoin({
      personal: this.adminService.getPendingPersonalChangeRequests(),
      vehicle: this.adminService.getPendingVehicleChangeRequests(),
      avatar: this.adminService.getPendingAvatarChangeRequests()
    }).subscribe({
      next: (results) => {
        this.allRequests = [];

        // Process Personal requests
        results.personal.forEach(req => {
          const changes: Change[] = [];

          if (req.currentName !== req.requestedName) {
            changes.push({ label: 'Name', oldValue: req.currentName, newValue: req.requestedName });
          }
          if (req.currentSurname !== req.requestedSurname) {
            changes.push({ label: 'Surname', oldValue: req.currentSurname, newValue: req.requestedSurname });
          }
          if (req.currentPhone !== req.requestedPhone) {
            changes.push({ label: 'Phone', oldValue: req.currentPhone, newValue: req.requestedPhone });
          }
          if (req.currentAddress !== req.requestedAddress) {
            changes.push({ label: 'Address', oldValue: req.currentAddress, newValue: req.requestedAddress });
          }

          this.allRequests.push({
            id: req.requestId,
            type: 'Personal Info',
            date: this.formatDate(req.createdAt),
            driverName: req.driverName,
            driverEmail: req.driverEmail,
            changes
          });
        });

        // Process Vehicle requests
        results.vehicle.forEach(req => {
          const changes: Change[] = [];

          if (req.currentVehicleModel !== req.requestedVehicleModel) {
            changes.push({ label: 'Model', oldValue: req.currentVehicleModel, newValue: req.requestedVehicleModel });
          }
          if (req.currentVehicleType !== req.requestedVehicleType) {
            changes.push({ label: 'Type', oldValue: req.currentVehicleType, newValue: req.requestedVehicleType });
          }
          if (req.currentVehicleLicensePlate !== req.requestedVehicleLicensePlate) {
            changes.push({ label: 'License Plate', oldValue: req.currentVehicleLicensePlate, newValue: req.requestedVehicleLicensePlate });
          }
          if (req.currentVehicleSeats !== req.requestedVehicleSeats) {
            changes.push({ label: 'Seats', oldValue: req.currentVehicleSeats.toString(), newValue: req.requestedVehicleSeats.toString() });
          }
          if (req.currentVehicleHasBabySeats !== req.requestedVehicleHasBabySeats) {
            changes.push({ label: 'Allows Babies', oldValue: req.currentVehicleHasBabySeats ? 'Yes' : 'No', newValue: req.requestedVehicleHasBabySeats ? 'Yes' : 'No' });
          }
          if (req.currentVehicleAllowsPets !== req.requestedVehicleAllowsPets) {
            changes.push({ label: 'Allows Pets', oldValue: req.currentVehicleAllowsPets ? 'Yes' : 'No', newValue: req.requestedVehicleAllowsPets ? 'Yes' : 'No' });
          }

          this.allRequests.push({
            id: req.requestId,
            type: 'Vehicle Info',
            date: this.formatDate(req.createdAt),
            driverName: req.driverName,
            driverEmail: req.driverEmail,
            changes
          });
        });

        // Process Avatar requests
        results.avatar.forEach(req => {
          this.allRequests.push({
            id: req.requestId,
            type: 'Profile Picture',
            date: this.formatDate(req.createdAt),
            driverName: req.driverName,
            driverEmail: req.driverEmail,
            currentPicture: req.currentProfilePictureUrl ? `${environment.apiHost}${req.currentProfilePictureUrl}` : 'assets/images/pfp.png',
            newPicture: req.requestedProfilePictureUrl ? `${environment.apiHost}${req.requestedProfilePictureUrl}` : 'assets/images/pfp.png'
          });
        });

        // Sort by date (newest first)
        this.allRequests.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());

        this.totalRequests = this.allRequests.length;
        this.currentPage = 1; // Reset to first page
        this.calculatePagination();
        this.updatePaginatedRequests();

        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading requests:', error);
        this.isLoading = false;
        alert('Failed to load change requests');
      }
    });
  }

  calculatePagination(): void {
    this.totalPages = Math.ceil(this.totalRequests / this.itemsPerPage);
    this.startIndex = (this.currentPage - 1) * this.itemsPerPage;
    this.endIndex = Math.min(this.startIndex + this.itemsPerPage, this.totalRequests);
  }

  updatePaginatedRequests(): void {
    this.paginatedRequests = this.allRequests.slice(this.startIndex, this.endIndex);
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.calculatePagination();
      this.updatePaginatedRequests();
    }
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.calculatePagination();
      this.updatePaginatedRequests();
    }
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  }

  onApprove(request: UnifiedRequest): void {
    const confirmMsg = `Are you sure you want to approve this ${request.type} change request for ${request.driverName}?`;
    if (!confirm(confirmMsg)) return;

    let approveObservable;

    if (request.type === 'Personal Info') {
      approveObservable = this.adminService.approvePersonalChangeRequest(request.id);
    } else if (request.type === 'Vehicle Info') {
      approveObservable = this.adminService.approveVehicleChangeRequest(request.id);
    } else {
      approveObservable = this.adminService.approveAvatarChangeRequest(request.id);
    }

    approveObservable.subscribe({
      next: (response) => {
        alert(`Request approved successfully!`);
        this.loadAllRequests(); // Reload to remove approved request
      },
      error: (error) => {
        console.error('Error approving request:', error);
        alert('Failed to approve request');
      }
    });
  }

  onReject(request: UnifiedRequest): void {
    const reason = prompt(`Please enter the reason for rejecting this ${request.type} change request for ${request.driverName}:`);

    if (!reason || reason.trim() === '') {
      alert('Rejection reason is required');
      return;
    }

    let rejectObservable;

    if (request.type === 'Personal Info') {
      rejectObservable = this.adminService.rejectPersonalChangeRequest(request.id, reason);
    } else if (request.type === 'Vehicle Info') {
      rejectObservable = this.adminService.rejectVehicleChangeRequest(request.id, reason);
    } else {
      rejectObservable = this.adminService.rejectAvatarChangeRequest(request.id, reason);
    }

    rejectObservable.subscribe({
      next: (response) => {
        alert(`Request rejected successfully!`);
        this.loadAllRequests(); // Reload to remove rejected request
      },
      error: (error) => {
        console.error('Error rejecting request:', error);
        alert('Failed to reject request');
      }
    });
  }
}
