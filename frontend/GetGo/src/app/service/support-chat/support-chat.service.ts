import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Message, Chat } from '../../model/support-chat.model';
import { rxResource } from '@angular/core/rxjs-interop';
import { environment } from '../../../env/environment';
import { of } from 'rxjs';
import { WebSocketService } from '../../service/websocket/websocket.service';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';



@Injectable({
  providedIn: 'root',
})
export class SupportChatService {
   private http = inject(HttpClient);
   ws = inject(WebSocketService);


  // trigger za reload
  private reloadTrigger = signal(0);

  sendMessage(chatId: number, text: string) {
    return this.http.post(`${environment.apiHost}/api/support/messages`, { text })
  }


  getAllChats() {
    return this.http.get<Chat[]>(`${environment.apiHost}/api/support/admin/chats`);
  }

  getMessages(chatId: number) {
    return this.http.get<Message[]>(`${environment.apiHost}/api/support/admin/messages/${chatId}`);
  }

  sendMessageAdmin(chatId: number, text: string) {
    return this.http.post(`${environment.apiHost}/api/support/admin/messages/${chatId}`, { text });
  }

  getMyChat(): Observable<{ id: number }> {
    return this.http.get<{ id: number }>(`${environment.apiHost}/api/support/chat/my`);
  }

  getMyMessages() {
    return this.http.get<Message[]>(`${environment.apiHost}/api/support/messages`);
  }

  subscribe(chatId: number, callback: (msg: Message) => void) {
    if (!this.ws.connectionStatus) {
      this.ws.connect().then(() => {
        this.ws.subscribeToChat(chatId).subscribe(msg => {

          console.log('WS MESSAGE (PASSENGER)', msg);

          callback(msg);
        });
      });
    } else {
      this.ws.subscribeToChat(chatId).subscribe(callback);
    }
  }

}
