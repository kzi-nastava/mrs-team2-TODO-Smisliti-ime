import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {MatFormField, MatInput, MatLabel} from '@angular/material/input';
import {MatIcon} from '@angular/material/icon';
import {MatButton} from '@angular/material/button';
import { HttpClient } from '@angular/common/http'; // added

@Component({
  selector: 'app-forgot-password',
  imports: [
    RouterLink,
    MatFormField,
    MatLabel,
    ReactiveFormsModule,
    MatIcon,
    MatInput,
    MatButton,
    CommonModule
  ],
  templateUrl: './forgot-password.html',
  styleUrls: ['./forgot-password.css']
})
export class ForgotPasswordComponent implements OnInit {
  form!: FormGroup;
  isSubmitting = false;

  constructor(private fb: FormBuilder, private router: Router, private http: HttpClient) {} // injected HttpClient

  ngOnInit(): void {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  submit(): void {
    const emailControl = this.form.get('email');
    if (!emailControl) {
      console.error('ForgotPassword: email control missing');
      return;
    }

    // Validation logging
    if (this.form.invalid) {
      if (emailControl.hasError('required')) {
        console.log('ForgotPassword validation: email is required');
      } else if (emailControl.hasError('email')) {
        console.log('ForgotPassword validation: email format is invalid');
      } else {
        console.log('ForgotPassword validation: email invalid', emailControl.errors);
      }
      emailControl.markAsTouched();
      this.form.markAllAsTouched();
      console.log('ForgotPassword: aborting submit due to validation errors');
      return;
    }

    this.isSubmitting = true;

    const payload = { email: emailControl.value as string };
    console.log('ForgotPassword: sending payload to /api/auth/forgot-password', payload);

    type ForgotPasswordResponse = { message?: string };

    this.http.post<ForgotPasswordResponse>('/api/auth/forgot-password', payload).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        console.log('ForgotPassword: server response', res);
        // navigate to login or show confirmation
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.isSubmitting = false;
        console.error('ForgotPassword: request failed', err);
        // keep form visible for retry, optionally show UI error
      }
    });
  }
}
