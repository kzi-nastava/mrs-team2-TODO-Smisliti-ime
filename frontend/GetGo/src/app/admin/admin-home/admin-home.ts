import { Component, AfterViewInit, OnDestroy, ElementRef, ViewChild, OnInit } from '@angular/core';
import { AdminNavBarComponent } from '../../layout/admin-nav-bar/admin-nav-bar.component';
import { MapComponent } from '../../layout/map/map.component';
import { WebSocketService } from '../../service/websocket/websocket.service';
import { SnackBarService } from '../../service/snackBar/snackBar.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-admin-home',
  standalone: true,
  imports: [AdminNavBarComponent, MapComponent],
  templateUrl: './admin-home.html',
  styleUrl: './admin-home.css',
})
export class AdminHome implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('adminMap', { read: ElementRef, static: false })
  private mapEl?: ElementRef<HTMLElement>;

  private mapClickListener?: (ev: Event) => void;
  private panicSubscription?: Subscription;

  constructor(
    private webSocketService: WebSocketService,
    private snackBarService: SnackBarService
  ) {}

  ngOnInit(): void {
    this.webSocketService.connect().then(() => {
      this.panicSubscription = this.webSocketService
        .subscribeToAdminPanic()
        .subscribe((payload: any) => {
          this.snackBarService.show(
            `PANIC ALERT: Ride #${payload.rideId} - User: ${payload.userEmail}`
          );
        });
    });
  }

  ngAfterViewInit(): void {
    this.mapClickListener = (ev: Event) => this.handleMapClick(ev);
    if (this.mapEl?.nativeElement && this.mapClickListener) {
      this.mapEl.nativeElement.addEventListener('map-click', this.mapClickListener as EventListener);
    }
  }

  ngOnDestroy(): void {
    if (this.mapEl?.nativeElement && this.mapClickListener) {
      this.mapEl.nativeElement.removeEventListener('map-click', this.mapClickListener as EventListener);
    }
    if (this.panicSubscription) {
      this.panicSubscription.unsubscribe();
    }
  }

  private handleMapClick(ev: Event): void {
  }
}
