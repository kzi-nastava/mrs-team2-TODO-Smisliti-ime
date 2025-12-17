import { Routes } from '@angular/router';
import {LoginComponent} from '../../../frontend/GetGo/src/app/pages/authentication/login/login';
import {ForgotPasswordComponent} from '../../../frontend/GetGo/src/app/pages/authentication/forgot-password/forgot-password';
import {RegisterComponent} from '../../../frontend/GetGo/src/app/pages/authentication/register/register';
import {NotFoundComponent} from '../../../frontend/GetGo/src/app/pages/not-found/not-found';
import {HomeComponent} from '../../../frontend/GetGo/src/app/layout/home/home';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'register', component: RegisterComponent },
  { path: '**', component: NotFoundComponent }
];
