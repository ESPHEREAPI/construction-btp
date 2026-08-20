import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { AdminService } from '../../../core/services/admin.service';
import { Permission } from '../../../core/models/admin.model';

interface PermissionGroup {
  category: string;
  permissions: Permission[];
}

@Component({
  selector: 'app-role-form',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, TranslateModule],
  templateUrl: './role-form.component.html',
  styleUrls: ['./role-form.component.scss']
})
export class RoleFormComponent implements OnInit {
  role: { nameFr: string; nameEn: string; namePt: string; permissionIds: number[] } = {
    nameFr: '',
    nameEn: '',
    namePt: '',
    permissionIds: []
  };

  isEditMode = false;
  loading = false;
  error = '';

  permissionGroups: PermissionGroup[] = [];

  constructor(
    private adminService: AdminService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.adminService.getAllPermissions().subscribe({
      next: (permissions) => (this.permissionGroups = this.groupByCategory(permissions)),
      error: (err) => console.error(err)
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.loadRole(+id);
    }
  }

  private groupByCategory(permissions: Permission[]): PermissionGroup[] {
    const map = new Map<string, Permission[]>();
    for (const p of permissions) {
      if (!map.has(p.category)) {
        map.set(p.category, []);
      }
      map.get(p.category)!.push(p);
    }
    return Array.from(map.entries())
      .sort((a, b) => a[0].localeCompare(b[0]))
      .map(([category, perms]) => ({ category, permissions: perms }));
  }

  loadRole(id: number): void {
    this.loading = true;
    this.adminService.getRoleById(id).subscribe({
      next: (data) => {
        this.role = {
          nameFr: data.nameFr || data.name,
          nameEn: data.nameEn || '',
          namePt: data.namePt || '',
          permissionIds: data.permissions.map(p => p.id)
        };
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement du rôle';
        this.loading = false;
        console.error(err);
      }
    });
  }

  togglePermission(permissionId: number): void {
    const index = this.role.permissionIds.indexOf(permissionId);
    if (index >= 0) {
      this.role.permissionIds.splice(index, 1);
    } else {
      this.role.permissionIds.push(permissionId);
    }
  }

  onSubmit(): void {
    this.loading = true;
    this.error = '';

    const id = this.route.snapshot.paramMap.get('id');
    const request = this.isEditMode && id
      ? this.adminService.updateRole(+id, this.role)
      : this.adminService.createRole(this.role);

    request.subscribe({
      next: () => this.router.navigate(['/admin/roles']),
      error: (err) => {
        this.error = err?.error?.message || "Erreur lors de l'enregistrement";
        this.loading = false;
        console.error(err);
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/admin/roles']);
  }
}
