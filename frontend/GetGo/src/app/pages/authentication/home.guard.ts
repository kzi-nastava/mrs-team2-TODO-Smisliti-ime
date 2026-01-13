import { inject } from '@angular/core';
import { Router, CanActivateFn, UrlTree } from '@angular/router';
import { AuthService } from './auth-service/auth.service';
import { UserRole } from '../../model/user.model';

export const homeGuard: CanActivateFn = (): boolean | UrlTree => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const role = auth.role();
  console.log('HomeGuard: current role', role);

  if (role === UserRole.Guest) {
    console.log('HomeGuard: guest user, redirecting to unregistered-home');
    return router.createUrlTree(['/unregistered-home']);
  } else {
    console.log('HomeGuard: logged in user, redirecting to registered-home');
    return router.createUrlTree(['/registered-home']);
  }
};
