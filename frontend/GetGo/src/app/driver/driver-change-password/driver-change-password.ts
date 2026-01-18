import { Component, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DriverService } from '../service/driver.service'

@Component({
  selector: 'app-driver-change-password',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './driver-change-password.html',
  styleUrl: './driver-change-password.css',
})
export class DriverChangePassword {
  passwordData = {
    oldPassword: '',
    password: '',
    confirmPassword: ''
  };

  constructor(
      private driverService: DriverService,
      private cdr: ChangeDetectorRef
    ) {}

    changePassword(): void {
      if (this.passwordData.password !== this.passwordData.confirmPassword) {
        alert('New password and confirm password do not match!');
        return;
      }

      if (!this.passwordData.oldPassword || !this.passwordData.password) {
        alert('Please fill in all fields!');
        return;
      }

      this.driverService.updatePassword(this.passwordData).subscribe({
        next: (response) => {
          console.log('Password changed successfully:', response);
          alert('Password changed successfully!');
          this.passwordData = { oldPassword: '', password: '', confirmPassword: '' };
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error changing password:', error);
          alert('Failed to change password. Please try again.');
        }
      });
    }
}
