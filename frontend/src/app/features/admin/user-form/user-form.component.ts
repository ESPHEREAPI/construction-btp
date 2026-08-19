import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { AdminService } from '../../../core/services/admin.service';
import { ProjectService } from '../../../core/services/project.service';
import { Role } from '../../../core/models/admin.model';

/** Roles that scope a user to a single assigned project - Super Admin/Company Admin/Admin see everything. */
const PROJECT_SCOPED_ROLE_NAMES = ['ROLE_PROJECT_MANAGER', 'ROLE_SITE_MANAGER', 'ROLE_INVENTORY_MANAGER', 'ROLE_READ_ONLY'];


@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, TranslateModule],
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.scss']
})
export class UserFormComponent implements OnInit {
  user: any = {
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    phone: '',
    roleIds: [],
    assignedProjectId: null
  };

  isEditMode = false;
  loading = false;
  error = '';

  availableRoles: Role[] = [];
  availableProjects: any[] = [];

  constructor(
    private adminService: AdminService,
    private projectService: ProjectService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.adminService.getAssignableRoles().subscribe({
      next: (roles) => (this.availableRoles = roles),
      error: (err) => console.error(err)
    });

    this.projectService.getAll().subscribe({
      next: (data: any) => (this.availableProjects = data?.content || data || []),
      error: (err: any) => console.error(err)
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.loadUser(+id);
    }
  }

  roleLabelKey(roleName: string): string {
    return 'roles.' + roleName;
  }

  toggleRole(roleId: number): void {
    const index = this.user.roleIds.indexOf(roleId);
    if (index >= 0) {
      this.user.roleIds.splice(index, 1);
    } else {
      this.user.roleIds.push(roleId);
    }
  }

  /** Whether at least one selected role is project-scoped - only then does the assignment field make sense. */
  get showProjectAssignment(): boolean {
    return this.availableRoles
      .filter(r => this.user.roleIds.includes(r.id))
      .some(r => PROJECT_SCOPED_ROLE_NAMES.includes(r.name));
  }

  loadUser(id: number): void {
    this.loading = true;
    this.adminService.getUserById(id).subscribe({
      next: (data) => {
        this.user = {
          ...data,
          roleIds: data.roles.map((r: any) => r.id)
        };
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement';
        this.loading = false;
        console.error(err);
      }
    });
  }

  onSubmit(): void {
    this.loading = true;
    this.error = '';

    const request = this.isEditMode
      ? this.adminService.updateUser(this.user.id, this.user)
      : this.adminService.createUser(this.user);

    request.subscribe({
      next: () => {
        this.router.navigate(['/admin/users']);
      },
      error: (err) => {
        this.error = 'Erreur lors de l enregistrement';
        this.loading = false;
        console.error(err);
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/admin/users']);
  }
}
