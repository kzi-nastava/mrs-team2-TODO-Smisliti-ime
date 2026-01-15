import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router, RouterModule} from '@angular/router';
import {HttpClient} from '@angular/common/http';
import { environment } from '../../../../env/environment';
import {CommonModule} from '@angular/common';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'app-activate',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div style="text-align:center; padding: 3rem;">
      <p>Activating your account...</p>
    </div>
  `
})
export class ActivateComponent implements OnInit {

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.showAndRedirect('Missing activation token', 'error-snackbar');
      return;
    }

    this.http
      .get<{ activated: boolean; message: string }>(
        `${environment.apiHost}/api/auth/activate?token=${token}`
      )
      .subscribe({
        next: res => {
          if (res.activated) {
            this.showAndRedirect(res.message || 'Account activated successfully', 'success-snackbar');
          } else {
            this.showAndRedirect(res.message || 'Activation failed', 'error-snackbar');
          }
        },
        error: err => {
          const msg = err?.error?.message || 'Activation failed';
          this.showAndRedirect(msg, 'error-snackbar');
        }
      });
  }

  private showAndRedirect(message: string, panelClass: string) {
    const ref = this.snackBar.open(message, 'Go to login', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'bottom',
      panelClass: [panelClass]
    });

    // ✔️ Redirect BOTH when time expires OR user closes it
    ref.afterDismissed().subscribe(() => {
      this.router.navigate(['/login']);
    });
  }
}
