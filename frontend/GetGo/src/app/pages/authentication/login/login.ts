import { Component } from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {RouterLink} from '@angular/router';
import { AuthService } from '../../../service/auth-service/auth.service';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import {environment} from '../../../../env/environment';
import { SnackBarService } from '../../../service/snackBar/snackBar.service';
import { UserRole } from '../../../model/user.model';

@Component({
  selector: 'app-login',
  imports: [CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule, MatIconModule, MatCheckboxModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginComponent {
  createLoginForm = new FormGroup({
    email: new FormControl('', [
      // Validators.required,
      // Validators.email,
      // Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/)
    ]),
    password: new FormControl('', [
      // Validators.required,
      // Validators.minLength(8)
    ]),
    stayLoggedIn: new FormControl(false),
  });

  // keep saving credentials locally for debugging/storage
  pendingCredentials: { email: string | undefined | null; password: string | undefined | null } | null = null;

  constructor(
    private auth: AuthService,
    private router: Router,
    private http: HttpClient,
    private snackBarService: SnackBarService
  ) {}

  login() {
    if (this.createLoginForm.invalid) {
      this.createLoginForm.markAllAsTouched();

      const fieldMap = {
        email: 'Email',
        password: 'Password'
      };

      this.snackBarService.showFormErrors(this.createLoginForm, fieldMap);
      console.log('Login: aborting due to validation errors');
      return;
    }

    this.pendingCredentials = {
      email: this.createLoginForm.value.email ?? null,
      password: this.createLoginForm.value.password ?? null
    };

    const stayLoggedIn = this.createLoginForm.value.stayLoggedIn ?? false;

    console.log('Login: collected credentials', this.pendingCredentials);
    console.log('Login: stayLoggedIn =', stayLoggedIn);

    // Build payload and send immediately (no role-selection UI). Default role is 'passenger'.
    const payload = {
      email: this.pendingCredentials.email,
      password: this.pendingCredentials.password,
      role: 'passenger' as 'admin' | 'driver' | 'passenger'
    };

    console.log('Login: sending payload to /api/auth/login', payload);

    type LoginResponse = { token?: string; role?: 'admin' | 'driver' | 'passenger' };

    this.http.post<LoginResponse>(`${environment.apiHost}/api/auth/login`, payload).subscribe({
      next: (res) => {
        console.log('Login: server response', res);

        if (!res?.token) {
          console.error('Login: token missing in response');
          this.snackBarService.show('Login failed: No token received');
          return;
        }

        if (stayLoggedIn) {
          localStorage.setItem('authToken', res.token);
          console.log('Login: token saved to localStorage (persistent)');
        } else {
          sessionStorage.setItem('authToken', res.token);
          console.log('Login: token saved to sessionStorage (session only)');
        }

      this.auth.setToken(res.token, stayLoggedIn);

      console.log('Login: token saved & role extracted from JWT');

      this.snackBarService.show('Login successful!', true, 3000);

      const userRole = this.auth.role();
      switch (userRole) {
        case UserRole.Admin:
          this.router.navigate(['/admin/admin-home']);
          break;
        case UserRole.Driver:
          this.router.navigate(['/driver/driver-home']);
          break;
        case UserRole.Passenger:
          this.router.navigate(['/registered-home']);
          break;
        default:
          this.router.navigate(['/home']);
      }
      },
      error: (err) => {
        console.error('Login: request failed', err);

        let errorMessages: string[] = [];

        if (err.status === 400 && err.error?.errors) {
          // Backend validation errors
          errorMessages = err.error.errors.map((e: any) => e.message || e);
        } else if (err.status === 401) {
          errorMessages = ['Invalid email or password'];
        } else if (err.status === 0) {
          errorMessages = ['Cannot connect to server'];
        } else if (err.error?.message) {
          errorMessages = [err.error.message];
        } else {
          errorMessages = ['Login failed. Please try again.'];
        }

        this.snackBarService.show(errorMessages);
      }
    });
  }
}
