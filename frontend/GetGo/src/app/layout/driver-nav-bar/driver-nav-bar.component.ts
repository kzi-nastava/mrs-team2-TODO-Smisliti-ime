import { Component } from '@angular/core';

import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {RouterLink} from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';

@Component({
  selector: 'app-driver-nav-bar',
  imports: [MatToolbarModule, MatButtonModule, MatIconModule, RouterLink, MatMenuModule],
  templateUrl: './driver-nav-bar.component.html',
  styleUrl: './driver-nav-bar.component.css',
})
export class DriverNavBarComponent {

}
