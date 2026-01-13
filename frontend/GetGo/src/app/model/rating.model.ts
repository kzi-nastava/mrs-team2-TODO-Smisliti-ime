export interface GetRatingDTO {
    id: number;
    rideId: number;
    driverId: number;

    vehicleId: number;

    passengerId: number;
    driverRating: number;
    vehicleRating: number;
    comment: string;
}
