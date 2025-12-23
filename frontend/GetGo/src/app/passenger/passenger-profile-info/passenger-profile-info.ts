import { Component, ChangeDetectorRef } from '@angular/core';
import { UserNavBarComponent } from '../../layout/user-nav-bar/user-nav-bar.component';

@Component({
  selector: 'app-passenger-profile-info',
  imports: [UserNavBarComponent],
  templateUrl: './passenger-profile-info.html',
  styleUrl: './passenger-profile-info.css',
})
export class PassengerProfileInfo {
  profileImageUrl: string = 'assets/images/pfp.png';
  selectedFile: File | null = null;

  constructor(private cdr: ChangeDetectorRef) {}

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

  onSave() {
    if (this.selectedFile) {
      console.log('File to upload:', this.selectedFile);
    }
  }
}