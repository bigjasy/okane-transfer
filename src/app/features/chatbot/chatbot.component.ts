import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DataService } from '../../core/services/data.service';
import { ChatbotRequest } from '../../core/models/notification.models';

interface ChatMessage { from: string; text: string; }

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
<section class="page">
  <h1 class="page-title">Chatbot OkaneTransfer</h1>
  <div class="card chat">
    @for (m of messages; track $index) {
      <div [class.user]="m.from === 'user'" class="bubble">
        <b>{{ m.from }}</b>
        <p>{{ m.text }}</p>
      </div>
    }
    <form (ngSubmit)="send()" class="send">
      <input name="msg" [(ngModel)]="text" placeholder="Posez votre question..." [disabled]="loading">
      <button class="btn primary" [disabled]="loading">Envoyer</button>
    </form>
  </div>
</section>`,
  styles: [`.chat{display:grid;gap:.8rem}.bubble{max-width:75%;background:#f1f5f9;padding:.8rem;border-radius:1rem}.bubble.user{margin-left:auto;background:#dbeafe}.bubble p{margin:.25rem 0 0}.send{display:flex;gap:.6rem}`]
})
export class ChatbotComponent {
  messages: ChatMessage[] = [{ from: 'bot', text: 'Bonjour, comment puis-je vous aider ?' }];
  text = '';
  loading = false;

  constructor(private data: DataService) {}

  send(): void {
    const msg = this.text.trim();
    if (!msg) return;
    this.messages.push({ from: 'user', text: msg });
    this.text = '';
    this.loading = true;
    const req: ChatbotRequest = { message: msg, language: 'FR' };
    this.data.chatbot(req).subscribe({
      next: r => {
        this.messages.push({ from: r.escalated ? 'support' : 'bot', text: r.answer });
        this.loading = false;
      },
      error: (err) => {
        const text = err?.status === 404
          ? 'Endpoint chatbot non disponible côté backend.'
          : 'Erreur de communication avec le chatbot.';
        this.messages.push({ from: 'bot', text });
        this.loading = false;
      }
    });
  }
}
