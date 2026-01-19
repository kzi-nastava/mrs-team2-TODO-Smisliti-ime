import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AbstractControl, FormGroup } from '@angular/forms';

@Injectable({
  providedIn: 'root'
})
export class SnackBarService {
  constructor(private snackBar: MatSnackBar) {}

  show(message: string | string[], success = false, duration = 8000) {
    const formattedMessage = Array.isArray(message)
      ? message.map((line, i) => `${i + 1}. ${line}`).join('\n')
      : message;

    this.snackBar.open(formattedMessage, 'Close', {
      duration,
      horizontalPosition: 'end',
      verticalPosition: 'bottom',
      panelClass: success ? ['success-snackbar'] : ['error-snackbar'],
      politeness: 'assertive'
    });
  }

  showFormErrors(form: FormGroup, fieldMap?: { [key: string]: string }) {
    const messages: string[] = [];

    Object.keys(form.controls).forEach(key => {
      const control = form.get(key);
      if (control && control.errors) {
        const fieldName = fieldMap?.[key] ?? key;
        Object.keys(control.errors).forEach(errorKey => {
          let msg = '';
          switch (errorKey) {
            case 'required':
              msg = `${fieldName} is required`;
              break;
            case 'email':
              msg = `${fieldName} must be a valid email`;
              break;
            case 'minlength':
              const minLen = control.errors!['minlength'].requiredLength;
              msg = `${fieldName} must be at least ${minLen} characters`;
              break;
            case 'maxlength':
              const maxLen = control.errors!['maxlength'].requiredLength;
              msg = `${fieldName} must be at most ${maxLen} characters`;
              break;
            case 'pattern':
              if (key === 'phoneNumber') {
                msg = `${fieldName} must be a valid Serbian number (e.g. +38161234567)`;
              } else if (key === 'email') {
                msg = `${fieldName} format is invalid (e.g. user@example.com)`;
              } else {
                msg = `${fieldName} format is invalid`;
              }
              break;
            default:
              msg = `${fieldName} is invalid`;
          }
          messages.push(msg);
        });
      }
    });

    if (form.errors?.['passwordsDontMatch']) {
      messages.push('Passwords do not match');
    }

    if (messages.length > 0) {
      this.show(messages);
    }

    return messages.length === 0;
  }
}
