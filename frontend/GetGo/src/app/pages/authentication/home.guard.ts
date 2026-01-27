import { inject } from '@angular/core';
import { Router, CanActivateFn, UrlTree } from '@angular/router';
import { AuthService } from '../../service/auth-service/auth.service';
import { UserRole } from '../../model/user.model';

export const homeGuard: CanActivateFn = (): boolean | UrlTree => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.logoutInProgress) {
    console.log('HomeGuard: logout in progress, staying on current page');
    return false;
  }

  if (!auth.isLoggedIn()) {
    console.log('HomeGuard: guest user, redirecting to unregistered-home');
    return router.createUrlTree(['/unregistered-home']);
  } else {
    console.log('HomeGuard: logged in user, redirecting to registered-home');
    return router.createUrlTree(['/registered-home']);
  }
};
