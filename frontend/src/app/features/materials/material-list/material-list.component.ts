import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { MaterialService } from '../../../core/services/material.service';
import { Material } from '../../../core/models/material.model';
import { AuthService } from '../../../core/services/auth.service';
import { AppCurrencyPipe } from '../../../shared/pipes/app-currency.pipe';

@Component({
  selector: 'app-material-list',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule, AppCurrencyPipe],
  templateUrl: './material-list.component.html',
  styleUrls: ['./material-list.component.scss']
})
export class MaterialListComponent implements OnInit {
  materials: Material[] = [];
  hiddenMaterials: Material[] = [];
  loading = true;
  showHidden = false;
  error = '';

  constructor(private materialService: MaterialService, private authService: AuthService) {}

  get canCreate(): boolean {
    return this.authService.hasPermission('MATERIAL_CREATE');
  }

  get canUpdate(): boolean {
    return this.authService.hasPermission('MATERIAL_UPDATE');
  }

  get canDelete(): boolean {
    return this.authService.hasPermission('MATERIAL_DELETE');
  }

  ngOnInit(): void {
    this.loadMaterials();
  }

  loadMaterials(): void {
    this.loading = true;
    this.materialService.getAll().subscribe({
      next: (data) => {
        this.materials = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur', err);
        this.loading = false;
      }
    });
  }

  /** A material with no owning company is part of the shared catalog - read/hide-only for a company user. */
  isSystemMaterial(material: Material): boolean {
    return material.company === null || material.company === undefined;
  }

  deleteMaterial(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce matériau ?')) {
      this.materialService.delete(id).subscribe({
        next: () => this.loadMaterials(),
        error: (err) => alert(err?.error?.message || 'Erreur lors de la suppression')
      });
    }
  }

  hideMaterial(id: number): void {
    this.materialService.hide(id).subscribe({
      next: () => this.loadMaterials(),
      error: (err) => alert(err?.error?.message || 'Erreur lors du masquage')
    });
  }

  toggleShowHidden(): void {
    this.showHidden = !this.showHidden;
    if (this.showHidden) {
      this.materialService.getHidden().subscribe({
        next: (data) => (this.hiddenMaterials = data)
      });
    }
  }

  unhideMaterial(id: number): void {
    this.materialService.unhide(id).subscribe({
      next: () => {
        this.hiddenMaterials = this.hiddenMaterials.filter(m => m.id !== id);
        this.loadMaterials();
      }
    });
  }
}
