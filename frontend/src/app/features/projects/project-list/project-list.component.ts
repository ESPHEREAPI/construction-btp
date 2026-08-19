import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ProjectService } from '../../../core/services/project.service';
import { AuthService } from '../../../core/services/auth.service';
import { Project } from '../../../core/models/project.model';

const PROJECT_MANAGEMENT_ROLES = ['ROLE_SUPER_ADMIN', 'ROLE_COMPANY_ADMIN', 'ROLE_ADMIN'];

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

  /** Only Admin Compagnie/Administrateur/Super Admin create, edit or delete projects - everyone else just works within their assigned project. */
  get canManageProjects(): boolean {
    return this.authService.hasAnyRole(PROJECT_MANAGEMENT_ROLES);
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
