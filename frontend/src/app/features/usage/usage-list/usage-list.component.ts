import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { Usage } from '../../../core/models/usage.model';
import { UsageService } from '../../../core/services/usage.service';
import { AuthService } from '../../../core/services/auth.service';


@Component({
  selector: 'app-usage-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, TranslateModule],
  templateUrl: './usage-list.component.html',
  styleUrls: ['./usage-list.component.scss']
})
export class UsageListComponent implements OnInit {
  usages: Usage[] = [];
  loading = false;
  error = '';
  
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  
  filterProjectId: number | null = null;
  filterMaterialId: number | null = null;

  constructor(private usageService: UsageService, private authService: AuthService) {}

  get canCreate(): boolean {
    return this.authService.hasPermission('USAGE_CREATE');
  }

  get canUpdate(): boolean {
    return this.authService.hasPermission('USAGE_UPDATE');
  }

  get canDelete(): boolean {
    return this.authService.hasPermission('USAGE_DELETE');
  }

  ngOnInit(): void {
    this.loadUsages();
  }

  loadUsages(): void {
    this.loading = true;
    this.error = '';
    
    this.usageService.getAll(
      this.filterProjectId || undefined,
      this.filterMaterialId || undefined,
      this.currentPage,
      this.pageSize
    ).subscribe({
      next: (response) => {
        this.usages = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des utilisations';
        this.loading = false;
        console.error(err);
      }
    });
  }

  applyFilters(): void {
    this.currentPage = 0;
    this.loadUsages();
  }

  clearFilters(): void {
    this.filterProjectId = null;
    this.filterMaterialId = null;
    this.currentPage = 0;
    this.loadUsages();
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadUsages();
  }

  deleteUsage(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette utilisation ?')) {
      this.usageService.delete(id).subscribe({
        next: () => this.loadUsages(),
        error: (err) => {
          alert('Erreur lors de la suppression');
          console.error(err);
        }
      });
    }
  }
}
