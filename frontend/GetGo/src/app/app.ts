import { Component, signal, computed } from '@angular/core';
import { Router, NavigationEnd, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs/operators';
import { CommonModule } from '@angular/common';

import { AuthService } from './pages/authentication/auth-service/auth.service';
import { NavBarComponent} from './layout/nav-bar/nav-bar.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, NavBarComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App {
  protected readonly title = signal('GetGo');
  protected readonly showNav = signal(true);

  // showNav = signal(true);
  currentRole = computed(() => this.auth.role());

  constructor(private router: Router, public auth: AuthService) {
    this.updateNavVisibility(this.router.url);

    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd)
    ).subscribe(e => this.updateNavVisibility(e.urlAfterRedirects || e.url));
  }

  private updateNavVisibility(url: string) {
    // parse primary segment so root (`/`) keeps nav visible
    console.log('Navigated to:', url);
    const tree = this.router.parseUrl(url || '');
    const segments = tree.root.children['primary']?.segments || [];
    const firstSegment = segments.length ? segments[0].path : '';
    console.log('First segment:', firstSegment);
    const hiddenSegments = ['login', 'register', 'forgot-password'];
    this.showNav.set(!hiddenSegments.includes(firstSegment));
    console.log('showNav:', this.showNav());
  }
}
