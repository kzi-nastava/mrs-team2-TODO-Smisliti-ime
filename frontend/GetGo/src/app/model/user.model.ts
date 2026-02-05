export interface User {
  email: string;
  username: string;
  name: string;
  surname: string;
  password: string;
  address: string;
  phone: string;
  profilePictureUrl: string;
  role: UserRole;
}

export enum UserRole {
  Admin = "ADMIN",
  Passenger = "PASSENGER",
  Driver = "DRIVER",
  Guest = "GUEST",
}

export interface GetDriverDTO {
  id: number;
  email: string;
  name: string;
  surname: string;
  phone: string;
  address: string;
  profilePictureUrl?: string;
  recentHoursWorked?: number;
  vehicleModel?: string;
  vehicleType?: string;
  vehicleLicensePlate?: string;
  vehicleSeats?: number;
  vehicleHasBabySeats?: boolean;
  vehicleAllowsPets?: boolean;
}

export interface GetPassengerDTO {
  id: number;
  email: string;
  name: string;
  surname: string;
  phone: string;
  address: string;
  profilePictureUrl?: string;
}
