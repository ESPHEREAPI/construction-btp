import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { MOVEMENT_TYPE_LABELS, MovementType } from '../../../core/models/stock.model';
import { StockService } from '../../../core/services/stock.service';
import { ProjectService } from '../../../core/services/project.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-stock-movement',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, TranslateModule],
  templateUrl: './stock-movement.component.html',
  styleUrls: ['./stock-movement.component.scss']
})
export class StockMovementComponent implements OnInit {
  movement: any = {
    stockId: null,
    destinationStockId: null,
    movementType: 'IN',
    quantity: null,
    reference: '',
    reason: '',
    performedBy: '',
    notes: ''
  };

  projects: any[] = [];
  allStocks: any[] = [];
  sourceStocks: any[] = [];
  destinationStocks: any[] = [];
  selectedProject: number | null = null;
  selectedStock: any = null;
  destinationProject: number | null = null;

  loading = false;
  loadingData = true;
  error = '';
  successMessage = '';

  movementTypeLabels = MOVEMENT_TYPE_LABELS;
  movementTypes = Object.keys(MOVEMENT_TYPE_LABELS) as MovementType[];

  constructor(
    private stockService: StockService,
    private projectService: ProjectService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadInitialData();
  }

  loadInitialData(): void {
    this.loadingData = true;

    forkJoin({
      projects: this.projectService.getAll(),
      stocks: this.stockService.getAll()
    }).subscribe({
      next: (data: any) => {
        this.projects = data.projects.content || data.projects || [];
        this.allStocks = data.stocks.content || data.stocks || [];
        this.loadingData = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement';
        this.loadingData = false;
        console.error(err);
      }
    });
  }

  onMovementTypeChange(): void {
    this.movement.stockId = null;
    this.movement.destinationStockId = null;
    this.selectedProject = null;
    this.destinationProject = null;
    this.selectedStock = null;
    this.sourceStocks = [];
    this.destinationStocks = [];
  }

  onProjectChange(): void {
    this.movement.stockId = null;
    this.selectedStock = null;

    if (this.selectedProject) {
      this.sourceStocks = this.allStocks.filter(
        s => s.projectId === this.selectedProject && s.currentQuantity > 0
      );
    } else {
      this.sourceStocks = this.allStocks.filter(s => s.currentQuantity > 0);
    }
  }

  onStockChange(): void {
    if (this.movement.stockId) {
      this.selectedStock = this.allStocks.find(s => s.id === this.movement.stockId);
      
      if (this.movement.movementType === 'TRANSFER' && this.selectedStock) {
        this.loadDestinationStocks();
      }
    } else {
      this.selectedStock = null;
    }
  }

  loadDestinationStocks(): void {
    if (!this.selectedStock) return;

    if (this.destinationProject) {
      this.destinationStocks = this.allStocks.filter(
        s => s.projectId === this.destinationProject && 
             s.materialId === this.selectedStock.materialId &&
             s.id !== this.movement.stockId
      );
    } else {
      this.destinationStocks = this.allStocks.filter(
        s => s.materialId === this.selectedStock.materialId &&
             s.id !== this.movement.stockId
      );
    }
  }

  onDestinationProjectChange(): void {
    this.movement.destinationStockId = null;
    this.loadDestinationStocks();
  }

  getStockLabel(stock: any): string {
    const project = this.projects.find(p => p.id === stock.projectId);
    return `Stock #${stock.id} - ${stock.materialName} (${stock.currentQuantity} ${stock.materialUnit}) - ${project?.code || ''}`;
  }

  getProjectName(projectId: number): string {
    const project = this.projects.find(p => p.id === projectId);
    return project ? `${project.code} - ${project.name}` : '';
  }

  isTransferType(): boolean {
    return this.movement.movementType === 'TRANSFER';
  }

  isQuantityValid(): boolean {
    if (!this.movement.quantity || !this.selectedStock) return true;
    
    if (this.movement.movementType === 'OUT' || this.movement.movementType === 'TRANSFER') {
      return this.movement.quantity <= this.selectedStock.currentQuantity;
    }
    
    return true;
  }

  validateForm(): boolean {
    if (!this.movement.stockId) {
      this.error = 'Veuillez sélectionner un stock source';
      return false;
    }
    if (this.isTransferType() && !this.movement.destinationStockId) {
      this.error = 'Veuillez sélectionner un stock de destination';
      return false;
    }
    if (!this.movement.movementType) {
      this.error = 'Veuillez sélectionner un type de mouvement';
      return false;
    }
    if (!this.movement.quantity || this.movement.quantity <= 0) {
      this.error = 'Veuillez saisir une quantité valide';
      return false;
    }
    if (!this.isQuantityValid()) {
      this.error = `Stock insuffisant. Maximum: ${this.selectedStock.currentQuantity}`;
      return false;
    }
    return true;
  }

  onSubmit(): void {
    if (!this.validateForm()) return;

    this.loading = true;
    this.error = '';
    this.successMessage = '';

    this.stockService.addMovement(this.movement).subscribe({
      next: () => {
        this.successMessage = 'Mouvement enregistré avec succès !';
        setTimeout(() => this.router.navigate(['/stocks']), 1500);
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de l\'enregistrement';
        this.loading = false;
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    });
  }

  resetForm(): void {
    if (confirm('Êtes-vous sûr de vouloir réinitialiser ?')) {
      this.movement = {
        stockId: null,
        destinationStockId: null,
        movementType: 'IN',
        quantity: null,
        reference: '',
        reason: '',
        performedBy: '',
        notes: ''
      };
      this.selectedProject = null;
      this.destinationProject = null;
      this.selectedStock = null;
      this.sourceStocks = [];
      this.destinationStocks = [];
      this.error = '';
      this.successMessage = '';
    }
  }

  onCancel(): void {
    if (confirm('Êtes-vous sûr de vouloir annuler ?')) {
      this.router.navigate(['/stocks']);
    }
  }
}
