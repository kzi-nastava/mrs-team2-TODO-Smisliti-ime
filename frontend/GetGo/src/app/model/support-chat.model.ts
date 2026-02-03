export interface Message {
  text: string;
  senderType: 'USER' | 'DRIVER' | 'ADMIN';
  timestamp: string;
}
