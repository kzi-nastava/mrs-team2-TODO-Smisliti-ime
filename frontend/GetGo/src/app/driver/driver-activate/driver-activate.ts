import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DriverService } from '../service/driver.service';
import { SnackBarService } from '../../service/snackBar/snackBar.service';

@Component({
  selector: 'app-driver-activate',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './driver-activate.html',
  styleUrl: './driver-activate.css',
})
export class DriverActivate implements OnInit {
  token: string = '';
  isValidating: boolean = true;
  isValid: boolean = false;
  validationError: string = '';
  driverEmail: string = '';

  passwordData = {
    password: '',
    confirmPassword: ''
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private driverService: DriverService,
    private snackBar: SnackBarService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    // Get token from URL parameter
    this.token = this.route.snapshot.paramMap.get('token') || '';

    if (!this.token) {
      this.isValidating = false;
      this.validationError = 'Invalid activation link';
      return;
    }

    // Validate the token with backend
    this.validateToken();
  }

  validateToken(): void {
    this.driverService.validateActivationToken(this.token).subscribe({
      next: (response) => {
        this.isValidating = false;
        if (response.valid) {
          this.isValid = true;
          this.driverEmail = response.email;
        } else {
          this.validationError = response.reason || 'Invalid activation token';
        }
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error validating token:', error);
        this.isValidating = false;
        this.validationError = 'Failed to validate activation link';
        this.cdr.detectChanges();
      }
    });
  }

  setPassword(): void {
    if (!this.passwordData.password || !this.passwordData.confirmPassword) {
      this.snackBar.show('Please fill in all fields')
      return;
    }
    if (this.passwordData.password !== this.passwordData.confirmPassword) {
      this.snackBar.show('Passwords do not match')
      return;
    }
    if (this.passwordData.password.length < 6) {
      this.snackBar.show('Password must be at least 6 characters long')
      return;
    }

    this.driverService.setDriverPassword({
      token: this.token,
      password: this.passwordData.password,
      confirmPassword: this.passwordData.confirmPassword
    }).subscribe({
      next: (response) => {
        if (response.success) {
          this.snackBar.show('Account activated successfully! You can now log in.', true)
          this.router.navigate(['/login']);
        } else {
          this.snackBar.show(response.message)
        }
      },
      error: (error) => {
        console.error('Error setting password:', error);
        this.snackBar.show('Failed to set password. Please try again.')
      }
    });
  }
}
