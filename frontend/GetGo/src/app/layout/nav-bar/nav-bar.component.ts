import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AuthService } from '../../pages/authentication/auth-service/auth.service';

import { UnregisteredNavBarComponent } from '../unregistered-nav-bar/unregistered-nav-bar.component';
import { DriverNavBarComponent } from '../driver-nav-bar/driver-nav-bar.component';
import { UserNavBarComponent } from '../user-nav-bar/user-nav-bar.component';
import { AdminNavBarComponent } from '../admin-nav-bar/admin-nav-bar.component';

@Component({
  selector: 'app-nav-bar',
  standalone: true,
  imports: [
    CommonModule,
    UnregisteredNavBarComponent,
    DriverNavBarComponent,
    UserNavBarComponent,
    AdminNavBarComponent
  ],
  template: `
    <ng-container [ngSwitch]="auth.role()">
      <app-unregistered-nav-bar *ngSwitchCase="'guest'" />
      <app-driver-nav-bar *ngSwitchCase="'driver'" />
      <app-user-nav-bar *ngSwitchCase="'user'" />
      <app-admin-nav-bar *ngSwitchCase="'admin'" />
    </ng-container>
  `,
  styleUrl: './nav-bar.component.css'
})
export class NavBarComponent {
  constructor(public auth: AuthService) {}
}

