import { Component, ChangeDetectorRef, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminNavBarComponent } from '../../layout/admin-nav-bar/admin-nav-bar.component';
import { AdminService, CreateDriverDTO } from '../service/admin.service';
import { VehicleService } from '../../service/vehicle-service/vehicle.service'

@Component({
  selector: 'app-driver-registration',
  imports: [AdminNavBarComponent, FormsModule],
  templateUrl: './driver-registration.html',
  styleUrl: './driver-registration.css',
})
export class DriverRegistration implements OnInit {
  vehicleTypes: string[] = [];

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

  constructor(
      private adminService: AdminService,
      private vehicleService: VehicleService,
      private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadVehicleTypes()
  }

  loadVehicleTypes(): void {
    this.vehicleService.getVehicleTypes().subscribe({
      next: (types) => {
        this.vehicleTypes = types;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Failed to load vehicle types:', err)
    });
  }

  goToVehicle(): void {
    // Validate driver data before moving to vehicle tab
    if (!this.driverData.email || !this.driverData.firstName || !this.driverData.lastName) {
      alert('Please fill in all driver fields');
      return;
    }
    this.activeTab = 'vehicle';
  }

  goToDriver() {
    this.activeTab = 'driver';
  }

  onRegister() {
        // Validate vehicle data
        if (!this.vehicleData.model || !this.vehicleData.type || !this.vehicleData.registrationNumber || !this.vehicleData.seats) {
          alert('Please fill in all vehicle fields');
          return;
        }

        const createDriverData: CreateDriverDTO = {
          email: this.driverData.email,
          name: this.driverData.firstName,
          surname: this.driverData.lastName,
          phone: this.driverData.phone,
          address: this.driverData.address,
          vehicleModel: this.vehicleData.model,
          vehicleType: this.vehicleData.type,
          vehicleLicensePlate: this.vehicleData.registrationNumber,
          vehicleSeats: this.vehicleData.seats,
          vehicleHasBabySeats: this.vehicleData.allowsBabies,
          vehicleAllowsPets: this.vehicleData.allowsPets
        };

        console.log('Registering driver:', createDriverData);

        this.adminService.registerDriver(createDriverData).subscribe({
          next: (response) => {
            console.log('Driver registered successfully:', response);
            alert(`Driver registered successfully! Activation email sent to ${response.email}`);

            // Reset forms
            this.driverData = {
              email: '',
              firstName: '',
              lastName: '',
              phone: '',
              address: ''
            };
            this.vehicleData = {
              model: '',
              type: '',
              registrationNumber: '',
              seats: null,
              allowsBabies: false,
              allowsPets: false
            };
            this.activeTab = 'driver';
            this.cdr.detectChanges();
          },
          error: (error) => {
            console.error('Error registering driver:', error);
            alert('Failed to register driver. Please try again.');
          }
        });
  }
}
