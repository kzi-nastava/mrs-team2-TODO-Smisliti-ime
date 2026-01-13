import { Component, OnInit } from '@angular/core';
import { RatingService } from '../../service/rating/rating.service';
import { GetRatingDTO } from '../../model/rating.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgForm } from '@angular/forms';


@Component({
  selector: 'app-rating-vehicle-driver',
  imports: [CommonModule, FormsModule],
  templateUrl: './rating-vehicle-driver.component.html',
  styleUrl: './rating-vehicle-driver.component.css',
})
export class RatingVehicleDriverComponent implements OnInit{
  ratings: GetRatingDTO[] = [];
  rideId = 1; // temporary hardcoded value
  driverRating: number | null = null;
  vehicleRating: number | null = null;
  commentText: string = '';

  avgVehicleRating = 0;
  avgDriverRating = 0;

  constructor(private ratingService: RatingService) {}

  ngOnInit(): void {
    this.loadRatings();
  }

  loadRatings() {
    this.ratingService.getRatingsByRide(this.rideId).subscribe({
      next: (data) => {
        console.log("Ratings from backend:", data);
        this.ratings = data.slice().reverse(); // show newest first
        this.calculateAverages();
        },
      error: (err) => console.error(err)
    });
  }

  submitRating(form: NgForm) {
    if (this.driverRating === null || this.vehicleRating === null || !this.commentText.trim()) {
      alert('Please fill all fields');
      return;
    }

    const newRating = {
      driverRating: this.driverRating,
      vehicleRating: this.vehicleRating,
      comment: this.commentText,
      rideId: this.rideId, // current ride
    };

    this.ratingService.createRating(newRating).subscribe({
      next: (saved) => {
        console.log('Rating saved:', saved);

          this.ratings = [saved, ...this.ratings];
          this.calculateAverages();
          form.resetForm();
      },
      error: (err) => console.error(err),
    });
  }

  calculateAverages() {
    if (this.ratings.length === 0) {
      this.avgVehicleRating = 0;
      this.avgDriverRating = 0;
      return;
    }

    const vehicleSum = this.ratings.reduce(
      (sum, r) => sum + r.vehicleRating,
      0
    );

    const driverSum = this.ratings.reduce(
      (sum, r) => sum + r.driverRating,
      0
    );

    this.avgVehicleRating = vehicleSum / this.ratings.length;
    this.avgDriverRating = driverSum / this.ratings.length;
  }



}
