import { Component } from '@angular/core';
import {UnregisteredNavBarComponent} from "../../layout/unregistered-nav-bar/unregistered-nav-bar.component";
@Component({
  selector: 'app-not-found',
    imports: [
        UnregisteredNavBarComponent
    ],
  templateUrl: './not-found.html',
  styleUrl: './not-found.css',
})
export class NotFoundComponent {

}
