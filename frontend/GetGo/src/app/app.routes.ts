import { Routes } from '@angular/router';
import {RideComponent}  from './driver/ride/ride.component';
import {LoginComponent} from './pages/authentication/login/login';
import {ForgotPasswordComponent} from './pages/authentication/forgot-password/forgot-password';
import {RegisterComponent} from './pages/authentication/register/register';
import {NotFoundComponent} from './pages/not-found/not-found';
// import {HomeComponent} from './layout/home/home';
import {UnregisteredHomeComponent} from './layout/unregistered-home/unregistered-home.component';
import {RideDetailsComponent} from './driver/ride-details/ride-details.component';
import { PassengerProfileInfo } from './passenger/passenger-profile-info/passenger-profile-info';
import { DriverProfile } from './driver/driver-profile/driver-profile';

export const routes: Routes = [
  { path: 'ride', component: RideComponent},
  { path: '', component: UnregisteredHomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'driver/rides/:id', component: RideDetailsComponent},
  { path: 'passenger/profile-info', component: PassengerProfileInfo},
  { path: 'driver/driver-info', component: DriverProfile},
  { path: '**', component: NotFoundComponent }
];
