import { Routes } from '@angular/router';
import {RideComponent}  from './driver/ride/ride.component';
import {LoginComponent} from './pages/authentication/login/login';
import {ForgotPasswordComponent} from './pages/authentication/forgot-password/forgot-password';
import {RegisterComponent} from './pages/authentication/register/register';
import {NotFoundComponent} from './pages/not-found/not-found';
import {UnregisteredHomeComponent} from './layout/unregistered-home/unregistered-home.component';
import {RideDetailsComponent} from './driver/ride-details/ride-details.component';
import { PassengerHome } from './passenger/passenger-home/passenger-home';
import { PassengerProfileInfo } from './passenger/passenger-profile-info/passenger-profile-info';
import { PassengerChangePassword } from './passenger/passenger-change-password/passenger-change-password';
import { DriverHome } from './driver/driver-home/driver-home'
import { DriverActivate } from './driver/driver-activate/driver-activate';
import { DriverProfile } from './driver/driver-profile/driver-profile';
import { DriverChangePassword } from './driver/driver-change-password/driver-change-password';
import { DriverAllScheduledRides } from './driver/driver-all-scheduled-rides/driver-all-scheduled-rides';
import { ScheduledRideDetails } from './driver/scheduled-ride-details/scheduled-ride-details';
import { AdminHome } from './admin/admin-home/admin-home'
import { AdminProfile } from './admin/admin-profile/admin-profile';
import { AdminChangePassword } from './admin/admin-change-password/admin-change-password';
import { AdminReviewDriverRequests } from './admin/admin-review-driver-requests/admin-review-driver-requests'
import { DriverRegistration } from './admin/driver-registration/driver-registration';
import { FavoriteRides } from './passenger/favorite-rides/favorite-rides';
import { RatingVehicleDriverComponent } from './passenger/rating-vehicle-driver/rating-vehicle-driver.component';
// import { InRideComponent } from './passenger/in-ride/in-ride.component';
import { RideTrackingComponent } from './passenger/ride-tracking/ride-tracking.component';
import { AuthGuard } from './pages/authentication/auth.guard';
import { homeGuard } from './pages/authentication/home.guard';
import { UserRole } from './model/user.model';
import {ResetPasswordComponent} from './pages/authentication/reset-password/reset-password';
import {ActivateComponent} from './pages/authentication/activate/activate';
import {PassengerRidesComponent} from './passenger/passenger-ride-history/passenger-rides.component';
import {PassengerRideDetailsComponent} from './passenger/passenger-ride-details/ride-details.component';
import { SupportChatComponent } from './layout/support-chat/support-chat.component';

export const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'home', canActivate: [homeGuard], children: [] },
  { path: 'activate', component: ActivateComponent },
  { path: 'user/reset-password', component: ResetPasswordComponent },
  { path: 'unregistered-home', component: UnregisteredHomeComponent },
  // passenger home now, maybe change later, also change roles when admin home page and admin home page are added:
  { path: 'registered-home', component: PassengerHome, canActivate: [AuthGuard], data: { roles: [UserRole.Passenger, UserRole.Driver, UserRole.Admin] } },
  { path: 'login', component: LoginComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'driver/ride-history', component: RideComponent, canActivate: [AuthGuard], data: { roles: [UserRole.Driver] } },
  { path: 'driver/rides/:id', component: RideDetailsComponent, canActivate: [AuthGuard], data: { roles: [UserRole.Driver] } },
  { path: 'passenger/passenger-profile', component: PassengerProfileInfo, canActivate: [AuthGuard], data: { roles: [UserRole.Passenger] } },
  { path: 'passenger/change-password', component: PassengerChangePassword, canActivate: [AuthGuard], data: { roles: [UserRole.Passenger] } },
  { path: 'rides/:rideId/rate', component: RatingVehicleDriverComponent, canActivate: [AuthGuard], data: { roles: [UserRole.Passenger] } },
  { path: 'passenger/rides/:id', component: PassengerRideDetailsComponent, canActivate: [AuthGuard], data: { roles: [UserRole.Passenger]}},
  { path: 'passenger/passenger-ride-history', component: PassengerRidesComponent, canActivate: [AuthGuard], data: { roles: [UserRole.Passenger] } },
  { path: 'passenger/ride-tracking', component: RideTrackingComponent, canActivate: [AuthGuard], data: { roles: [UserRole.Passenger] } },
  { path: 'driver/activate/:token', component: DriverActivate },
  { path: 'driver/driver-home', component: DriverHome, canActivate: [AuthGuard], data: { roles: [UserRole.Driver] } },
  { path: 'driver/driver-profile', component: DriverProfile, canActivate: [AuthGuard], data: { roles: [UserRole.Driver] } },
  { path: 'driver/change-password', component: DriverChangePassword, canActivate: [AuthGuard], data: { roles: [UserRole.Driver] } },
  { path: 'driver/all-scheduled', component: DriverAllScheduledRides, canActivate: [AuthGuard], data: { roles: [UserRole.Driver ] } },
  { path: 'driver/scheduled-rides/:id', component: ScheduledRideDetails, canActivate: [AuthGuard], data: { roles: [UserRole.Driver] } },
  { path: 'admin/admin-home', component: AdminHome, canActivate: [AuthGuard], data: {roles: [UserRole.Admin] } },
  { path: 'admin/admin-profile', component: AdminProfile, canActivate: [AuthGuard], data: { roles: [UserRole.Admin] } },
  { path: 'admin/review-driver-requests', component: AdminReviewDriverRequests, canActivate: [AuthGuard], data: { roles: [UserRole.Admin] } },
  { path: 'admin/change-password', component: AdminChangePassword, canActivate: [AuthGuard], data: { roles: [UserRole.Admin] } },
  { path: 'admin/driver-registration', component: DriverRegistration, canActivate: [AuthGuard], data: { roles: [UserRole.Admin] } },
  { path: 'favorite-rides', component: FavoriteRides, canActivate: [AuthGuard], data: { roles: [UserRole.Passenger] } },
  { path: 'support-chat', component: SupportChatComponent, canActivate: [AuthGuard], data: { roles: [UserRole.Passenger, UserRole.Driver, UserRole.Admin] } },
  { path: '**', component: NotFoundComponent }
];
