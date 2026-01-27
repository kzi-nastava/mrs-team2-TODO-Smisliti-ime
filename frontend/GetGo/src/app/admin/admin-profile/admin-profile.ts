import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminNavBarComponent } from '../../layout/admin-nav-bar/admin-nav-bar.component';
import { AdminService, GetAdminDTO, UpdateAdminDTO } from '../service/admin.service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-admin-profile',
  standalone: true,
  imports: [FormsModule, AdminNavBarComponent, RouterLink],
  templateUrl: './admin-profile.html',
  styleUrl: './admin-profile.css',
})
export class AdminProfile implements OnInit {
  admin: GetAdminDTO = {
    id: 0,
    email: '',
    name: '',
    surname: '',
    phone: '',
    address: ''
  };

  constructor(
    private adminService: AdminService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.adminService.getProfile().subscribe({
      next: (data) => {
        console.log("Admin profile updated successfully")
        this.admin = data;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading profile:', error);
      }
    });
  }

  saveProfile(): void {
      const updateData: UpdateAdminDTO = {
        name: this.admin.name,
        surname: this.admin.surname,
        phone: this.admin.phone,
        address: this.admin.address
      };

      console.log('Saving profile:', updateData);

      this.adminService.updateProfile(updateData).subscribe({
        next: (response) => {
          console.log('Profile updated successfully:', response);
          // Update local data with response
          this.admin = response;
          this.cdr.detectChanges();
          alert('Profile updated successfully!'); // Simple feedback
        },
        error: (error) => {
          console.error('Error updating profile:', error);
          alert('Failed to update profile. Please try again.');
        }
      });
  }
}
