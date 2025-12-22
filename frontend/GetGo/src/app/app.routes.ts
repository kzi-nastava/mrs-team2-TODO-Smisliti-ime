import { Routes } from '@angular/router';
import {RideComponent}  from './driver/ride/ride.component';
import {LoginComponent} from './pages/authentication/login/login';
import {ForgotPasswordComponent} from './pages/authentication/forgot-password/forgot-password';
import {RegisterComponent} from './pages/authentication/register/register';
import {NotFoundComponent} from './pages/not-found/not-found';
import {UnregisteredHomeComponent} from './layout/unregistered-home/unregistered-home.component';
import {RideDetailsComponent} from './driver/ride-details/ride-details.component';
import {RegisteredHomeComponent} from './layout/registered-home/registered-home.component';

export const routes: Routes = [
  { path: '', component: UnregisteredHomeComponent },
  { path: 'home', component: RegisteredHomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'ride', component: RideComponent},
  { path: 'driver/rides/:id', component: RideDetailsComponent},
  { path: '**', component: NotFoundComponent }
];
