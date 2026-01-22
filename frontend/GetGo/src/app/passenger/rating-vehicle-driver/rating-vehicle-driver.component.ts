import { Component, inject, effect, signal } from '@angular/core';
import { RatingService } from '../../service/rating/rating.service';
import { GetRatingDTO } from '../../model/rating.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgForm } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';


@Component({
  selector: 'app-rating-vehicle-driver',
  imports: [CommonModule, FormsModule],
  templateUrl: './rating-vehicle-driver.component.html',
  styleUrl: './rating-vehicle-driver.component.css',
})
export class RatingVehicleDriverComponent{

  rideId!: number;
//   rideId = 1; // temporary hardcoded value

  driverRating = signal<number | null>(null);
  vehicleRating = signal<number | null>(null);
  commentText = signal<string>('');

  private ratingService = inject(RatingService);

  ratings = this.ratingService.ratings;
  avgVehicleRating = this.ratingService.avgVehicleRating
  avgDriverRating = this.ratingService.avgDriverRating



  constructor(private route: ActivatedRoute) {
    this.route.params.subscribe(params => {
      this.rideId = +params['rideId'];
      this.ratingService.setRide(this.rideId);
    })

//     this.ratingService.setRide(this.rideId);

    effect(() => {
      console.log("Ratings changed: ", this.ratings());
    })
  }

  submitRating() {
    if (this.driverRating() === null || this.vehicleRating() === null || !this.commentText().trim()) {
      alert('Please fill all fields');
      return;
    }

    const newRating = {
      driverRating: this.driverRating()!,
      vehicleRating: this.vehicleRating()!,
      comment: this.commentText(),
      rideId: this.rideId, // current ride
    };

    this.ratingService.createRating(newRating).subscribe({
      next: (saved) => {
        console.log('Rating saved:', saved);

        this.driverRating.set(null);
        this.vehicleRating.set(null);
        this.commentText.set('');
      },
      error: (err) => console.error(err),
    });
  }
}
