import { Component, OnInit, signal, effect } from '@angular/core';
import { RidePriceService } from '../../service/ride-price/ride-price.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

type VehicleType = 'STANDARD' | 'LUXURY' | 'VAN';

interface PriceInfo {
  vehicleType: VehicleType;
  pricePerKm: number | null;
  startPrice: number | null;
}

@Component({
  selector: 'app-admin-ride-price',
  imports: [ CommonModule, FormsModule, MatSnackBarModule ],
  templateUrl: './admin-ride-price.component.html',
  styleUrl: './admin-ride-price.component.css',
})
export class AdminRidePriceComponent implements OnInit {

  vehicleTypes: VehicleType[] = ['STANDARD', 'LUXURY', 'VAN'];


  prices = signal<PriceInfo[]>([]);

  constructor(
    private ridePriceService: RidePriceService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.loadPrices();
  }

  loadPrices() {
    // Fill the prices signal with initial null values for each vehicle type
    this.prices.set(this.vehicleTypes.map(type => ({
      vehicleType: type,
      pricePerKm: null,
      startPrice: null
    })));

    // load prices for each vehicle type
    this.vehicleTypes.forEach((type, index) => {
      this.ridePriceService.getPrice(type).subscribe({
        next: data => {
          const newPrices = [...this.prices()];
          newPrices[index] = {
            vehicleType: type,
            pricePerKm: data.pricePerKm ?? null,
            startPrice: data.startPrice ?? null
          };
          this.prices.set(newPrices);
        },
        error: () => {
          // If there is null then --
        }
      });
    });
  }

  save(row: PriceInfo) {
    if ((row.pricePerKm ?? 0) < 0 || (row.startPrice ?? 0) < 0) {
        this.snackBar.open('Prices cannot be negative!', 'Close', { duration: 3000 });
        return;
      }
    this.ridePriceService.updatePrice(row.vehicleType, {
      pricePerKm: row.pricePerKm,
      startPrice: row.startPrice
    }).subscribe({
      next: () => {
        this.snackBar.open(`${row.vehicleType} prices updated`, 'Close', {
          duration: 3000
        });
      },
      error: () => {
        this.snackBar.open(`Failed to update ${row.vehicleType}`, 'Close', {
          duration: 3000
        });
      }
    });
  }
}
