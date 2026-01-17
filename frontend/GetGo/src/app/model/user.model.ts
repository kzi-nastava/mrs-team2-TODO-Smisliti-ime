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
