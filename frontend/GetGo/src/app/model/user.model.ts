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
  Admin = "admin",
  Passenger = "passenger",
  Driver = "driver",
  Guest = "guest",
}
