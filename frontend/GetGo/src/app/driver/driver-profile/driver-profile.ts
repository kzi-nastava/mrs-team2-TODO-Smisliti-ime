import { Component, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DriverNavBarComponent } from '../../layout/driver-nav-bar/driver-nav-bar.component';

@Component({
  selector: 'app-driver-profile',
  imports: [DriverNavBarComponent, FormsModule],
  templateUrl: './driver-profile.html',
  styleUrl: './driver-profile.css',
})
export class DriverProfile {
  profileImageUrl: string = 'assets/images/pfp.png';
  selectedFile: File | null = null;
  activeTab: string = 'driver';

  // driver form data
  driverData = {
    email: '',
    firstName: '',
    lastName: '',
    phone: '',
    address: ''
  };

  // vehicle form data
  vehicleData = {
    model: '',
    type: '',
    registrationNumber: '',
    seats: null,
    allowsBabies: false,
    allowsPets: false
  };

  constructor(private cdr: ChangeDetectorRef) {}

  onImageClick() {
    const fileInput = document.getElementById('fileInput') as HTMLInputElement;
    fileInput.click();
  }

  onFileSelected(event: Event) {
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

  setActiveTab(tab: string) {
    this.activeTab = tab;
  }

  onSave() {
    console.log('Driver data:', this.driverData);
    console.log('Vehicle data:', this.vehicleData);
    console.log('Profile picture:', this.selectedFile);
    // send to backend when done
  }
}