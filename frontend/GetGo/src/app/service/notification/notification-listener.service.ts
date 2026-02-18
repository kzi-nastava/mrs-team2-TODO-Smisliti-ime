import { Injectable } from '@angular/core';
import { WebSocketService } from '../websocket/websocket.service';
import { AuthService } from '../auth-service/auth.service';
import { SnackBarService } from '../snackBar/snackBar.service';
import { Subject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class NotificationListenerService {
  private listening = false;
  notifications$ = new Subject<any>();

  constructor(
    private ws: WebSocketService,
    private auth: AuthService,
    private snackBar: SnackBarService
  ) {}

  async startListening(): Promise<void> {
    if (this.listening) return;
    this.listening = true;

    const userId = this.auth.getUserId();
    if (!userId) return;

    if (!this.ws.connectionStatus) {
      await this.ws.connect();
    }

    this.ws.createSubscription(`/socket-publisher/user/${userId}/notification`)
      .subscribe(notification => {
        this.notifications$.next(notification);
        this.snackBar.show(notification.message, true, 5000);
      });
  }

  stopListening(): void {
    this.listening = false;
    this.ws.disconnect();
  }
}
