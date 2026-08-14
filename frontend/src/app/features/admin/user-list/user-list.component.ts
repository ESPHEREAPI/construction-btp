import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { UserManagement } from '../../../core/models/admin.model';
import { AdminService } from '../../../core/services/admin.service';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, TranslateModule],
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent implements OnInit {
  users: UserManagement[] = [];
  loading = false;
  error = '';
  
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;

  resetPasswordResult: { username: string; tempPassword: string } | null = null;
  copiedTempPassword = false;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.error = '';
    
    this.adminService.getAllUsers(this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        this.users = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des utilisateurs';
        this.loading = false;
        console.error(err);
      }
    });
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadUsers();
  }

  toggleUserStatus(id: number): void {
    if (confirm('Changer le statut de cet utilisateur ?')) {
      this.adminService.toggleUserStatus(id).subscribe({
        next: () => this.loadUsers(),
        error: (err) => {
          alert('Erreur lors du changement de statut');
          console.error(err);
        }
      });
    }
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

  deleteUser(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')) {
      this.adminService.deleteUser(id).subscribe({
        next: () => this.loadUsers(),
        error: (err) => {
          alert('Erreur lors de la suppression');
          console.error(err);
        }
      });
    }
  }
}
