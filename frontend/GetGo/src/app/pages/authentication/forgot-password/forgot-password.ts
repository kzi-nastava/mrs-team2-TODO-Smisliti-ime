import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {MatFormField, MatInput, MatLabel} from '@angular/material/input';
import {MatIcon} from '@angular/material/icon';
import {MatButton} from '@angular/material/button';
import { HttpClient } from '@angular/common/http';
import {environment} from '../../../../env/environment';
import { SnackBarService } from '../../../service/snackBar/snackBar.service';

@Component({
  selector: 'app-forgot-password',
  imports: [
    CommonModule,
    RouterLink,
    MatFormField,
    MatLabel,
    ReactiveFormsModule,
    MatIcon,
    MatInput,
    MatButton
  ],
  templateUrl: './forgot-password.html',
  styleUrls: ['./forgot-password.css']
})
export class ForgotPasswordComponent implements OnInit {
  form!: FormGroup;
  isSubmitting = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private http: HttpClient,
    private snackBarService: SnackBarService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      email: ['', [
        Validators.required,
        Validators.email,
        Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/)
      ]]
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();

      const fieldMap = { email: 'Email' };
      this.snackBarService.showFormErrors(this.form, fieldMap);
      console.log('ForgotPassword: aborting submit due to validation errors');
      return;
    }

    this.isSubmitting = true;

    const payload = { email: this.form.value.email };
    console.log('ForgotPassword: sending payload to /api/auth/forgot-password', payload);

    type ForgotPasswordResponse = { message?: string };

    this.http.post<ForgotPasswordResponse>(`${environment.apiHost}/api/auth/forgot-password`, payload).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        console.log('ForgotPassword: server response', res);

        this.snackBarService.show('Password reset link sent to your email!', true);
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.isSubmitting = false;
        console.error('ForgotPassword: request failed', err);

        let errorMessages: string[] = [];

        if (err.status === 400 && err.error?.errors) {
          errorMessages = err.error.errors.map((e: any) => e.message || e);
        } else if (err.error?.message) {
          errorMessages = [err.error.message];
        } else {
          errorMessages = ['Failed to send reset email. Please try again.'];
        }

        this.snackBarService.show(errorMessages);
      }
    });
  }
}
