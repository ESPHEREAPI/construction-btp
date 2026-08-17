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
  error = '';
  units = Object.values(MaterialUnit);

  categories: string[] = [];
  suppliers: string[] = [];
  showCustomCategory = false;
  showCustomSupplier = false;

  constructor(
    private fb: FormBuilder,
    private materialService: MaterialService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadCategories();
    this.loadSuppliers();
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.materialId = +params['id'];
        this.loadMaterial(this.materialId);
      }
    });
  }

  private loadCategories(): void {
    this.materialService.getCategories().subscribe({
      next: (data) => {
        this.categories = data;
        this.ensureOptionPresent(this.categories, this.materialForm.get('category')?.value);
      }
    });
  }

  private loadSuppliers(): void {
    this.materialService.getSuppliers().subscribe({
      next: (data) => {
        this.suppliers = data;
        this.ensureOptionPresent(this.suppliers, this.materialForm.get('supplier')?.value);
      }
    });
  }

  /** Keeps a value already on the form selectable even if it wasn't in the fetched distinct list yet. */
  private ensureOptionPresent(options: string[], value: string | null | undefined): void {
    if (value && !options.includes(value)) {
      options.push(value);
    }
  }

  onCategoryChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.materialForm.get('category')?.setValue(value);
  }

  onSupplierChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.materialForm.get('supplier')?.setValue(value);
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
      next: (material) => {
        this.materialForm.patchValue(material);
        this.ensureOptionPresent(this.categories, material.category);
        this.ensureOptionPresent(this.suppliers, material.supplier);
      }
    });
  }

  onSubmit(): void {
    if (this.materialForm.invalid) return;

    this.loading = true;
    this.error = '';
    const material: Material = this.materialForm.value;

    const operation = this.isEditMode && this.materialId
      ? this.materialService.update(this.materialId, material)
      : this.materialService.create(material);

    operation.subscribe({
      next: () => this.router.navigate(['/materials']),
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || err?.error?.code
          || Object.values(err?.error || {}).join(' ')
          || 'Erreur lors de l\'enregistrement du matériau';
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/materials']);
  }
}
