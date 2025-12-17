import { Routes } from '@angular/router';
import {RideComponent}  from './driver/ride/ride.component';
import {LoginComponent} from './pages/authentication/login/login';
import {ForgotPasswordComponent} from './pages/authentication/forgot-password/forgot-password';
import {RegisterComponent} from './pages/authentication/register/register';
import {NotFoundComponent} from './pages/not-found/not-found';
import {HomeComponent} from './layout/home/home';

export const routes: Routes = [
  { path: 'ride', component: RideComponent},
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'register', component: RegisterComponent },
  { path: '**', component: NotFoundComponent }
];
