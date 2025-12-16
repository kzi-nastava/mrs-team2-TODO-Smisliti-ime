import { Component, signal } from '@angular/core';
import {Router, NavigationEnd, RouterOutlet} from '@angular/router';
import { filter } from 'rxjs/operators';
import {NavBar} from './layout/nav-bar/nav-bar';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  imports: [
    NavBar,
    NgIf,
    RouterOutlet
  ],
  styleUrls: ['./app.css']
})
export class AppComponent {
  showNavbar = signal(true);

  constructor(private router: Router) {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        this.showNavbar.set(event.urlAfterRedirects === '/home' || event.urlAfterRedirects === '/');
      });
  }
}
