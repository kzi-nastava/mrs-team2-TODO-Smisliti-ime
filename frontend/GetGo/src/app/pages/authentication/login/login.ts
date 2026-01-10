import { Component } from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {RouterLink} from '@angular/router';
import { AuthService } from '../auth-service/auth.service';
import { Router } from '@angular/router';
import {UserRole} from '../../../model/user.model';
import { HttpClient } from '@angular/common/http';

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
  });

  // keep saving credentials locally for debugging/storage
  pendingCredentials: { email: string | undefined | null; password: string | undefined | null } | null = null;

  constructor(private auth: AuthService, private router: Router, private http: HttpClient) {}

  create() {
    if (this.createLoginForm.valid) {
      // TODO: Implement actual login logic here
    }
  }

  login() {
    // New: per-field validation logs before proceeding
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

    console.log('Login: collected credentials', this.pendingCredentials);

    // Build payload and send immediately (no role-selection UI). Default role is 'passenger'.
    const payload = {
      email: this.pendingCredentials.email,
      password: this.pendingCredentials.password,
      role: 'passenger' as 'admin' | 'driver' | 'passenger'
    };

    console.log('Login: sending payload to /api/auth/login', payload);

    type LoginResponse = { token?: string; role?: 'admin' | 'driver' | 'passenger' };

    this.http.post<LoginResponse>('/api/auth/login', payload).subscribe({
      next: (res) => {
        console.log('Login: server response', res);

        if (res && res.token) {
          localStorage.setItem('authToken', res.token);
          console.log('Login: token stored in localStorage');
        }

        // prefer server-provided role; fallback to passenger
        const assignedRole = res && res.role
          ? (res.role === 'admin' ? UserRole.Admin : res.role === 'driver' ? UserRole.Driver : UserRole.Passenger)
          : UserRole.Passenger;

        this.auth.loginAs(assignedRole);
        console.log('Login: applied role', assignedRole);

        this.router.navigate(['/home']);
        console.log('Login: navigated to /home');
      },
      error: (err) => {
        console.error('Login: request failed', err);
        // optionally set UI error state here
      }
    });
  }
}
