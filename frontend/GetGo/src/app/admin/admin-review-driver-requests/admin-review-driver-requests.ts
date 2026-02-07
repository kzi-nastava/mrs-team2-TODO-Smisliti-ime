import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs';
import { AdminNavBarComponent } from '../../layout/admin-nav-bar/admin-nav-bar.component';
import { AdminService } from '../service/admin.service';
import { environment } from '../../../env/environment';

interface Change {
  label: string;
  oldValue: string;
  newValue: string;
}

interface PersonalRequest {
  id: number;
  type: 'Personal Info';
  date: string;
  driverName: string;
  driverEmail: string;
  changes: Change[];
}

interface VehicleRequest {
  id: number;
  type: 'Vehicle Info';
  date: string;
  driverName: string;
  driverEmail: string;
  changes: Change[];
}

interface AvatarRequest {
  id: number;
  type: 'Profile Picture';
  date: string;
  driverName: string;
  driverEmail: string;
  currentPicture: string;
  newPicture: string;
}

type RequestType = PersonalRequest | VehicleRequest | AvatarRequest;

@Component({
  selector: 'app-admin-review-driver-requests',
  standalone: true,
  imports: [CommonModule, AdminNavBarComponent],
  templateUrl: './admin-review-driver-requests.html',
  styleUrl: './admin-review-driver-requests.css',
})
export class AdminReviewDriverRequests implements OnInit {
  activeTab: 'personal' | 'vehicle' | 'avatar' = 'personal';

  requests: RequestType[] = [];
  isLoading = false;

  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  itemsPerPage = 2;

  personalTotal = 0;
  vehicleTotal = 0;
  avatarTotal = 0;

  constructor(
    private adminService: AdminService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadTotals();
    this.loadRequests();
  }

  loadTotals(): void {
    this.adminService.getPendingPersonalChangeRequests(0, 1).subscribe({
      next: (page) => this.personalTotal = page.totalElements
    });
    this.adminService.getPendingVehicleChangeRequests(0, 1).subscribe({
      next: (page) => this.vehicleTotal = page.totalElements
    });
    this.adminService.getPendingAvatarChangeRequests(0, 1).subscribe({
      next: (page) => this.avatarTotal = page.totalElements
    });
  }

  setActiveTab(tab: 'personal' | 'vehicle' | 'avatar'): void {
    this.activeTab = tab;
    this.currentPage = 0;
    this.loadRequests();
  }

  loadRequests(): void {
    this.isLoading = true;

    const loader: Observable<any> = this.activeTab === 'personal'
      ? this.adminService.getPendingPersonalChangeRequests(this.currentPage, this.itemsPerPage)
      : this.activeTab === 'vehicle'
      ? this.adminService.getPendingVehicleChangeRequests(this.currentPage, this.itemsPerPage)
      : this.adminService.getPendingAvatarChangeRequests(this.currentPage, this.itemsPerPage);

    loader.subscribe({
      next: (page: any) => {
        this.requests = this.mapRequests(page.content);
        this.totalPages = page.totalPages;
        this.totalElements = page.totalElements;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (error: any) => {
        console.error('Error loading requests:', error);
        this.isLoading = false;
      }
    });
  }

  private mapRequests(content: any[]): RequestType[] {
    if (this.activeTab === 'personal') {
      return content.map(req => ({
        id: req.requestId,
        type: 'Personal Info' as const,
        date: this.formatDate(req.createdAt),
        driverName: req.driverName,
        driverEmail: req.driverEmail,
        changes: this.getPersonalChanges(req)
      }));
    }

    if (this.activeTab === 'vehicle') {
      return content.map(req => ({
        id: req.requestId,
        type: 'Vehicle Info' as const,
        date: this.formatDate(req.createdAt),
        driverName: req.driverName,
        driverEmail: req.driverEmail,
        changes: this.getVehicleChanges(req)
      }));
    }

    return content.map(req => ({
      id: req.requestId,
      type: 'Profile Picture' as const,
      date: this.formatDate(req.createdAt),
      driverName: req.driverName,
      driverEmail: req.driverEmail,
      currentPicture: req.currentProfilePictureUrl ? `${environment.apiHost}${req.currentProfilePictureUrl}` : 'assets/images/pfp.png',
      newPicture: req.requestedProfilePictureUrl ? `${environment.apiHost}${req.requestedProfilePictureUrl}` : 'assets/images/pfp.png'
    }));
  }

  private getPersonalChanges(req: any): Change[] {
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
    return changes;
  }

  private getVehicleChanges(req: any): Change[] {
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
    return changes;
  }

  get currentRequests(): RequestType[] {
    return this.requests;
  }

  get currentTotal(): number {
    return this.totalElements;
  }

  get currentTotalPages(): number {
    return this.totalPages;
  }

  get startIndex(): number {
    return this.currentPage * this.itemsPerPage;
  }

  get endIndex(): number {
    return Math.min((this.currentPage + 1) * this.itemsPerPage, this.totalElements);
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadRequests();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadRequests();
    }
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  }

  onApprove(request: RequestType): void {
    if (!confirm(`Are you sure you want to approve this ${request.type} change request for ${request.driverName}?`)) {
      return;
    }

    const approveObservable = request.type === 'Personal Info'
      ? this.adminService.approvePersonalChangeRequest(request.id)
      : request.type === 'Vehicle Info'
      ? this.adminService.approveVehicleChangeRequest(request.id)
      : this.adminService.approveAvatarChangeRequest(request.id);

    approveObservable.subscribe({
      next: () => {
        alert('Request approved successfully!');
        this.loadTotals();
        this.loadRequests();
      },
      error: (error) => {
        console.error('Error approving request:', error);
        alert('Failed to approve request');
      }
    });
  }

  onReject(request: RequestType): void {
    const reason = prompt(`Please enter the reason for rejecting this ${request.type} change request for ${request.driverName}:`);
    if (!reason?.trim()) {
      alert('Rejection reason is required');
      return;
    }

    const rejectObservable = request.type === 'Personal Info'
      ? this.adminService.rejectPersonalChangeRequest(request.id, reason)
      : request.type === 'Vehicle Info'
      ? this.adminService.rejectVehicleChangeRequest(request.id, reason)
      : this.adminService.rejectAvatarChangeRequest(request.id, reason);

    rejectObservable.subscribe({
      next: () => {
        alert('Request rejected successfully!');
        this.loadTotals();
        this.loadRequests();
      },
      error: (error) => {
        console.error('Error rejecting request:', error);
        alert('Failed to reject request');
      }
    });
  }
}
