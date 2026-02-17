import { Component, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminNavBarComponent } from '../../layout/admin-nav-bar/admin-nav-bar.component';
import { AdminService } from '../service/admin.service';
import { SnackBarService } from '../../service/snackBar/snackBar.service';

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
    private cdr: ChangeDetectorRef,
    private snackBar: SnackBarService
  ) {}

  changePassword(): void {
    if (this.passwordData.password !== this.passwordData.confirmPassword) {
      this.snackBar.show('New password and confirm password do not match!')
      return;
    }

    if (!this.passwordData.oldPassword || !this.passwordData.password) {
      this.snackBar.show('Please fill in all fields!')
      return;
    }

    this.adminService.updatePassword(this.passwordData).subscribe({
      next: (response) => {
        console.log('Password changed successfully:', response);
        this.snackBar.show('Password changed successfully!')
        this.passwordData = { oldPassword: '', password: '', confirmPassword: '' };
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error changing password:', error);
        this.snackBar.show('Failed to change password. Please try again.')
      }
    });
  }
}
