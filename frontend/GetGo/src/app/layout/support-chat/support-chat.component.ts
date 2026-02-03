import { Component, inject, signal } from '@angular/core';
import { SupportChatService } from '../../service/support-chat/support-chat.service';
import { Message } from '../../model/support-chat.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-support-chat',
  imports: [CommonModule],
  templateUrl: './support-chat.component.html',
  styleUrl: './support-chat.component.css',
})
export class SupportChatComponent  {
  chat = inject(SupportChatService);

  newMessage = signal('');

  send() {
    const text = this.newMessage().trim();
    if (!text) return;

    this.chat.sendMessage(text);
    this.newMessage.set('');
  }

  isMine(msg: any) {
    return msg.senderType === 'USER';
  }
}
