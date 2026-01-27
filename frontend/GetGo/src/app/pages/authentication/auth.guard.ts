import { Injectable, inject } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, CanActivateFn } from '@angular/router';
import { AuthService } from '../../service/auth-service/auth.service';
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
      console.log('AuthGuard: user not logged in, clearing session');
      this.auth.clearSession();
      return this.router.createUrlTree(
        ['/login'],
        { queryParams: { redirectUrl: state.url } }
      );
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

  return new Promise<boolean | UrlTree>(resolve => {
    const check = () => {
      const role = auth.role();
      if (role !== undefined) {
        if (!auth.isLoggedIn()) {
          console.log('authGuard: user not logged in, clearing session');
          auth.clearSession();
          const redirect = state.url;
          resolve(router.createUrlTree(['/login'], { queryParams: { redirectUrl: redirect } }));
        } else {
          resolve(true);
        }
      } else {
        // waiting for role to be loaded
        setTimeout(check, 50);
      }
    };

    check();
  });
};
