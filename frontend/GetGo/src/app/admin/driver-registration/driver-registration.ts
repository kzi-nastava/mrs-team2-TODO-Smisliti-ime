import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminNavBarComponent } from '../../layout/admin-nav-bar/admin-nav-bar.component';

@Component({
  selector: 'app-driver-registration',
  imports: [AdminNavBarComponent, FormsModule],
  templateUrl: './driver-registration.html',
  styleUrl: './driver-registration.css',
})
export class DriverRegistration {
  activeTab: string = 'driver';

  // Driver form data
  driverData = {
    email: '',
    firstName: '',
    lastName: '',
    phone: '',
    address: ''
  };

  // Vehicle form data
  vehicleData = {
    model: '',
    type: '',
    registrationNumber: '',
    seats: null,
    allowsBabies: false,
    allowsPets: false
  };

  goToVehicle() {
    this.activeTab = 'vehicle';
  }

  goToDriver() {
    this.activeTab = 'driver';
  }

  onRegister() {
    console.log('Driver data:', this.driverData);
    console.log('Vehicle data:', this.vehicleData);
    // Send to backend when ready
  }
}