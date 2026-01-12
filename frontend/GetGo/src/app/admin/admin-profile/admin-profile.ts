import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminNavBarComponent } from '../../layout/admin-nav-bar/admin-nav-bar.component';
import { AdminService, GetAdminDTO } from '../service/admin.service';

@Component({
  selector: 'app-admin-profile',
  imports: [AdminNavBarComponent, CommonModule, FormsModule],
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

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.adminService.getProfile().subscribe({
      next: (data) => {
        this.admin = data;
      },
      error: (error) => {
        console.error('Error loading profile:', error);
      }
    });
  }

  saveProfile(): void {
    // TODO: Implement save functionality
    console.log('Saving profile:', this.admin);
  }
}