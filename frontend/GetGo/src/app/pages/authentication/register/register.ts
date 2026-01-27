import {Component, ChangeDetectorRef} from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {MatButtonModule, MatIconButton} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatFormField, MatInput, MatLabel} from '@angular/material/input';
import { HttpClient } from '@angular/common/http';
import {CommonModule} from '@angular/common';
import {AuthService} from '../../../service/auth-service/auth.service';
import {environment} from '../../../../env/environment';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SnackBarService } from '../../../service/snackBar/snackBar.service';

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

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private auth: AuthService,
    private router: Router,
    private snackBarService: SnackBarService
  ) {
    this.form = this.fb.group(
      {
        email: ['', [
          // Validators.required,
          // Validators.email,
          // Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/)
        ]],
        firstName: ['', [
          // Validators.required,
          // Validators.minLength(2),
          // Validators.maxLength(50)
        ]],
        lastName: ['', [
          // Validators.required,
          // Validators.minLength(2),
          // Validators.maxLength(50)
        ]],
        address: ['', [
          // Validators.required
        ]],
        phoneNumber: ['', [
          // Validators.required,
          // Validators.pattern(/^(\+3816|06)[0-9]{7,8}$/)
        ]],
        password: ['', [
          // Validators.required,
          // Validators.minLength(8)
        ]],
        confirmPassword: ['', [
          // Validators.required
        ]],
        imageUrl: ['']
      },
      // {validators: this.passwordsMatchValidator}
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
      this.form.markAllAsTouched();

      const fieldMap = {
        email: 'Email',
        firstName: 'First name',
        lastName: 'Last name',
        address: 'Address',
        phoneNumber: 'Phone number',
        password: 'Password',
        confirmPassword: 'Confirm password'
      };

      this.snackBarService.showFormErrors(this.form, fieldMap);
      console.log('Register: aborting due to validation errors');
      return;
    }

    const formData = new FormData();
    formData.append('email',

    this.form.value.email);const capitalize = (s: string) => s.charAt(0).toUpperCase() + s.slice(1).toLowerCase();

    formData.append('name', capitalize(this.form.value.firstName));
    formData.append('surname', capitalize(this.form.value.lastName));

    formData.append('password', this.form.value.password);
    formData.append('phone', this.form.value.phoneNumber);
    formData.append('address', this.form.value.address);

    if (this.selectedFile) {
      formData.append('file', this.selectedFile, this.selectedFile.name);
    }

    this.isSubmitting = true;
    console.log('Register: sending data to server');

    this.http.post(`${environment.apiHost}/api/auth/register`, formData).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        this.cdr.detectChanges();
        console.log('Register: success', res);

        this.snackBarService.show('Registration successful! Please login.', true, 3000);
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.cdr.detectChanges();
        console.error('Register: failed', err);

        let errorMessages: string[] = [];

        if (err.status === 400 && err.error?.errors) {
          errorMessages = err.error.errors.map((e: any) => e.message || e);
        } else if (err.error?.message) {
          errorMessages = [err.error.message];
        } else {
          errorMessages = ['Registration failed. Please try again.'];
        }

        this.snackBarService.show(errorMessages);
      }
    });
  }
}
