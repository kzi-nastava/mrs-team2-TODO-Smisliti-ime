import { Component } from '@angular/core';
import { RidePriceService } from '../../service/ride-price/ride-price.service';

@Component({
  selector: 'app-admin-ride-price',
  imports: [],
  templateUrl: './admin-ride-price.component.html',
  styleUrl: './admin-ride-price.component.css',
})
export class AdminRidePriceComponent {
  selectedVehicleType: string = 'STANDARD';
    pricePerKm: number = 0;
    currentPrice?: number;

    constructor(private priceService: RidePriceService) {}

    ngOnInit() {
      this.loadCurrentPrice();
    }

    loadCurrentPrice() {
      this.priceService
        .getPrice(this.selectedVehicleType)
        .subscribe(price => this.currentPrice = price);
    }

    savePrice() {
      this.priceService
        .updatePrice(this.selectedVehicleType, this.pricePerKm)
        .subscribe(() => {
          this.currentPrice = this.pricePerKm;
        });
    }

}
