import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ProjectService } from '../../../core/services/project.service';
import { AuthService } from '../../../core/services/auth.service';
import { Project } from '../../../core/models/project.model';

@Component({
  selector: 'app-project-list',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule],
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.scss']
})
export class ProjectListComponent implements OnInit {
  projects: Project[] = [];
  loading = true;
  error = '';

  constructor(private projectService: ProjectService, private authService: AuthService) {}

  /**
   * By default only Admin Compagnie/Administrateur/Super Admin have PROJECT_CREATE/
   * UPDATE/DELETE (system roles hold every permission; operational roles don't get
   * these by default) - but a company can now grant them to another role via Role
   * management, so this checks the actual permission rather than a hardcoded role list,
   * matching the backend's ProjectController @PreAuthorize checks.
   */
  get canCreate(): boolean {
    return this.authService.hasPermission('PROJECT_CREATE');
  }

  get canEdit(): boolean {
    return this.authService.hasPermission('PROJECT_UPDATE');
  }

  get canDelete(): boolean {
    return this.authService.hasPermission('PROJECT_DELETE');
  }

  get canManageProjects(): boolean {
    return this.canEdit || this.canDelete;
  }

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects(): void {
    this.projectService.getAll().subscribe({
      next: (data) => {
        this.projects = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des projets';
        this.loading = false;
        console.error('❌ Error:', err);
      }
    });
  }

  deleteProject(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce projet ?')) {
      this.projectService.delete(id).subscribe({
        next: () => {
          this.loadProjects();
        },
        error: (err) => {
          alert('Erreur lors de la suppression');
        }
      });
    }
  }
}
