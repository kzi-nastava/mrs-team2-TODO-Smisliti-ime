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
    localStorage.removeItem(this.TOKEN_KEY);
    this._role.set(UserRole.Guest);
  }

  isLoggedIn(): boolean {
    const token = localStorage.getItem(this.TOKEN_KEY);
    return !!token && !this.jwtHelper.isTokenExpired(token);
  }

  private loadRoleFromToken() {
    const token = localStorage.getItem(this.TOKEN_KEY);
    if (!token) {
      this._role.set(UserRole.Guest);
      return;
    }

    const decoded = this.jwtHelper.decodeToken(token);
    this._role.set(decoded?.role ?? UserRole.Guest);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }
}
