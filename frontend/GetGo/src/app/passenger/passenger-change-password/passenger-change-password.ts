import { Component, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PassengerService } from '../service/passenger.service'

@Component({
  selector: 'app-passenger-change-password',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './passenger-change-password.html',
  styleUrl: './passenger-change-password.css',
})
export class PassengerChangePassword {
passwordData = {
    oldPassword: '',
    password: '',
    confirmPassword: ''
  };

  constructor(
    private passengerService: PassengerService,
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

    this.passengerService.updatePassword(this.passwordData).subscribe({
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
