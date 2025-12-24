import {Component, EventEmitter, Input, Output} from '@angular/core';
import { MatRadioModule } from '@angular/material/radio';
import { MatButtonModule } from '@angular/material/button';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-role-selector',
  imports: [
    MatRadioModule,
    MatButtonModule,
    FormsModule
  ],
  templateUrl: './role-selector.html',
  styleUrl: './role-selector.css',
})
export class RoleSelector {
  @Input() visible = true;
  @Output() selectRole = new EventEmitter<"admin" | "driver" | "passenger">();
  @Output() close = new EventEmitter<void>();

  selected: 'admin' | 'driver' | 'passenger' = 'passenger';

  confirm() {
    this.selectRole.emit(this.selected);
    this.close.emit();
  }

  cancel() {
    this.close.emit();
  }
}
