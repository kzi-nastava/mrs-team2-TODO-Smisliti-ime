import { Component, signal, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { SupportChatService } from '../../service/support-chat/support-chat.service';
import { Chat, Message } from '../../model/support-chat.model';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { WebSocketService } from '../../service/websocket/websocket.service';

@Component({
  selector: 'app-admin-support-chat',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-support-chat.component.html',
  styleUrl: './admin-support-chat.component.css',
})
export class AdminSupportChatComponent implements AfterViewChecked {
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;

  chats = signal<Chat[]>([]);
  selectedChat = signal<Chat | null>(null);
  messages = signal<Message[]>([]);
  newMessage = signal('');
  private subscribedChats = new Set<number>();

  constructor(private chatService: SupportChatService, private wsService: WebSocketService) {}

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  private scrollToBottom() {
    try {
      this.messagesContainer.nativeElement.scrollTop = this.messagesContainer.nativeElement.scrollHeight;
    } catch (err) {}
  }

  ngOnInit() {
    this.chatService.getAllChats().subscribe(chats => {
      const flatChats: Chat[] = chats.map(c => ({
        id: c.id,
        user: c.user,
        messages: [],
      }));
      this.chats.set(flatChats);

    });
  }

  selectChat(chat: Chat) {
    console.log('WS connected?', this.wsService.connectionStatus);
    this.selectedChat.set(chat);
    this.chatService.getMessages(chat.id).subscribe(msgs => this.messages.set(msgs));
    this.subscribeToChat(chat.id);
  }

  sendMessage() {
    if (!this.selectedChat() || !this.newMessage().trim()) return;

    const chatId = this.selectedChat()!.id;
    const text = this.newMessage();

    this.chatService.sendMessageAdmin(chatId, text).subscribe(() => {
      this.newMessage.set('');
    });
  }



  subscribeToChat(chatId: number) {
    if (this.subscribedChats.has(chatId)) return;

    this.subscribedChats.add(chatId);

    this.wsService.subscribeToChat(chatId)
      .subscribe((msg: Message) => {
        console.log('WS message received:', msg);
        this.messages.update(m => [...m, msg]);
      });
  }

  ngOnDestroy() {
  }
}
