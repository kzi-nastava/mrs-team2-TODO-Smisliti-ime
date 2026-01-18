import { Routes } from '@angular/router';
import {RideComponent}  from './driver/ride/ride.component';
import {LoginComponent} from './pages/authentication/login/login';
import {ForgotPasswordComponent} from './pages/authentication/forgot-password/forgot-password';
import {RegisterComponent} from './pages/authentication/register/register';
import {NotFoundComponent} from './pages/not-found/not-found';
import {UnregisteredHomeComponent} from './layout/unregistered-home/unregistered-home.component';
import {RideDetailsComponent} from './driver/ride-details/ride-details.component';
import {RegisteredHomeComponent} from './layout/registered-home/registered-home.component';
import { PassengerProfileInfo } from './passenger/passenger-profile-info/passenger-profile-info';
import { PassengerChangePassword } from './passenger/passenger-change-password/passenger-change-password';
import { DriverActivate } from './driver/driver-activate/driver-activate';
import { DriverProfile } from './driver/driver-profile/driver-profile';
import { DriverChangePassword } from './driver/driver-change-password/driver-change-password';
import { AdminProfile } from './admin/admin-profile/admin-profile';
import { AdminChangePassword } from './admin/admin-change-password/admin-change-password';
import { AdminReviewDriverRequests } from './admin/admin-review-driver-requests/admin-review-driver-requests'
import { DriverRegistration } from './admin/driver-registration/driver-registration';
import { OrderRide } from './passenger/order-ride/order-ride';
import { FavoriteRides } from './passenger/favorite-rides/favorite-rides';
import { RatingVehicleDriverComponent } from './passenger/rating-vehicle-driver/rating-vehicle-driver.component';
// import { InRideComponent } from './passenger/in-ride/in-ride.component';
import { RideTrackingComponent } from './passenger/ride-tracking/ride-tracking.component';
import { AuthGuard } from './pages/authentication/auth.guard';
import { homeGuard } from './pages/authentication/home.guard';
import { UserRole } from './model/user.model';

export const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'home', canActivate: [homeGuard], children: [] },
  { path: 'unregistered-home', component: UnregisteredHomeComponent },
  { path: 'registered-home', component: RegisteredHomeComponent, canActivate: [AuthGuard], data: { roles: [UserRole.Passenger, UserRole.Driver, UserRole.Admin] } },
  { path: 'login', component: LoginComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'ride', component: RideComponent, canActivate: [AuthGuard], data: { roles: [UserRole.Passenger] } },
  { path: 'driver/rides/:id', component: RideDetailsComponent, canActivate: [AuthGuard], data: { roles: [UserRole.Driver] } },
  { path: 'passenger/passenger-profile', component: PassengerProfileInfo, canActivate: [AuthGuard], data: { roles: [UserRole.Passenger] } },
  { path: 'passenger/change-password', component: PassengerChangePassword, canActivate: [AuthGuard], data: { roles: [UserRole.Passenger] } },
  { path: 'passenger/rating-vehicle-driver', component: RatingVehicleDriverComponent, canActivate: [AuthGuard], data: { roles: [UserRole.Passenger] } },
//   { path: 'passenger/in-ride', component: InRideComponent, canActivate: [AuthGuard], data: { roles: [UserRole.Passenger] } },
  { path: 'passenger/ride-tracking', component: RideTrackingComponent, canActivate: [AuthGuard], data: { roles: [UserRole.Passenger] } },
  { path: 'driver/activate/:token', component: DriverActivate },
  { path: 'driver/driver-profile', component: DriverProfile, canActivate: [AuthGuard], data: { roles: [UserRole.Driver] } },
  { path: 'driver/change-password', component: DriverChangePassword, canActivate: [AuthGuard], data: { roles: [UserRole.Driver] } },
  { path: 'admin/admin-profile', component: AdminProfile, canActivate: [AuthGuard], data: { roles: [UserRole.Admin] } },
  { path: 'admin/review-driver-requests', component: AdminReviewDriverRequests, canActivate: [AuthGuard], data: { roles: [UserRole.Admin] } },
  { path: 'admin/change-password', component: AdminChangePassword, canActivate: [AuthGuard], data: { roles: [UserRole.Admin] } },
  { path: 'admin/driver-registration', component: DriverRegistration, canActivate: [AuthGuard], data: { roles: [UserRole.Admin] } },
  { path: 'order-ride', component: OrderRide, canActivate: [AuthGuard], data: { roles: [UserRole.Passenger] } },
  { path: 'favorite-rides', component: FavoriteRides, canActivate: [AuthGuard], data: { roles: [UserRole.Passenger] } },
  { path: '**', component: NotFoundComponent }
];
