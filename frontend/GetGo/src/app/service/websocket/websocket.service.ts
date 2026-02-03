import { Injectable } from '@angular/core';
import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { Message } from '../../model/support-chat.model';


@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private stompClient: Client | null = null;
  private connected$ = new BehaviorSubject<boolean>(false);
  private subscriptions = new Map<string, StompSubscription>();
  private chatMessages = new Map<number, Subject<Message>>();

  constructor() {}

  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      // Create SockJS socket
      const socket = new SockJS('http://localhost:8080/socket');

      this.stompClient = new Client({
        webSocketFactory: () => socket as any,
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,

        onConnect: () => {
          console.log('WebSocket connected');
          this.connected$.next(true);
          resolve();
        },

        onStompError: (frame) => {
          console.error('WebSocket STOMP error:', frame);
          this.connected$.next(false);
          reject(frame);
        },

        onWebSocketClose: () => {
          console.log('WebSocket connection closed');
          this.connected$.next(false);
        }
      });

      this.stompClient.activate();
    });
  }

  disconnect(): void {
    if (this.stompClient) {
      this.subscriptions.forEach(sub => sub.unsubscribe());
      this.subscriptions.clear();
      this.stompClient.deactivate();
      this.connected$.next(false);
      console.log('WebSocket disconnected');
    }
  }

  subscribeToDriverRideAssigned(driverEmail: string): Observable<any> {
    const topic = `/socket-publisher/driver/${driverEmail}/ride-assigned`;
    return this.createSubscription(topic);
  }

  subscribeToDriverLocation(driverEmail: string): Observable<any> {
    const topic = `/socket-publisher/driver/${driverEmail}/location`;
    return this.createSubscription(topic);
  }

  subscribeToDriverStatusUpdates(driverEmail: string): Observable<any> {
    const topic = `/socket-publisher/driver/${driverEmail}/status-update`;
    return this.createSubscription(topic);
  }

  subscribeToRideFinished(driverEmail: string): Observable<any> {
      const topic = `/socket-publisher/driver/${driverEmail}/ride-finished`;
      return this.createSubscription(topic);
  }

  subscribeToRideDriverLocation(rideId: number): Observable<any> {
    const topic = `/socket-publisher/ride/${rideId}/driver-location`;
    return this.createSubscription(topic);
  }

  subscribeToRideStatusUpdates(rideId: number): Observable<any> {
    const topic = `/socket-publisher/ride/${rideId}/status-update`;
    return this.createSubscription(topic);
  }

  subscribeToPassengerRideFinished(rideId: number): Observable<any> {
    const topic = `/socket-publisher/ride/${rideId}/ride-finished`;
    return this.createSubscription(topic);
  }

  // Generic subscription creator
  public createSubscription(topic: string): Observable<any> {
    return new Observable(observer => {
      if (!this.stompClient || !this.connected$.value) {
        observer.error('WebSocket not connected');
        return;
      }

      console.log(`Subscribing to: ${topic}`);

      const subscription = this.stompClient.subscribe(topic, (message) => {
        try {
          const payload = JSON.parse(message.body);
          /*console.log(`Received message from ${topic}:`, payload);*/
          observer.next(payload);
        } catch (error) {
          console.error('Failed to parse WebSocket message:', error);
          observer.error(error);
        }
      });

      // Store subscription for cleanup
      this.subscriptions.set(topic, subscription);

      // Cleanup on unsubscribe
      return () => {
        console.log(`Unsubscribing from: ${topic}`);
        subscription.unsubscribe();
        this.subscriptions.delete(topic);
      };
    });
  }

  isConnected(): Observable<boolean> {
    return this.connected$.asObservable();
  }

  get connectionStatus(): boolean {
    return this.connected$.value;
  }

  subscribeToAdminPanic(): Observable<any> {
    return this.createSubscription('/socket-publisher/admin/panic-alerts');
  }

  subscribeToPassengerRideStopped(rideId: number): Observable<any> {
    return this.createSubscription(`/socket-publisher/ride/${rideId}/ride-stopped`);
  }

  subscribeToChat(chatId: number): Observable<Message> {
    if (!this.chatMessages.has(chatId)) {
      const subject = new Subject<Message>();
      this.chatMessages.set(chatId, subject);

      // wait for connection if not connected
      const connect$ = new Observable<void>(observer => {
        if (this.connected$.value) {
          observer.next();
          observer.complete();
        } else {
          const sub = this.connected$.subscribe(connected => {
            if (connected) {
              observer.next();
              observer.complete();
              sub.unsubscribe();
            }
          });
        }
      });

      connect$.subscribe(() => {
        this.createSubscription(`/socket-publisher/chat/${chatId}`)
          .subscribe(msg => subject.next(msg));
      });
    }

    return this.chatMessages.get(chatId)!.asObservable();
  }

}
