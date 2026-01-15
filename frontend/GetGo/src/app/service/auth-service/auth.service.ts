import {Injectable, signal} from '@angular/core';
import {UserRole} from '../../model/user.model';
import {JwtHelperService} from '@auth0/angular-jwt';
import {computed} from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private jwtHelper = new JwtHelperService();
  private TOKEN_KEY = 'authToken';

  private roleSignal = signal<UserRole>(UserRole.Guest);
  public role = this.roleSignal.asReadonly();

  constructor() {
    // Load token from either storage
    const token = localStorage.getItem(this.TOKEN_KEY) || sessionStorage.getItem(this.TOKEN_KEY);
    if (token) {
      this.loadRoleFromToken(token);
    } else {
      this.roleSignal.set(UserRole.Guest);
    }
  }

  setToken(token: string, persistent: boolean = false) {
    console.log('SET TOKEN CALLED, persistent =', persistent);

    // Save to proper storage
    if (persistent) {
      localStorage.setItem(this.TOKEN_KEY, token);
      sessionStorage.removeItem(this.TOKEN_KEY);
    } else {
      sessionStorage.setItem(this.TOKEN_KEY, token);
      localStorage.removeItem(this.TOKEN_KEY);
    }

    this.loadRoleFromToken(token);
    console.log('ROLE AFTER SET:', this.roleSignal());
  }

  logout() {
    console.log('AuthService: logout called');
    localStorage.removeItem(this.TOKEN_KEY);
    sessionStorage.removeItem(this.TOKEN_KEY);
    this.roleSignal.set(UserRole.Guest);
    console.log('AuthService: role set to Guest after logout');
  }

  isLoggedIn(): boolean {
    const token = localStorage.getItem(this.TOKEN_KEY) || sessionStorage.getItem(this.TOKEN_KEY);
    return !!token && !this.jwtHelper.isTokenExpired(token);
  }

  private loadRoleFromToken(token: string) {
    try {
      const decoded = this.jwtHelper.decodeToken(token);
      const roleFromToken = decoded?.role;

      let mappedRole: UserRole = UserRole.Guest;
      if (roleFromToken === 'admin' || roleFromToken === 'ADMIN') mappedRole = UserRole.Admin;
      else if (roleFromToken === 'driver' || roleFromToken === 'DRIVER') mappedRole = UserRole.Driver;
      else if (roleFromToken === 'passenger' || roleFromToken === 'PASSENGER') mappedRole = UserRole.Passenger;

      this.roleSignal.set(mappedRole);
    } catch (err) {
      console.error('loadRoleFromToken: failed to decode token', err);
      this.roleSignal.set(UserRole.Guest);
    }
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY) || sessionStorage.getItem(this.TOKEN_KEY);
  }
}
