import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ProjectService } from '../../../core/services/project.service';
import { AuthService } from '../../../core/services/auth.service';
import { Project } from '../../../core/models/project.model';

const PROJECT_MANAGEMENT_ROLES = ['ROLE_SUPER_ADMIN', 'ROLE_COMPANY_ADMIN', 'ROLE_ADMIN'];

@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule],
  templateUrl: './project-detail.component.html',
  styleUrls: ['./project-detail.component.scss']
})
export class ProjectDetailComponent implements OnInit {
  project?: Project;
  loading = true;

  constructor(
    private projectService: ProjectService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  get canManageProjects(): boolean {
    return this.authService.hasAnyRole(PROJECT_MANAGEMENT_ROLES);
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.loadProject(+params['id']);
      }
    });
  }

  loadProject(id: number): void {
    this.projectService.getById(id).subscribe({
      next: (data) => {
        this.project = data;
        this.loading = false;
      },
      error: () => this.router.navigate(['/projects'])
    });
  }

  deleteProject(): void {
    if (confirm('Êtes-vous sûr ?') && this.project?.id) {
      this.projectService.delete(this.project.id).subscribe({
        next: () => this.router.navigate(['/projects'])
      });
    }
  }
}
