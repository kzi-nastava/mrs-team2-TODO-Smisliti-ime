import { Component } from '@angular/core';

import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {Router, RouterLink} from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import {AuthService} from '../../service/auth-service/auth.service';

@Component({
  selector: 'app-user-nav-bar',
    imports: [MatToolbarModule, MatButtonModule, MatIconModule, RouterLink, MatMenuModule],
  templateUrl: './user-nav-bar.component.html',
  styleUrl: './user-nav-bar.component.css',
})
export class UserNavBarComponent {
  constructor(public auth: AuthService, private router: Router) {}

  logout() {
    this.auth.logout();
    this.router.navigate(['/']);
  }

  get fullName(): string {
    return this.auth.fullName();
  }
}
