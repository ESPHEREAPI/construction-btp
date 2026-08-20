import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { Role } from '../../../core/models/admin.model';
import { AdminService } from '../../../core/services/admin.service';
import { RoleNamePipe } from '../../../shared/pipes/role-name.pipe';

@Component({
  selector: 'app-role-list',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule, RoleNamePipe],
  templateUrl: './role-list.component.html',
  styleUrls: ['./role-list.component.scss']
})
export class RoleListComponent implements OnInit {
  roles: Role[] = [];
  loading = false;
  error = '';

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadRoles();
  }

  loadRoles(): void {
    this.loading = true;
    this.error = '';
    this.adminService.getAssignableRoles().subscribe({
      next: (roles) => {
        // System roles (Super Admin/Company Admin/Admin) never appear here.
        this.roles = roles.filter(r => !r.systemRole);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des rôles';
        this.loading = false;
        console.error(err);
      }
    });
  }

  deleteRole(role: Role): void {
    if (!confirm(`Supprimer le rôle "${role.nameFr || role.name}" ?`)) return;
    this.adminService.deleteRole(role.id).subscribe({
      next: () => this.loadRoles(),
      error: (err) => {
        alert(err?.error?.message || 'Erreur lors de la suppression du rôle');
        console.error(err);
      }
    });
  }
}
