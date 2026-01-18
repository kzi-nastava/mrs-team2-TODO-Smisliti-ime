import { Component, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../service/auth-service/auth.service';
import { UserRole } from '../../model/user.model';
import { UnregisteredNavBarComponent } from '../../layout/unregistered-nav-bar/unregistered-nav-bar.component';
import {NavBarComponent} from '../../layout/nav-bar/nav-bar.component';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [CommonModule, UnregisteredNavBarComponent, NavBarComponent],
  templateUrl: './not-found.html',
  styleUrls: ['./not-found.css'],
})
export class NotFoundComponent {
  isGuest = computed(() => {
    const role = this.auth.role();
    console.log('NotFound: isGuest computed, role:', role);
    return role === UserRole.Guest;
  });

  constructor(public auth: AuthService) {
    console.log('NotFound: constructor, role:', this.auth.role());
  }
}
