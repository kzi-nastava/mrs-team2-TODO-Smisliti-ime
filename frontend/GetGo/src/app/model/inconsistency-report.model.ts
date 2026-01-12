export interface CreateInconsistencyReportDTO {
  text: string;
}

export interface CreatedInconsistencyReportDTO {
  id: number;
  rideId: number;
  passengerId: number | null;
  text: string;
}
