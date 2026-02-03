export interface Message {
  text: string;
  senderType: 'USER' | 'DRIVER' | 'ADMIN';
  timestamp: string;
}

export interface Chat {
  id: number;
  user: { id: number; name: string; };
  messages: Message[];
}
