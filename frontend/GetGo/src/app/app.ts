import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { UnregisteredNavBarComponent} from './layout/unregistered-nav-bar/unregistered-nav-bar.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, UnregisteredNavBarComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('GetGo');
}
