import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AuthService} from './auth-service/auth.service';
import {UserRole} from '../../model/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private auth: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | UrlTree {

    const expectedRoles: UserRole[] = route.data['roles'] || [];

    if (!this.auth.isLoggedIn()) {
      return this.router.parseUrl('/login');
    }

    const currentRole = this.auth.role();
    if (expectedRoles.length && !expectedRoles.includes(currentRole)) {
      return this.router.parseUrl('/home');
    }

    return true;
  }
}
