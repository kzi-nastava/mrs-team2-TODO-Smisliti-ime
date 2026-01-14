import { Injectable, inject } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, CanActivateFn } from '@angular/router';
import { AuthService } from './auth-service/auth.service';
import { UserRole } from '../../model/user.model';

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

export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  console.log('AuthGuard: checking auth, current role:', auth.role());

  const role = auth.role();

  // If user is logged in and tries to access login/register, redirect to /home
  if (state.url.includes('/login') || state.url.includes('/register')) {
    if (role !== UserRole.Guest) {
      console.log('AuthGuard: logged in user accessing auth page, redirecting to /home');
      router.navigate(['/home']);
      return false;
    }
  }

  // If user tries to access /home without being logged in, allow (unregistered home will show)
  if (state.url === '/home' || state.url === '/') {
    return true;
  }

  return true;
};
