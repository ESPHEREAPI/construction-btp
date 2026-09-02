import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { UsageService } from '../../../core/services/usage.service';
import { ProjectService } from '../../../core/services/project.service';
import { StockService } from '../../../core/services/stock.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-usage-form',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, TranslateModule],
  templateUrl: './usage-form.component.html',
  styleUrls: ['./usage-form.component.scss']
})
export class UsageFormComponent implements OnInit {
  usage: any = {
    projectId: null,
    materialId: null,
    quantity: null,
    usageDate: new Date().toISOString().split('T')[0],
    usedBy: '',
    location: '',
    purpose: '',
    notes: ''
  };

  projects: any[] = [];
  availableStocks: any[] = [];
  selectedStock: any = null;

  isEditMode = false;
  loading = false;
  loadingData = true;
  error = '';
  successMessage = '';
  maxDate: string;

  constructor(
    private usageService: UsageService,
    private projectService: ProjectService,
    private stockService: StockService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.maxDate = new Date().toISOString().split('T')[0];
  }

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects(): void {
    this.loadingData = true;
    const id = this.route.snapshot.paramMap.get('id');

    this.projectService.getAll().subscribe({
      next: (data: any) => {
        this.projects = data.content || data || [];
        this.loadingData = false;

        if (id) {
          this.isEditMode = true;
          this.loadUsage(+id);
        }
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des projets';
        this.loadingData = false;
        console.error(err);
      }
    });
  }

  loadUsage(id: number): void {
    this.loading = true;
    this.usageService.getById(id).subscribe({
      next: (data) => {
        this.usage = {
          ...data,
          usageDate: this.formatDate(data.usageDate)
        };
        if (this.usage.projectId) {
          this.onProjectChange();
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement';
        this.loading = false;
        console.error(err);
      }
    });
  }

  formatDate(date: any): string {
    if (!date) return '';
    const d = new Date(date);
    return d.toISOString().split('T')[0];
  }

  onProjectChange(): void {
    this.usage.materialId = null;
    this.selectedStock = null;
    this.availableStocks = [];
    
    if (this.usage.projectId) {
      this.loadStocksByProject(this.usage.projectId);
    }
  }

  loadStocksByProject(projectId: number): void {
    this.loading = true;
    this.stockService.getByProject(projectId).subscribe({
      next: (response) => {
        this.availableStocks = (response.content || response || [])
          .filter((stock: any) => stock.currentQuantity > 0);
        this.loading = false;
        console.log(`✅ ${this.availableStocks.length} matériau(x) disponible(s) pour ce projet`);
      },
      error: (err) => {
        console.error('Erreur chargement stock:', err);
        this.availableStocks = [];
        this.loading = false;
      }
    });
  }

  onMaterialChange(): void {
    if (this.usage.materialId) {
      this.selectedStock = this.availableStocks.find(s => s.materialId === this.usage.materialId);
    } else {
      this.selectedStock = null;
    }
  }

  getStockLabel(stock: any): string {
    return `${stock.materialName} - Stock: ${stock.currentQuantity} ${stock.materialUnit}`;
  }

  isStockSufficient(): boolean {
    if (!this.usage.quantity || !this.selectedStock) return true;
    const available = this.selectedStock.currentQuantity - (this.selectedStock.reservedQuantity || 0);
    return this.usage.quantity <= available;
  }

  getMaxAvailable(): number {
    if (!this.selectedStock) return 0;
    return this.selectedStock.currentQuantity - (this.selectedStock.reservedQuantity || 0);
  }

  onSubmit(): void {
    if (!this.validateForm()) return;

    this.loading = true;
    this.error = '';
    this.successMessage = '';

    const request = this.isEditMode
      ? this.usageService.update(this.usage.id, this.usage)
      : this.usageService.create(this.usage);

    request.subscribe({
      next: () => {
        this.successMessage = this.isEditMode 
          ? 'Utilisation modifiée avec succès !' 
          : 'Utilisation enregistrée avec succès !';
        
        setTimeout(() => {
          this.router.navigate(['/usages']);
        }, 1500);
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de l\'enregistrement';
        this.loading = false;
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    });
  }

  validateForm(): boolean {
    if (!this.usage.projectId) {
      this.error = 'Veuillez sélectionner un projet';
      return false;
    }
    if (!this.usage.materialId) {
      this.error = 'Veuillez sélectionner un matériau';
      return false;
    }
    if (!this.usage.quantity || this.usage.quantity <= 0) {
      this.error = 'Veuillez saisir une quantité valide';
      return false;
    }
    if (!this.isStockSufficient()) {
      this.error = `Stock insuffisant. Maximum disponible: ${this.getMaxAvailable()}`;
      return false;
    }
    if (!this.usage.usageDate) {
      this.error = 'Veuillez saisir une date d\'utilisation';
      return false;
    }
    return true;
  }

  resetForm(): void {
    if (confirm('Êtes-vous sûr de vouloir réinitialiser le formulaire ?')) {
      this.usage = {
        projectId: null,
        materialId: null,
        quantity: null,
        usageDate: new Date().toISOString().split('T')[0],
        usedBy: '',
        location: '',
        purpose: '',
        notes: ''
      };
      this.selectedStock = null;
      this.availableStocks = [];
      this.error = '';
      this.successMessage = '';
    }
  }

  onCancel(): void {
    if (confirm('Êtes-vous sûr de vouloir annuler ?')) {
      this.router.navigate(['/usages']);
    }
  }
}
