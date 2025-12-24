import { Component, OnInit } from '@angular/core';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-registered-home',
  templateUrl: './registered-home.component.html',
  imports: [
    FormsModule
  ],
  styleUrls: ['./registered-home.component.css']
})
export class RegisteredHomeComponent {
  protected destination: any;

}
