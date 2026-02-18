import { Component, OnInit, signal, computed, effect } from '@angular/core';
import { Router, NavigationEnd, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from './service/auth-service/auth.service';
import { UserRole } from './model/user.model';
import { CommonModule } from '@angular/common';
import { NavBarComponent } from './layout/nav-bar/nav-bar.component';
import { UnregisteredNavBarComponent } from './layout/unregistered-nav-bar/unregistered-nav-bar.component';
import { NotificationListenerService } from './service/notification/notification-listener.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    NavBarComponent,
    UnregisteredNavBarComponent,
  ],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App implements OnInit {
  protected readonly title = signal('GetGo');

  isGuest = computed(() => {
    const role = this.auth.role();
    console.log('App: isGuest computed, role:', role, 'result:', role === UserRole.Guest);
    return role === UserRole.Guest;
  });

  constructor(
    private router: Router,
    public auth: AuthService,
    private notificationListener: NotificationListenerService
    ) {
    console.log('App: constructor, initial role', this.auth.role());

    // Track role changes with effect
    effect(() => {
      console.log('App: role changed to', this.auth.role(), 'isGuest:', this.isGuest());
    });

    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd)
    ).subscribe(e => {
      console.log('App: navigation event, current role', this.auth.role(), 'isGuest:', this.isGuest(), 'url:', e.url);
    });
  }

    async ngOnInit() {
      if (this.auth.role() !== UserRole.Guest) {
        await this.notificationListener.startListening();
      }
    }
}
