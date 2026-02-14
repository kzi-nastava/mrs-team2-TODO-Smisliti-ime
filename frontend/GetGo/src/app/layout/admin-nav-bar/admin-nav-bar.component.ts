import { Component } from '@angular/core';

import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {Router, RouterLink} from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import {AuthService} from '../../service/auth-service/auth.service';
import { NotificationListenerService } from '../../service/notification/notification-listener.service';

@Component({
  selector: 'app-admin-nav-bar',
    imports: [MatToolbarModule, MatButtonModule, MatIconModule, RouterLink, MatMenuModule],
  templateUrl: './admin-nav-bar.component.html',
  styleUrl: './admin-nav-bar.component.css',
})
export class AdminNavBarComponent {
  constructor(
    public auth: AuthService,
    private router: Router,
    private notificationListener: NotificationListenerService
    ) {}

  logout() {
    this.notificationListener.stopListening();

    this.auth.logout();
    this.router.navigate(['/']);
  }

  get fullName(): string {
    return this.auth.fullName();
  }
}
