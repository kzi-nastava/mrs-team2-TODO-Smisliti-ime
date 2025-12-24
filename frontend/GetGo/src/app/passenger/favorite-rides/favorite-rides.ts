import { Component } from '@angular/core';
import { UserNavBarComponent } from '../../layout/user-nav-bar/user-nav-bar.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-favorite-rides',
  imports: [UserNavBarComponent, CommonModule],
  templateUrl: './favorite-rides.html',
  styleUrl: './favorite-rides.css',
})
export class FavoriteRides {
  // Load data from backend later
  favoriteRides = [
    { from: 'Novi Sad, Bulevar Oslobodjenja 538', to: 'Novi Sad, Bulevar Oslobodjenja 645' },
    { from: 'Novi Sad, Bulevar Oslobodjenja 538', to: 'Novi Sad, Bulevar Oslobodjenja 645' },
    { from: 'Novi Sad, Bulevar Oslobodjenja 538', to: 'Novi Sad, Bulevar Oslobodjenja 645' },
    { from: 'Novi Sad, Bulevar Oslobodjenja 538', to: 'Novi Sad, Bulevar Oslobodjenja 645' },
    { from: 'Novi Sad, Bulevar Oslobodjenja 538', to: 'Novi Sad, Bulevar Oslobodjenja 645' },
    { from: 'Novi Sad, Bulevar Oslobodjenja 538', to: 'Novi Sad, Bulevar Oslobodjenja 645' },
    { from: 'Novi Sad, Bulevar Oslobodjenja 538', to: 'Novi Sad, Bulevar Oslobodjenja 645' },
    { from: 'Novi Sad, Bulevar Oslobodjenja 538', to: 'Novi Sad, Bulevar Oslobodjenja 645' },
    { from: 'Novi Sad, Bulevar Oslobodjenja 538', to: 'Novi Sad, Bulevar Oslobodjenja 645' },
    { from: 'Novi Sad, Bulevar Oslobodjenja 538', to: 'Novi Sad, Bulevar Oslobodjenja 645' }
  ];

}