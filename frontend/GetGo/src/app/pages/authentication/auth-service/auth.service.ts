import {Injectable, signal} from '@angular/core';
import {UserRole} from '../../../model/user.model';
import {JwtHelperService} from '@auth0/angular-jwt';
import {computed} from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private jwtHelper = new JwtHelperService();
  private TOKEN_KEY = 'authToken';

  private roleSignal = signal<UserRole>(UserRole.Guest);
  public role = this.roleSignal.asReadonly();

  constructor() {
    // Load role synchronously before app starts
    const token = localStorage.getItem('authToken');
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const role = payload.role || payload.authorities?.[0] || UserRole.Guest;
        console.log('AuthService: constructor loaded role from token:', role);
        this.roleSignal.set(role);
      } catch (e) {
        console.error('AuthService: constructor failed to decode token', e);
        this.roleSignal.set(UserRole.Guest);
      }
    } else {
      console.log('AuthService: constructor no token found, setting Guest');
      this.roleSignal.set(UserRole.Guest);
    }
  }

  setToken(token: string) {
    console.log('SET TOKEN CALLED');
    localStorage.setItem(this.TOKEN_KEY, token);
    this.loadRoleFromToken();
    console.log('ROLE AFTER SET:', this.roleSignal());
  }

  logout() {
    console.log('AuthService: logout called');
    localStorage.removeItem(this.TOKEN_KEY);
    this.roleSignal.set(UserRole.Guest);
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
      this.roleSignal.set(UserRole.Guest);
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
      this.roleSignal.set(mappedRole);
    } catch (err) {
      console.error('loadRoleFromToken: failed to decode token', err);
      this.roleSignal.set(UserRole.Guest);
    }
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }
}
