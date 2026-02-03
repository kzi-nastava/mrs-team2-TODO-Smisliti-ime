import { Component } from '@angular/core';
import { signal } from '@angular/core';
import { SupportChatService } from '../../service/support-chat/support-chat.service';
import { Chat, Message } from '../../model/support-chat.model';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin-support-chat',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-support-chat.component.html',
  styleUrl: './admin-support-chat.component.css',
})
export class AdminSupportChatComponent {
  chats = signal<Chat[]>([]);
  selectedChat = signal<Chat | null>(null);
  messages = signal<Message[]>([]);
  newMessage = signal('');

  constructor(private chatService: SupportChatService) {}

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
    this.selectedChat.set(chat);
    this.chatService.getMessages(chat.id).subscribe(msgs => this.messages.set(msgs));
  }

  sendMessage() {
    if (!this.selectedChat() || !this.newMessage().trim()) return;

    this.chatService.sendMessageAdmin(this.selectedChat()!.id, this.newMessage())
      .subscribe(() => {
        this.newMessage.set('');
        this.selectChat(this.selectedChat()!); // reload messages
      });
  }
}
