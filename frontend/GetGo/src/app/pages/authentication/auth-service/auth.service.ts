import {Injectable, signal} from '@angular/core';
import {UserRole} from '../../../model/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  public role = signal<UserRole>(UserRole.Guest);

  constructor() {
    console.log('AuthService instance created');
  }

  loginAs(role: UserRole) {
    this.role.set(role);
  }

  logout() {
    this.role.set(UserRole.Guest);
  }
}
