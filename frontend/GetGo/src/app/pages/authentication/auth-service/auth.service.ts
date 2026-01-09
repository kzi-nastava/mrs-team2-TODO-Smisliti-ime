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
    console.log('AuthService: loginAs called, role set to', role);
  }

  logout() {
    this.role.set(UserRole.Guest);
    console.log('AuthService: logout called, role set to Guest');
  }
}
