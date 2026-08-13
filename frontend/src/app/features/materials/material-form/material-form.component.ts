import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { MaterialService } from '../../../core/services/material.service';
import { Material, MaterialUnit } from '../../../core/models/material.model';

@Component({
  selector: 'app-material-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TranslateModule, RouterModule],
  templateUrl: './material-form.component.html',
  styleUrls: ['./material-form.component.scss']
})
export class MaterialFormComponent implements OnInit {
  materialForm!: FormGroup;
  isEditMode = false;
  materialId?: number;
  loading = false;
  units = Object.values(MaterialUnit);

  constructor(
    private fb: FormBuilder,
    private materialService: MaterialService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.materialId = +params['id'];
        this.loadMaterial(this.materialId);
      }
    });
  }

  initForm(): void {
    this.materialForm = this.fb.group({
      code: ['', Validators.required],
      name: ['', Validators.required],
      description: [''],
      unit: [MaterialUnit.PIECE, Validators.required],
      category: [''],
      unitPrice: [0],
      supplier: [''],
      active: [true]
    });
  }

  loadMaterial(id: number): void {
    this.materialService.getById(id).subscribe({
      next: (material) => this.materialForm.patchValue(material)
    });
  }

  onSubmit(): void {
    if (this.materialForm.invalid) return;

    this.loading = true;
    const material: Material = this.materialForm.value;

    const operation = this.isEditMode && this.materialId
      ? this.materialService.update(this.materialId, material)
      : this.materialService.create(material);

    operation.subscribe({
      next: () => this.router.navigate(['/materials']),
      error: () => this.loading = false
    });
  }

  cancel(): void {
    this.router.navigate(['/materials']);
  }
}
