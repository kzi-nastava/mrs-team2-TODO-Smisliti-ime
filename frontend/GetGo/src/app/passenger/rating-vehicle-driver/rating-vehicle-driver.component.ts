import { Component, OnInit } from '@angular/core';
import { RatingService } from '../../service/rating/rating.service';
import { GetRatingDTO } from '../../model/rating.model';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-rating-vehicle-driver',
  imports: [CommonModule],
  templateUrl: './rating-vehicle-driver.component.html',
  styleUrl: './rating-vehicle-driver.component.css',
})
export class RatingVehicleDriverComponent implements OnInit{
  ratings: GetRatingDTO[] = [];
  rideId = 1; // temporary hardcoded value

  constructor(private ratingService: RatingService) {}

  ngOnInit(): void {
    this.loadRatings();
  }

  loadRatings() {
    this.ratingService.getRatingsByRide(this.rideId).subscribe({
      next: (data) => {
        console.log("Ratings from backend:", data);
        this.ratings = data;
        },
      error: (err) => console.error(err)
    });
  }
}
