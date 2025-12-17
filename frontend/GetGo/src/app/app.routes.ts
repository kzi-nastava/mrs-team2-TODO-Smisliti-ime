import { Routes } from '@angular/router';
import {RideComponent}  from './driver/ride/ride.component';
import {UnregisteredHomeComponent} from './layout/unregistered-home/unregistered-home.component';


export const routes: Routes = [
  {path: 'ride', component: RideComponent},
  {path: '', component: UnregisteredHomeComponent},
  {path: '**', redirectTo: ''}
];
