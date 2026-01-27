export interface CreateInconsistencyReportDTO {
  text: string;
}

export interface CreatedInconsistencyReportDTO {
  id: number;
  rideId: number;
  passengerId: number | null;
  text: string;
}

export interface GetInconsistencyReportDTO {
  id: number;
  createdAt: string;
  passengerEmail: string;
  text: string;
}
