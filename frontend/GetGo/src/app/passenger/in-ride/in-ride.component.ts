import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {MapComponent} from '../../layout/map/map.component';

@Component({
  selector: 'app-in-ride',
  imports: [CommonModule, MapComponent, FormsModule],
  templateUrl: './in-ride.component.html',
  styleUrl: './in-ride.component.css',
})
export class InRideComponent {
  showReportForm = false;
  reportText = '';

  submitReport() {
    this.showReportForm = false;
    this.reportText = '';
  }
}
