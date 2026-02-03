import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Message } from '../../model/support-chat.model';
import { rxResource } from '@angular/core/rxjs-interop';
import { environment } from '../../../env/environment';
import { of } from 'rxjs';


@Injectable({
  providedIn: 'root',
})
export class SupportChatService {
   private http = inject(HttpClient);

  // trigger za reload
  private reloadTrigger = signal(0);

  messagesResource = rxResource({
    params: () => ({ reload: this.reloadTrigger() }),
    stream: () => {
      return this.http.get<Message[]>(
        `${environment.apiHost}/api/support/messages`
      );
    }
  });

  messages = this.messagesResource.value;

  sendMessage(text: string) {
    return this.http.post(
      `${environment.apiHost}/api/support/messages`,
      { text }
    ).subscribe(() => {
      this.reloadTrigger.update(v => v + 1);
    });
  }
}
