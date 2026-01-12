import { Routes } from '@angular/router';
import {RideComponent}  from './driver/ride/ride.component';
import {LoginComponent} from './pages/authentication/login/login';
import {RegisterComponent} from './pages/authentication/register/register';
import {NotFoundComponent} from './pages/not-found/not-found';
import {UnregisteredHomeComponent} from './layout/unregistered-home/unregistered-home.component';
import {RideDetailsComponent} from './driver/ride-details/ride-details.component';
import {RegisteredHomeComponent} from './layout/registered-home/registered-home.component';
import { PassengerProfileInfo } from './passenger/passenger-profile-info/passenger-profile-info';
import { DriverProfile } from './driver/driver-profile/driver-profile';
import { AdminProfile } from './admin/admin-profile/admin-profile';
import { DriverRegistration } from './admin/driver-registration/driver-registration';
import { OrderRide } from './passenger/order-ride/order-ride';
import { FavoriteRides } from './passenger/favorite-rides/favorite-rides';
import {AuthGuard} from './pages/authentication/auth.guard';
import {UserRole} from './model/user.model';

export const routes: Routes = [
  { path: '', component: UnregisteredHomeComponent },
  { path: 'home', component: RegisteredHomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'ride', component: RideComponent, canActivate: [AuthGuard], data: { roles: [UserRole.Passenger] } },
  { path: 'driver/rides/:id', component: RideDetailsComponent, canActivate: [AuthGuard], data: { roles: [UserRole.Driver] } },
  { path: 'passenger/passenger-profile', component: PassengerProfileInfo, canActivate: [AuthGuard], data: { roles: [UserRole.Passenger] } },
  { path: 'driver/driver-profile', component: DriverProfile, canActivate: [AuthGuard], data: { roles: [UserRole.Driver] } },
  { path: 'admin/admin-profile', component: AdminProfile, canActivate: [AuthGuard], data: { roles: [UserRole.Admin] } },
  { path: 'admin/driver-registration', component: DriverRegistration, canActivate: [AuthGuard], data: { roles: [UserRole.Admin] } },
  { path: 'order-ride', component: OrderRide, canActivate: [AuthGuard], data: { roles: [UserRole.Passenger] } },
  { path: 'favorite-rides', component: FavoriteRides, canActivate: [AuthGuard], data: { roles: [UserRole.Passenger] } },
  { path: '**', component: NotFoundComponent }
];
