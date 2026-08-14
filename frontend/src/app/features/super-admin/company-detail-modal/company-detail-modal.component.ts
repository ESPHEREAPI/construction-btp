import { Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Company } from '../../../core/models/company.model';
import { UserManagement } from '../../../core/models/admin.model';
import { AdminService } from '../../../core/services/admin.service';

@Component({
  selector: 'app-company-detail-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './company-detail-modal.component.html',
  styleUrls: ['./company-detail-modal.component.scss']
})
export class CompanyDetailModalComponent implements OnChanges {
  @Input({ required: true }) company!: Company;
  @Output() closed = new EventEmitter<void>();

  users: UserManagement[] = [];
  loading = true;
  error = '';

  resetPasswordResult: { username: string; tempPassword: string } | null = null;
  copiedTempPassword = false;

  constructor(private adminService: AdminService) {}

  ngOnChanges(): void {
    this.resetPasswordResult = null;
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.error = '';
    this.adminService.getAllUsers(0, 100, this.company.id).subscribe({
      next: (response) => {
        this.users = response.content;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des utilisateurs';
        this.loading = false;
        console.error(err);
      }
    });
  }

  resetPassword(user: UserManagement): void {
    if (!confirm(`Réinitialiser le mot de passe de ${user.username} ?`)) return;
    this.adminService.resetPassword(user.id).subscribe({
      next: (result) => {
        this.resetPasswordResult = { username: user.username, tempPassword: result.tempPassword };
        this.copiedTempPassword = false;
      },
      error: (err) => {
        alert('Erreur lors de la réinitialisation du mot de passe');
        console.error(err);
      }
    });
  }

  copyTempPassword(): void {
    if (!this.resetPasswordResult) return;
    navigator.clipboard.writeText(this.resetPasswordResult.tempPassword).then(() => {
      this.copiedTempPassword = true;
      setTimeout(() => (this.copiedTempPassword = false), 2000);
    });
  }

  close(): void {
    this.closed.emit();
  }
}
