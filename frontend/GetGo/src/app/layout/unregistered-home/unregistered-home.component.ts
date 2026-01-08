import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {MapComponent} from '../map/map.component';

@Component({
  selector: 'app-unregistered-home',
  templateUrl: './unregistered-home.component.html',
  imports: [FormsModule, MapComponent],
  styleUrls: ['./unregistered-home.component.css']
})
export class UnregisteredHomeComponent {
  destination = '';

  calculateTime() {
    console.log('Calculate for unregistered user:', { destination: this.destination });
    alert('Calculation triggered (check console).');
  }
}
