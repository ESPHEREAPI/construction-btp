import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { MaterialService } from '../../../core/services/material.service';
import { Material } from '../../../core/models/material.model';

@Component({
  selector: 'app-material-list',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule],
  templateUrl: './material-list.component.html',
  styleUrls: ['./material-list.component.scss']
})
export class MaterialListComponent implements OnInit {
  materials: Material[] = [];
  loading = true;

  constructor(private materialService: MaterialService) {}

  ngOnInit(): void {
    this.loadMaterials();
  }

  loadMaterials(): void {
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

  deleteMaterial(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce matériau ?')) {
      this.materialService.delete(id).subscribe({
        next: () => {
          this.loadMaterials();
        }
      });
    }
  }
}
