import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UserNavBarComponent } from '../../layout/user-nav-bar/user-nav-bar.component';
import { PassengerService, GetPassengerDTO, UpdatePassengerDTO, UpdatedPassengerDTO, UpdatedProfilePictureDTO } from '../service/passenger.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-passenger-profile-info',
  standalone: true,
  imports: [UserNavBarComponent, FormsModule, RouterLink],
  templateUrl: './passenger-profile-info.html',
  styleUrl: './passenger-profile-info.css',
})
export class PassengerProfileInfo implements OnInit {
  passenger: GetPassengerDTO = {
    id: 0,
    email: '',
    name: '',
    surname: '',
    phone: '',
    address: '',
    profilePictureUrl: ''
  };

  profileImageUrl: string = 'assets/images/pfp.png';
  selectedFile: File | null = null;

  constructor(
    private passengerService: PassengerService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.passengerService.getProfile().subscribe({
      next: (data: GetPassengerDTO) => {
        console.log('Passenger profile loaded:', data);
        this.passenger = data;
        if (data.profilePictureUrl) {
          this.profileImageUrl = data.profilePictureUrl;
        }
        this.cdr.detectChanges();
      },
      error: (error: any) => {
        console.error('Error loading profile:', error);
      }
    });
  }

  onImageClick() {
    const fileInput = document.getElementById('fileInput') as HTMLInputElement;
    fileInput.click();
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.selectedFile = input.files[0];

      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.profileImageUrl = e.target.result;
        this.cdr.detectChanges();
      };
      reader.readAsDataURL(this.selectedFile);
    }
  }

  onSave(): void {
    const updateData: UpdatePassengerDTO = {
      name: this.passenger.name,
      surname: this.passenger.surname,
      phone: this.passenger.phone,
      address: this.passenger.address
    };

    console.log('Saving profile:', updateData);

    // If there's a new profile picture, save both; otherwise just save profile info
    if (this.selectedFile) {
      // Save both profile info and picture
      forkJoin({
        profile: this.passengerService.updateProfile(updateData),
        picture: this.passengerService.uploadProfilePicture(this.selectedFile)
      }).subscribe({
        next: (response: { profile: UpdatedPassengerDTO; picture: UpdatedProfilePictureDTO }) => {
          console.log('Profile and picture updated:', response);
          this.passenger = response.profile;
          if (response.picture.pictureUrl) {
            this.profileImageUrl = response.picture.pictureUrl;
            this.passenger.profilePictureUrl = response.picture.pictureUrl;
          }
          this.selectedFile = null;
          this.cdr.detectChanges();
          alert('Profile updated successfully!');
        },
        error: (error: any) => {  // ADD TYPE
          console.error('Error updating profile:', error);
          alert('Failed to update profile. Please try again.');
        }
      });
    } else {
      // Only save profile info
      this.passengerService.updateProfile(updateData).subscribe({
        next: (response: UpdatedPassengerDTO) => {
          console.log('Profile updated:', response);
          this.passenger = response;
          this.cdr.detectChanges();
          alert('Profile updated successfully!');
        },
        error: (error: any) => {
          console.error('Error updating profile:', error);
          alert('Failed to update profile. Please try again.');
        }
      });
    }
  }
}
