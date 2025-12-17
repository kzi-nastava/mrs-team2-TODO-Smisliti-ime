import { Injectable, signal } from '@angular/core';

export type UserRole = 'guest' | 'user' | 'driver' | 'admin';

@Injectable({ providedIn: 'root' })
export class AuthService {
  public role = signal<UserRole>('guest');

  loginAs(role: UserRole) {
    this.role.set(role);
  }

  logout() {
    this.role.set('guest');
  }
}
