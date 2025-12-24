import { Component } from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {RouterLink} from '@angular/router';
import { AuthService } from '../auth-service/auth.service';
import { Router } from '@angular/router';
import {UserRole} from '../../../model/user.model';
import {RoleSelector} from '../role-selector/role-selector';

@Component({
  selector: 'app-login',
  imports: [CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule, MatIconModule, MatCheckboxModule, RouterLink, RoleSelector],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginComponent {
  createLoginForm = new FormGroup({
    email: new FormControl('', Validators.required),
    password: new FormControl('', Validators.required),
  });

  pendingCredentials: { email: string | undefined | null; password: string | undefined | null } | null = null;
  showRoleSelector = false;
  constructor(private auth: AuthService, private router: Router) {}

  create() {
    if (this.createLoginForm.valid) {
      // TODO: Implement actual login logic here
    }
  }

  login() {
    if (!this.createLoginForm.valid) {
      console.log('Form is not valid!');
      return;
    }

    this.pendingCredentials = {
      email: this.createLoginForm.value.email ?? null,
      password: this.createLoginForm.value.password ?? null
    };
    this.showRoleSelector = true;
  }

  onRoleSelected(role: "admin" | "driver" | "passenger") {
    this.showRoleSelector = false;

    // navigate based on role
    switch (role) {
      case 'admin':
        this.auth.loginAs(UserRole.Admin);
        break;
      case 'driver':
        this.auth.loginAs(UserRole.Driver);
        break;
      case 'passenger':
        this.auth.loginAs(UserRole.Passenger);
        break;
    }
    this.router.navigate(['/home']);
  }
}
