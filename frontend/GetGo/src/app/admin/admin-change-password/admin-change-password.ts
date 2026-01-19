import { Component, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminNavBarComponent } from '../../layout/admin-nav-bar/admin-nav-bar.component';
import { AdminService } from '../service/admin.service';

@Component({
  selector: 'app-admin-change-password',
  standalone: true,
  imports: [FormsModule, AdminNavBarComponent],
  templateUrl: './admin-change-password.html',
  styleUrl: './admin-change-password.css',
})
export class AdminChangePassword {
  passwordData = {
    oldPassword: '',
    password: '',
    confirmPassword: ''
  };

  constructor(
    private adminService: AdminService,
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

    this.adminService.updatePassword(this.passwordData).subscribe({
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
