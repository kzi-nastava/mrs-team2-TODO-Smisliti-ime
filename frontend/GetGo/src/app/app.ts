import { Component, signal, computed, effect } from '@angular/core';
import { Router, NavigationEnd, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from './pages/authentication/auth-service/auth.service';
import { UserRole } from './model/user.model';
import { CommonModule } from '@angular/common';
import { NavBarComponent } from './layout/nav-bar/nav-bar.component';
import { UnregisteredNavBarComponent } from './layout/unregistered-nav-bar/unregistered-nav-bar.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    NavBarComponent,
    UnregisteredNavBarComponent
  ],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App {
  protected readonly title = signal('GetGo');
  protected readonly showNav = signal(true);

  // Computed signal za prikaz navbara u zavisnosti od role
  isGuest = computed(() => this.auth.role() === UserRole.Guest);

  constructor(private router: Router, public auth: AuthService) {
    console.log('App: constructor, initial role', this.auth.role());

    // Track role changes with effect
    effect(() => {
      console.log('App: role changed to', this.auth.role(), 'isGuest:', this.isGuest());
    });

    this.updateNavVisibility(this.router.url);

    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd)
    ).subscribe(e => {
      console.log('App: navigation event, current role', this.auth.role());
      this.updateNavVisibility(e.urlAfterRedirects || e.url);
    });
  }

  private updateNavVisibility(url: string) {
    console.log('Navigated to:', url, 'role:', this.auth.role());
    // parse primary segment so root (`/`) keeps nav visible
    const tree = this.router.parseUrl(url || '');
    const segments = tree.root.children['primary']?.segments || [];
    const firstSegment = segments.length ? segments[0].path : '';

    console.log('First segment:', firstSegment, 'showNav:', this.showNav());
  }
}
