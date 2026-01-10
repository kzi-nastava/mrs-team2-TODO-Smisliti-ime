import {Component} from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {User, UserRole} from '../../../model/user.model';
import {RouterLink} from '@angular/router';
import {MatButtonModule, MatIconButton} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatFormField, MatInput, MatLabel} from '@angular/material/input';
import { HttpClient } from '@angular/common/http';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-register',
  templateUrl: './register.html',
  styleUrls: ['./register.css'],
  imports: [
    CommonModule,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatIconButton,
    MatFormField,
    ReactiveFormsModule,
    MatLabel,
    MatInput
  ]
})
export class RegisterComponent {
  form: FormGroup;
  hidePassword = true;
  hideConfirmPassword = true;
  isSubmitting = false;

  constructor(private fb: FormBuilder, private http: HttpClient) {
    this.form = this.fb.group(
      {
        email: ['', [Validators.required, Validators.email]],
        firstName: ['', [Validators.required]],
        lastName: ['', [Validators.required]],
        address: ['', [Validators.required]],
        phoneNumber: ['', [Validators.required]],
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', [Validators.required]],
      },
      { validators: this.passwordsMatchValidator }
    );
  }

  private passwordsMatchValidator(group: AbstractControl) {
    const password : string = group.get('password')?.value;
    const confirm : string = group.get('confirmPassword')?.value;
    if (!password || !confirm) {
      return null;
    }
    return password === confirm ? null : { passwordsDontMatch: true };
  }

  // New helper: log per-field validation errors
  private logValidationErrors(): void {
    const controls: { [key: string]: AbstractControl | null } = {
      email: this.form.get('email'),
      firstName: this.form.get('firstName'),
      lastName: this.form.get('lastName'),
      address: this.form.get('address'),
      phoneNumber: this.form.get('phoneNumber'),
      password: this.form.get('password'),
      confirmPassword: this.form.get('confirmPassword')
    };

    for (const [name, control] of Object.entries(controls)) {
      if (!control) {
        console.warn(`Register validation: missing control ${name}`);
        continue;
      }
      if (control.invalid) {
        if (control.hasError('required')) {
          console.log(`Register validation: ${name} is required`);
        } else if (name === 'email' && control.hasError('email')) {
          console.log('Register validation: email format is invalid');
        } else if (name === 'password' && control.hasError('minlength')) {
          console.log('Register validation: password must be at least 6 characters');
        } else {
          console.log(`Register validation: ${name} invalid`, control.errors);
        }
      }
      // mark touched so errors show in UI
      control.markAsTouched();
    }

    if (this.form.errors && this.form.errors["passwordsDontMatch"]) {
      console.log('Register validation: passwords do not match');
    }
  }

  get password() {
    return this.form.get('password');
  }

  get confirmPassword() {
    return this.form.get('confirmPassword');
  }

  submit(): void {
    if (this.form.invalid) {
      this.logValidationErrors();
      this.form.markAllAsTouched();
      console.log('Register: form invalid, abort submit');
      return;
    }

    this.isSubmitting = true;

    const user: User = {
      email: this.form.value.email,
      username: this.form.value.username || this.form.value.email, // fallback to email
      firstName: this.form.value.firstName,
      lastName: this.form.value.lastName,
      password: this.form.value.password,
      address: this.form.value.address,
      phoneNumber: this.form.value.phoneNumber,
      role: this.form.value.role || UserRole.Passenger // default role
    };

    console.log('Register: sending user to /api/auth/register', user);

    type RegisterResponse = { id?: string };

    this.http.post<RegisterResponse>('/api/auth/register', user).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        console.log('Register: success', res);
      },
      error: (err) => {
        this.isSubmitting = false;
        console.error('Register: failed', err);
      }
    });
  }
}
