import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../core/services/auth.service';
import { ChangePasswordRequest } from '../../../core/models/user.model';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.scss']
})
export class ChangePasswordComponent {
  request: ChangePasswordRequest = {
    currentPassword: '',
    newPassword: ''
  };
  confirmPassword = '';
  loading = false;
  error = '';

  forced: boolean;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.forced = this.authService.mustChangePassword();
  }

  onSubmit(): void {
    this.error = '';

    if (!this.request.currentPassword || !this.request.newPassword) {
      this.error = 'Veuillez remplir tous les champs';
      return;
    }

    if (this.request.newPassword !== this.confirmPassword) {
      this.error = 'Les mots de passe ne correspondent pas';
      return;
    }

    this.loading = true;

    this.authService.changePassword(this.request).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || err?.message || 'Une erreur est survenue';
      }
    });
  }
}
