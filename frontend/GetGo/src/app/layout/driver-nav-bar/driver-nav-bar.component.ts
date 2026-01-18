import { Component } from '@angular/core';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {Router, RouterLink} from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import {AuthService} from '../../service/auth-service/auth.service';

@Component({
  selector: 'app-driver-nav-bar',
  imports: [MatToolbarModule, MatButtonModule, MatIconModule, RouterLink, MatMenuModule],
  templateUrl: './driver-nav-bar.component.html',
  styleUrl: './driver-nav-bar.component.css',
})
export class DriverNavBarComponent {
  constructor(public auth: AuthService, private router: Router) {}

  logout() {
    this.auth.logout();
    this.router.navigate(['/']);
  }

  get fullName(): string {
    return this.auth.fullName();
  }
}
