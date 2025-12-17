import { Component } from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatCheckbox} from '@angular/material/checkbox';
import {RouterLink} from '@angular/router';
import { AuthService } from '../auth-service/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule, MatIcon, MatCheckbox, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginComponent {

  constructor(private auth: AuthService, private router: Router) {}

  createLoginForm = new FormGroup({
    email: new FormControl('', Validators.required),
    password: new FormControl('', Validators.required),
  });

  create() {
    if (this.createLoginForm.valid) {
    }
  }

  // login(){
  //   if(this.createLoginForm.valid){
  //     this.auth.loginAs('user');
  //     this.router.navigate(['/ride']);
  //   }
  // }

  login(){

    if (this.createLoginForm.valid) {

      // Trenutno je ovako ali cemo izmeniti da otvori odgovarajucu stranicu u skladu sa korisnickom rolom
      // koja se ulogovala
      console.log('Role before login:', this.auth.role());
      this.auth.loginAs('user');

      console.log('Role after login:', this.auth.role());

      this.router.navigate(['/ride']);
    } else {
      console.log('Forma nije validna');
    }
  }


}
