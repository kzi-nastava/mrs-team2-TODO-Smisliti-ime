import { Component, signal } from '@angular/core';
import { Router, NavigationEnd, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs/operators';
import { UnregisteredNavBarComponent } from './layout/unregistered-nav-bar/unregistered-nav-bar.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, UnregisteredNavBarComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App {
  protected readonly title = signal('GetGo');
  protected readonly showNav = signal(true);

  constructor(private router: Router) {
    this.updateNavVisibility(this.router.url);

    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd)
    ).subscribe(e => this.updateNavVisibility(e.urlAfterRedirects || e.url));
  }

  private updateNavVisibility(url: string) {
    // parse primary segment so root (`/`) keeps nav visible
    const tree = this.router.parseUrl(url || '');
    const segments = tree.root.children['primary']?.segments || [];
    const firstSegment = segments.length ? segments[0].path : '';
    const hiddenSegments = ['login', 'register', 'forgot-password'];
    this.showNav.set(!hiddenSegments.includes(firstSegment));
  }
}
