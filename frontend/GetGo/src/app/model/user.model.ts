export interface User {
  email: string;
  username: string;
  firstName: string;
  lastName: string;
  password: string;
  address: string;
  phoneNumber: string;
  role: UserRole;
}

export enum UserRole {
  Admin = "ADMIN",
  Passenger = "PASSENGER",
  Driver = "DRIVER",
  Guest = "GUEST",
}
