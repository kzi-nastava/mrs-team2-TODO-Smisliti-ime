import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UserNavBarComponent } from '../../layout/user-nav-bar/user-nav-bar.component';
import { PassengerService, GetPassengerDTO, UpdatePassengerDTO, UpdatedPassengerDTO, UpdatedProfilePictureDTO } from '../service/passenger.service';
import { switchMap, of } from 'rxjs';
import { environment } from "../../../env/environment"

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
        this.passenger = data;
        this.updateProfileImage(data.profilePictureUrl);
        this.cdr.detectChanges();
      },
      error: (error: any) => {
        console.error('Error loading profile:', error);
      }
    });
  }

  private updateProfileImage(pictureUrl: string | null | undefined): void {
    if (pictureUrl) {
      this.profileImageUrl = `${environment.apiHost}${pictureUrl}`;
    } else {
      this.profileImageUrl = 'assets/images/pfp.png';
    }
  }

  onImageClick(): void {
    const fileInput = document.getElementById('fileInput') as HTMLInputElement;
    fileInput.click();
  }

  onFileSelected(event: Event): void {
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

    // Update profile first, then conditionally upload picture
    this.passengerService.updateProfile(updateData).pipe(
      switchMap((profileResponse: UpdatedPassengerDTO) => {
        // Update passenger data from response
        this.passenger = profileResponse;

        // If file is selected, upload it
        if (this.selectedFile) {
          return this.passengerService.uploadProfilePicture(this.selectedFile);
        } else {
          return of(null);
        }
      })
    ).subscribe({
      next: (pictureResponse: UpdatedProfilePictureDTO | null) => {
        // If picture was uploaded, update the URLs
        if (pictureResponse && pictureResponse.pictureUrl) {
          this.passenger.profilePictureUrl = pictureResponse.pictureUrl;
          this.updateProfileImage(pictureResponse.pictureUrl);
          this.selectedFile = null;
        }

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
