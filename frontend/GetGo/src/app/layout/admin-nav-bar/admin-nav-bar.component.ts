import { Component } from '@angular/core';

import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {RouterLink} from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';

@Component({
  selector: 'app-admin-nav-bar',
    imports: [MatToolbarModule, MatButtonModule, MatIconModule, RouterLink, MatMenuModule],
  templateUrl: './admin-nav-bar.component.html',
  styleUrl: './admin-nav-bar.component.css',
})
export class AdminNavBarComponent {

}
