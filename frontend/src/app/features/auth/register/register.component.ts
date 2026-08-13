import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../core/services/auth.service';
import { RegisterRequest } from '../../../core/models/user.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, TranslateModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  request: RegisterRequest = {
    companyName: '',
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    phone: ''
  };
  confirmPassword = '';
  loading = false;
  error = '';
  success = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(): void {
    this.error = '';
    this.success = '';

    if (!this.request.companyName || !this.request.username || !this.request.email || !this.request.password) {
      this.error = 'Veuillez remplir tous les champs obligatoires';
      return;
    }

    if (this.request.password !== this.confirmPassword) {
      this.error = 'Les mots de passe ne correspondent pas';
      return;
    }

    this.loading = true;

    this.authService.register(this.request).subscribe({
      next: () => {
        this.loading = false;
        this.success = 'Compagnie créée avec succès. Vous pouvez maintenant vous connecter.';
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || err?.message || 'Une erreur est survenue lors de l\'inscription';
      }
    });
  }
}
