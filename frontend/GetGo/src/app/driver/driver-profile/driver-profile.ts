import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DriverNavBarComponent } from '../../layout/driver-nav-bar/driver-nav-bar.component';
import { DriverService, GetDriverDTO, UpdateDriverPersonalDTO, UpdateDriverVehicleDTO } from '../service/driver.service';
import { environment } from '../../../env/environment';

@Component({
  selector: 'app-driver-profile',
  standalone: true,
  imports: [DriverNavBarComponent, FormsModule, RouterLink],
  templateUrl: './driver-profile.html',
  styleUrl: './driver-profile.css',
})
export class DriverProfile implements OnInit {
  profileImageUrl: string = 'assets/images/pfp.png';
  selectedFile: File | null = null;
  activeTab: string = 'driver';
  recentHoursWorked: number = 0;
  blocked: boolean = false;
  blockReason: string | null = null;

  driverData = {
    id: 0,
    email: '',
    firstName: '',
    lastName: '',
    phone: '',
    address: ''
  };

  vehicleData = {
    model: '',
    type: '',
    registrationNumber: '',
    seats: null as number | null,
    allowsBabies: false,
    allowsPets: false
  };

  constructor(
    private driverService: DriverService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.driverService.getProfile().subscribe({
      next: (data: GetDriverDTO) => {
        console.log('Driver profile loaded:', data);

        this.driverData = {
          id: data.id,
          email: data.email,
          firstName: data.name,
          lastName: data.surname,
          phone: data.phone,
          address: data.address
        };

        this.vehicleData = {
          model: data.vehicleModel || '',
          type: data.vehicleType || '',
          registrationNumber: data.vehicleLicensePlate || '',
          seats: data.vehicleSeats || null,
          allowsBabies: data.vehicleHasBabySeats || false,
          allowsPets: data.vehicleAllowsPets || false
        };

        if (data.profilePictureUrl) {
          this.profileImageUrl = `${environment.apiHost}${data.profilePictureUrl}`;
        }

        this.recentHoursWorked = data.recentHoursWorked || 0;

        this.blocked = data.blocked || false;
        this.blockReason = data.blockReason || null;

        this.cdr.detectChanges();
      },
      error: (error: any) => {
        console.error('Error loading profile:', error);
        alert('Failed to load profile');
      }
    });
  }

  onImageClick(): void {
    const fileInput = document.getElementById('fileInput') as HTMLInputElement;
    fileInput.click();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.selectedFile = input.files[0];

      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.profileImageUrl = e.target.result;
        this.cdr.detectChanges();
      };
      reader.readAsDataURL(this.selectedFile);
    }
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }

  onSave(): void {
    if (this.activeTab === 'driver') {
      this.saveDriverInfo();
    } else if (this.activeTab === 'vehicle') {
      this.saveVehicleInfo();
    }

    // Save profile picture if changed
    if (this.selectedFile) {
      this.saveProfilePicture();
    }
  }

  private saveDriverInfo(): void {
    const updateData: UpdateDriverPersonalDTO = {
      name: this.driverData.firstName,
      surname: this.driverData.lastName,
      phone: this.driverData.phone,
      address: this.driverData.address
    };

    this.driverService.requestPersonalInfoChange(updateData).subscribe({
      next: (response) => {
        console.log('Personal info change request created:', response);
        alert(`Change request submitted successfully! Request ID: ${response.requestId}\nStatus: ${response.status}`);
      },
      error: (error) => {
        console.error('Error creating personal change request:', error);
        alert('Failed to submit personal change request');
      }
    });
  }

  private saveVehicleInfo(): void {
    if (!this.vehicleData.seats) {
      alert('Please enter number of seats');
      return;
    }

    const updateData: UpdateDriverVehicleDTO = {
      vehicleModel: this.vehicleData.model,
      vehicleType: this.vehicleData.type,
      vehicleLicensePlate: this.vehicleData.registrationNumber,
      vehicleSeats: this.vehicleData.seats,
      vehicleHasBabySeats: this.vehicleData.allowsBabies,
      vehicleAllowsPets: this.vehicleData.allowsPets
    };

    this.driverService.requestVehicleInfoChange(updateData).subscribe({
      next: (response) => {
        console.log('Vehicle info change request created:', response);
        alert(`Change request submitted successfully! Request ID: ${response.requestId}\nStatus: ${response.status}`);
      },
      error: (error) => {
        console.error('Error creating vehicle change request:', error);
        alert('Failed to submit vehicle change request');
      }
    });
  }

  private saveProfilePicture(): void {
    if (!this.selectedFile) return;

    this.driverService.requestProfilePictureChange(this.selectedFile).subscribe({
      next: (response) => {
        console.log('Profile picture change request created:', response);
        alert(`Profile picture change request submitted! Request ID: ${response.requestId}\nStatus: ${response.status}`);
        this.selectedFile = null;
      },
      error: (error) => {
        console.error('Error creating picture change request:', error);
        alert('Failed to submit picture change request');
      }
    });
  }
}
