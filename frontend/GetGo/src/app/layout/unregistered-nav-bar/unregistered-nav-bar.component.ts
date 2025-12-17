import { Component } from '@angular/core';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {RouterLink} from '@angular/router';
import { MatIconModule } from '@angular/material/icon';


@Component({
  selector: 'app-unregistered-nav-bar',
  imports: [MatToolbarModule, MatButtonModule, MatIconModule, RouterLink],
  templateUrl: './unregistered-nav-bar.component.html',
  styleUrl: './unregistered-nav-bar.component.css',
})
export class UnregisteredNavBarComponent {

}
