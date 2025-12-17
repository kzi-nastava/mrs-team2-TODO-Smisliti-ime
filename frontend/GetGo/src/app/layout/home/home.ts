import { Component } from '@angular/core';
import {NavBar} from '../nav-bar/nav-bar';
import {UnregisteredNavBarComponent} from '../unregistered-nav-bar/unregistered-nav-bar.component';

@Component({
  selector: 'app-home',
  imports: [
    UnregisteredNavBarComponent
  ],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class HomeComponent {

}
