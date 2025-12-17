import {Component, signal} from '@angular/core';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {RouterModule} from '@angular/router';
import {MatButtonToggle} from '@angular/material/button-toggle';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'app-nav-bar',
  imports: [MatToolbarModule, MatButtonModule, RouterModule, MatIcon],
  templateUrl: './nav-bar.html',
  styleUrl: './nav-bar.css',
})
export class NavBar {
  applicationName = signal("GetGo");
}
