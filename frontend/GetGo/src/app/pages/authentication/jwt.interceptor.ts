import {
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';
import { Injectable, Injector, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../../service/auth-service/auth.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {

  private TOKEN_KEY = 'authToken';
  private router = inject(Router);

  constructor(private injector: Injector) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    if (req.url.includes('/api/ratings/rate')) {
      return next.handle(req);
    }

    const token =
      localStorage.getItem(this.TOKEN_KEY) ||
      sessionStorage.getItem(this.TOKEN_KEY);

    if (token) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {

        if (error.status === 401 || error.status === 403) {
          const authService = this.injector.get(AuthService); // lazy
          authService.clearSession();

          this.router.navigate(['/login'], {
            queryParams: { redirectUrl: this.router.url }
          });
        }

        return throwError(() => error);
      })
    );
  }
}
