import {Component, ChangeDetectorRef} from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {User, UserRole} from '../../../model/user.model';
import {Router, RouterLink} from '@angular/router';
import {MatButtonModule, MatIconButton} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatFormField, MatInput, MatLabel} from '@angular/material/input';
import { HttpClient } from '@angular/common/http';
import {CommonModule} from '@angular/common';
import {AuthService} from '../../../service/auth-service/auth.service';
import {environment} from '../../../../env/environment';

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
  selectedFile: File | null = null;
  profileImageUrl: string | null = null;

  constructor(private fb: FormBuilder, private http: HttpClient, private cdr: ChangeDetectorRef, private auth: AuthService, private router: Router) {
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
      {validators: this.passwordsMatchValidator}
    );
  }

  private passwordsMatchValidator(group: AbstractControl) {
    const password: string = group.get('password')?.value;
    const confirm: string = group.get('confirmPassword')?.value;
    if (!password || !confirm) {
      return null;
    }
    return password === confirm ? null : {passwordsDontMatch: true};
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

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];

      // Validate file type
      if (!file.type.startsWith('image/')) {
        console.error('Selected file is not an image');
        alert('Please select an image file');
        return;
      }

      // Validate file size (max 5MB)
      const maxSize = 5 * 1024 * 1024;
      if (file.size > maxSize) {
        console.error('File size exceeds 5MB');
        alert('Image size must be less than 5MB');
        return;
      }

      this.selectedFile = file;
      console.log('Profile image selected:', file.name, file.type, file.size);

      // Create preview URL
      const reader = new FileReader();
      reader.onload = () => {
        this.profileImageUrl = reader.result as string;
        this.cdr.detectChanges();
        console.log('Profile image preview loaded');
      };
      reader.readAsDataURL(file);
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.logValidationErrors();
      this.form.markAllAsTouched();
      console.log('Register: form invalid, abort submit');
      return;
    }

    this.isSubmitting = true;
    this.cdr.detectChanges();
    console.log('Register: isSubmitting set to true');

    const user: User = {
      email: this.form.value.email,
      username: this.form.value.username || this.form.value.email,
      firstName: this.form.value.firstName,
      lastName: this.form.value.lastName,
      password: this.form.value.password,
      address: this.form.value.address,
      phoneNumber: this.form.value.phoneNumber,
      role: this.form.value.role || UserRole.Passenger
    };

    console.log('Register: sending user to /api/auth/register', user);
    if (this.selectedFile) {
      console.log('Register: profile image will be uploaded:', this.selectedFile.name);
    }

    type RegisterResponse = { id?: string };

    this.http.post<RegisterResponse>(`${environment.apiHost}/api/auth/register`, user).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        this.cdr.detectChanges();
        console.log('Register: success, redirecting to login', res);

        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.cdr.detectChanges();
        console.error('Register: failed', err);
      }
    });
  }
}
