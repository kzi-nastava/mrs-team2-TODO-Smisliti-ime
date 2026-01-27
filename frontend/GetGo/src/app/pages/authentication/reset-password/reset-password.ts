import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { HttpClient } from '@angular/common/http';
import {environment} from '../../../../env/environment';
import { SnackBarService } from '../../../service/snackBar/snackBar.service';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.html',
  styleUrls: ['./reset-password.css'],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule
  ],
})
export class ResetPasswordComponent implements OnInit {
  form: FormGroup;
  token: string | null = null;
  email: string | null = null;
  isSubmitting = false;
  serverError: string | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private snackBarService: SnackBarService
  ) {
    this.form = this.fb.group(
      {
        password: ['', [
          // Validators.required,
          // Validators.minLength(8)
        ]],
        confirmPassword: ['', [
          // Validators.required
        ]]
      },
      // { validators: this.passwordsMatchValidator }
    );
  }

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      this.token = params.get('token');
      const rawEmail = params.get('email');
      this.email = rawEmail ? decodeURIComponent(rawEmail) : null;
      console.log('ResetPassword: query params', { token: this.token, email: this.email });
      if (!this.token || !this.email) {
        console.log('ResetPassword: missing token or email in query params');
        this.serverError = 'Invalid or missing reset token/email.';
      }
    });
  }

  private passwordsMatchValidator(group: AbstractControl) {
    const password: string = group.get('password')?.value;
    const confirm: string = group.get('confirmPassword')?.value;
    if (!password || !confirm) {
      return null;
    }
    return password === confirm ? null : { passwordsDontMatch: true };
  }

  get password() {
    return this.form.get('password');
  }

  get confirmPassword() {
    return this.form.get('confirmPassword');
  }

  private logValidationErrors(): void {
    const pwd = this.password;
    const confirm = this.confirmPassword;

    if (!pwd || !confirm) {
      console.warn('ResetPassword validation: controls missing');
      return;
    }

    if (pwd.invalid) {
      if (pwd.hasError('required')) {
        console.log('ResetPassword validation: password is required');
      } else if (pwd.hasError('minlength')) {
        console.log('ResetPassword validation: password must be at least 6 characters');
      } else {
        console.log('ResetPassword validation: password invalid', pwd.errors);
      }
      pwd.markAsTouched();
    }

    if (confirm.invalid) {
      if (confirm.hasError('required')) {
        console.log('ResetPassword validation: confirmPassword is required');
      } else {
        console.log('ResetPassword validation: confirmPassword invalid', confirm.errors);
      }
      confirm.markAsTouched();
    }

    if (this.form.errors && this.form.errors['passwordsDontMatch']) {
      console.log('ResetPassword validation: passwords do not match');
    }
  }

  submit(): void {
    this.serverError = null;

    if (!this.token || !this.email) {
      console.log('ResetPassword: cannot submit - missing token/email');
      this.serverError = 'Invalid or missing reset token/email.';
      this.snackBarService.show(this.serverError);
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();

      const fieldMap = {
        password: 'Password',
        confirmPassword: 'Confirm password'
      };

      this.snackBarService.showFormErrors(this.form, fieldMap);
      console.log('ResetPassword: aborting submit due to validation errors');
      return;
    }

    this.isSubmitting = true;

    const payload = {
      token: this.token,
      email: this.email,
      password: this.form.value.password
    };

    console.log('ResetPassword: sending payload to /api/auth/reset-password', payload);

    type ResetResponse = { message?: string };

    this.http.post<ResetResponse>(`${environment.apiHost}/api/auth/reset-password`, payload).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        console.log('ResetPassword: server response', res);

        this.snackBarService.show('Password reset successful! Please login.', true);
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.isSubmitting = false;
        console.error('ResetPassword: request failed', err);

        let errorMessages: string[] = [];

        if (err.status === 400 && err.error?.errors) {
          errorMessages = err.error.errors.map((e: any) => e.message || e);
        } else if (err.error?.message) {
          errorMessages = [err.error.message];
        } else {
          errorMessages = ['Password reset failed. Please try again.'];
        }

        this.serverError = errorMessages.join(', ');
        this.snackBarService.show(errorMessages);
      }
    });
  }
}
