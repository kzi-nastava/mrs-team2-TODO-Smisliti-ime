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
import { MatSnackBar } from '@angular/material/snack-bar';

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
    email: new FormControl('', Validators.required),
    password: new FormControl('', Validators.required),
    stayLoggedIn: new FormControl(false), // Add checkbox control
  });

  // keep saving credentials locally for debugging/storage
  pendingCredentials: { email: string | undefined | null; password: string | undefined | null } | null = null;

  constructor(
    private auth: AuthService,
    private router: Router,
    private http: HttpClient,
    private snackBar: MatSnackBar
  ) {}

  create() {
    if (this.createLoginForm.valid) {
      // TODO: Implement actual login logic here
    }
  }

  login() {
    const emailControl = this.createLoginForm.get('email');
    const passwordControl = this.createLoginForm.get('password');

    if (!emailControl || !passwordControl) {
      console.error('Login: form controls missing');
      return;
    }

    let hasError = false;

    if (emailControl.invalid) {
      if (emailControl.hasError('required')) {
        console.log('Login validation: email is required');
      } else if (emailControl.hasError('email')) {
        console.log('Login validation: email format is invalid');
      } else {
        console.log('Login validation: email is invalid', emailControl.errors);
      }
      hasError = true;
    }

    if (passwordControl.invalid) {
      if (passwordControl.hasError('required')) {
        console.log('Login validation: password is required');
      } else {
        console.log('Login validation: password is invalid', passwordControl.errors);
      }
      hasError = true;
    }

    if (hasError) {
      // mark touched so UI shows errors
      emailControl.markAsTouched();
      passwordControl.markAsTouched();
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
          this.snackBar.open('Login failed: No token received', 'Close', {
            duration: 4000,
            horizontalPosition: 'end',
            verticalPosition: 'bottom',
            panelClass: ['error-snackbar']
          });
          return;
        }

        // Save token based on stayLoggedIn checkbox
        if (stayLoggedIn) {
          localStorage.setItem('authToken', res.token);
          console.log('Login: token saved to localStorage (persistent)');
        } else {
          sessionStorage.setItem('authToken', res.token);
          console.log('Login: token saved to sessionStorage (session only)');
        }

        this.auth.setToken(res.token, stayLoggedIn);

        console.log('Login: token saved & role extracted from JWT');
        console.log("Current role:", res.role);
        this.router.navigate(['/home']);
      },
      error: (err) => {
        console.error('Login: request failed', err);

        let errorMessage = 'Login failed. Please try again.';
        if (err.status === 401) {
          errorMessage = 'Invalid email or password.';
        } else if (err.status === 0) {
          errorMessage = 'Cannot connect to server.';
        } else if (err.error?.message) {
          errorMessage = err.error.message;
        }

        this.snackBar.open(errorMessage, 'Close', {
          duration: 5000,
          horizontalPosition: 'end',
          verticalPosition: 'bottom',
          panelClass: ['error-snackbar']
        });
      }
    });
  }
}
