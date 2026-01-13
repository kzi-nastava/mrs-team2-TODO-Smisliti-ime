import { Component } from '@angular/core';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {RouterLink, Router} from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-unregistered-nav-bar',
  imports: [CommonModule, MatToolbarModule, MatButtonModule, MatIconModule, RouterLink],
  templateUrl: './unregistered-nav-bar.component.html',
  styleUrl: './unregistered-nav-bar.component.css',
})
export class UnregisteredNavBarComponent {
  constructor(private router: Router) {}

  isOnLoginPage(): boolean {
    return this.router.url === '/login';
  }

  isOnRegisterPage(): boolean {
    return this.router.url === '/register';
  }
}
