import { Component, inject, effect, signal } from '@angular/core';
import { RatingService } from '../../service/rating/rating.service';
import { GetRatingDTO } from '../../model/rating.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgForm } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../service/auth-service/auth.service';
import { HttpClient } from '@angular/common/http';
import { environment } from "../../../env/environment"



@Component({
  selector: 'app-rating-vehicle-driver',
  imports: [CommonModule, FormsModule],
  templateUrl: './rating-vehicle-driver.component.html',
  styleUrl: './rating-vehicle-driver.component.css',
})
export class RatingVehicleDriverComponent{

  rideId: number | null = null;

//   rideId = 1; // temporary hardcoded value

  driverRating = signal<number | null>(null);
  vehicleRating = signal<number | null>(null);
  commentText = signal<string>('');

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);
  private ratingService = inject(RatingService);
  private http = inject(HttpClient);

  ratings = this.ratingService.ratings;
  avgVehicleRating = this.ratingService.avgVehicleRating
  avgDriverRating = this.ratingService.avgDriverRating


  constructor() {
    this.route.params.subscribe(params => {
      this.rideId = params['rideId'] ? +params['rideId'] : null;
      console.log('RideId iz rute:', this.rideId);

      if (this.rideId !== null) {
            this.http.get<number>(`${environment.apiHost}/api/completed-rides/${this.rideId}/driver`)
              .subscribe({
                next: (driverId) => {
                  console.log('DriverId:', driverId);
                  this.ratingService.setDriver(driverId);
                },
                error: (err) => console.error('Failed to get driverId', err)
              });
          }

    });



    effect(() => {
      console.log("Ratings changed: ", this.ratingService.ratings());
    });
  }



  submitRating() {
    if (this.driverRating() === null || this.vehicleRating() === null || !this.commentText().trim()) {
      alert('Please fill all fields');
      return;
    }

    const newRating = {
      driverRating: this.driverRating()!,
      vehicleRating: this.vehicleRating()!,
      comment: this.commentText()
    };

    if (this.rideId){
      this.ratingService.createRating({...newRating, rideId: this.rideId}).subscribe({
        next: (saved) => {
          console.log('Rating saved:', saved);
          this.driverRating.set(null);
          this.vehicleRating.set(null);
          this.commentText.set('');
        },
        error: (err) => console.error(err),
      });
    } else {
      console.error("Cannot submit rating: no rideId found");
    }

  }
}
