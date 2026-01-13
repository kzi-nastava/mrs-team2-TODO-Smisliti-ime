import {Injectable, signal} from '@angular/core';
import {UserRole} from '../../../model/user.model';
import {JwtHelperService} from '@auth0/angular-jwt';
import {computed} from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private jwtHelper = new JwtHelperService();
  private TOKEN_KEY = 'authToken';

  private _role = signal<UserRole>(UserRole.Guest);
  role = computed(() => this._role());

  constructor() {
    this.loadRoleFromToken();
  }

  setToken(token: string) {
    console.log('SET TOKEN CALLED');
    localStorage.setItem(this.TOKEN_KEY, token);
    this.loadRoleFromToken();
    console.log('ROLE AFTER SET:', this._role());
  }

  logout() {
    console.log('AuthService: logout called');
    localStorage.removeItem(this.TOKEN_KEY);
    this._role.set(UserRole.Guest);
    console.log('AuthService: role set to Guest after logout');
  }

  isLoggedIn(): boolean {
    const token = localStorage.getItem(this.TOKEN_KEY);
    return !!token && !this.jwtHelper.isTokenExpired(token);
  }

  private loadRoleFromToken() {
    const token = localStorage.getItem(this.TOKEN_KEY);
    console.log('loadRoleFromToken: token from storage', token ? 'exists' : 'missing');

    if (!token) {
      console.log('loadRoleFromToken: no token, setting Guest');
      this._role.set(UserRole.Guest);
      return;
    }

    try {
      const decoded = this.jwtHelper.decodeToken(token);
      console.log('loadRoleFromToken: decoded token', decoded);

      const roleFromToken = decoded?.role;
      console.log('loadRoleFromToken: role field from token', roleFromToken);

      // Map backend role strings to UserRole enum
      let mappedRole: UserRole = UserRole.Guest;

      if (roleFromToken === 'admin' || roleFromToken === 'ADMIN') {
        mappedRole = UserRole.Admin;
      } else if (roleFromToken === 'driver' || roleFromToken === 'DRIVER') {
        mappedRole = UserRole.Driver;
      } else if (roleFromToken === 'passenger' || roleFromToken === 'PASSENGER') {
        mappedRole = UserRole.Passenger;
      }

      console.log('loadRoleFromToken: mapped role', mappedRole);
      this._role.set(mappedRole);
    } catch (err) {
      console.error('loadRoleFromToken: failed to decode token', err);
      this._role.set(UserRole.Guest);
    }
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }
}
