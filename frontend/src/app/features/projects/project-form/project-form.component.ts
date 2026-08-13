import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ProjectService } from '../../../core/services/project.service';
import { Project, ProjectStatus } from '../../../core/models/project.model';

@Component({
  selector: 'app-project-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TranslateModule, RouterModule],
  templateUrl: './project-form.component.html',
  styleUrls: ['./project-form.component.scss']
})
export class ProjectFormComponent implements OnInit {
  projectForm!: FormGroup;
  isEditMode = false;
  projectId?: number;
  loading = false;
  statuses = Object.values(ProjectStatus);

  constructor(
    private fb: FormBuilder,
    private projectService: ProjectService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.initForm();
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.projectId = +params['id'];
        this.loadProject(this.projectId);
      }
    });
  }

  initForm(): void {
    this.projectForm = this.fb.group({
      code: ['', Validators.required],
      name: ['', Validators.required],
      description: [''],
      location: [''],
      client: [''],
      status: [ProjectStatus.PLANNED, Validators.required],
      startDate: ['', Validators.required],
      endDate: [''],
      budget: [0]
    });
  }

  loadProject(id: number): void {
    this.projectService.getById(id).subscribe({
      next: (project) => {
        this.projectForm.patchValue(project);
      }
    });
  }

  onSubmit(): void {
    if (this.projectForm.invalid) return;

    this.loading = true;
    const project: Project = this.projectForm.value;

    const operation = this.isEditMode && this.projectId
      ? this.projectService.update(this.projectId, project)
      : this.projectService.create(project);

    operation.subscribe({
      next: () => this.router.navigate(['/projects']),
      error: (err) => {
        this.loading = false
        console.log(err)
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/projects']);
  }
}
