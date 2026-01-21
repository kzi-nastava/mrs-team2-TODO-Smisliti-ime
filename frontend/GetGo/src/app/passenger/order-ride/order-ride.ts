import { Component } from '@angular/core';
import { UserNavBarComponent } from '../../layout/user-nav-bar/user-nav-bar.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-order-ride',
  imports: [UserNavBarComponent, CommonModule],
  templateUrl: './order-ride.html',
  styleUrl: './order-ride.css',
})
export class OrderRide {
  vehicleTypes: string[] = [
    'Standard',
    'Luxury',
    'Van',
    'SUV'
  ];
}
