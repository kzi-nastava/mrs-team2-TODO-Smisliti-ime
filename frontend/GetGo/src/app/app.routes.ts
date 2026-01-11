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
import { DriverProfile } from './driver/driver-profile/driver-profile';
import { AdminProfile } from './admin/admin-profile/admin-profile';
import { DriverRegistration } from './admin/driver-registration/driver-registration';
import { OrderRide } from './passenger/order-ride/order-ride';
import { FavoriteRides } from './passenger/favorite-rides/favorite-rides';
import { RatingVehicleDriverComponent } from './passenger/rating-vehicle-driver/rating-vehicle-driver.component';
// import { InRideComponent } from './passenger/in-ride/in-ride.component';
import { RideTrackingComponent } from './passenger/ride-tracking/ride-tracking.component';

export const routes: Routes = [
  { path: '', component: UnregisteredHomeComponent },
  { path: 'home', component: RegisteredHomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'ride', component: RideComponent},
  { path: 'driver/rides/:id', component: RideDetailsComponent},
  { path: 'passenger/passenger-profile', component: PassengerProfileInfo},
  { path: 'passenger/rating-vehicle-driver', component: RatingVehicleDriverComponent},
//   { path: 'passenger/in-ride', component: InRideComponent},
  { path: 'passenger/ride-tracking', component: RideTrackingComponent},
  { path: 'driver/driver-profile', component: DriverProfile},
  { path: 'admin/admin-profile', component: AdminProfile},
  { path: 'admin/driver-registration', component: DriverRegistration},
  { path: 'order-ride', component: OrderRide},
  { path: 'favorite-rides', component:FavoriteRides},
  { path: '**', component: NotFoundComponent }
];
