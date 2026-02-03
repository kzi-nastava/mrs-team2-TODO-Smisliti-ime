import { Component, inject, signal, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { SupportChatService } from '../../service/support-chat/support-chat.service';
import { Message } from '../../model/support-chat.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-support-chat',
  imports: [CommonModule],
  templateUrl: './support-chat.component.html',
  styleUrl: './support-chat.component.css',
})
export class SupportChatComponent implements AfterViewChecked {
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;

  chatService = inject(SupportChatService);

  messages = signal<Message[]>([]);
  newMessage = signal('');
  chatId!: number;

  ngOnInit() {
    this.chatService.getMyChat().subscribe(chat => {
      this.chatId = chat.id;

      this.chatService.getMyMessages()
        .subscribe(msgs => this.messages.set(msgs));

      this.chatService.subscribe(chat.id, msg => {
        this.messages.update(m => [...m, msg]);
      });
    });
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  private scrollToBottom() {
    try {
      this.messagesContainer.nativeElement.scrollTop = this.messagesContainer.nativeElement.scrollHeight;
    } catch (err) {}
  }


  send() {
    const text = this.newMessage().trim();
    if (!text) return;

    this.chatService.sendMessage(this.chatId, text).subscribe(() => {
      this.newMessage.set('');
    });
  }


  isMine(msg: Message) {
    return msg.senderType === 'USER';
  }
}
