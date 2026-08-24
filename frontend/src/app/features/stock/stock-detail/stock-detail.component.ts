import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Stock } from '../../../core/models/stock.model';
import { StockService } from '../../../core/services/stock.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-stock-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './stock-detail.component.html',
  styleUrls: ['./stock-detail.component.scss']
})
export class StockDetailComponent implements OnInit {
  stock: Stock | null = null;
  loading = false;
  error = '';

  editingThresholds = false;
  thresholds: { minimumQuantity: number | null; maximumQuantity: number | null } = {
    minimumQuantity: null,
    maximumQuantity: null
  };
  savingThresholds = false;

  constructor(
    private stockService: StockService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  get canUpdate(): boolean {
    return this.authService.hasPermission('STOCK_UPDATE');
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadStock(+id);
    }
  }

  loadStock(id: number): void {
    this.loading = true;
    this.stockService.getById(id).subscribe({
      next: (data) => {
        this.stock = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement du stock';
        this.loading = false;
        console.error(err);
      }
    });
  }

  startEditThresholds(): void {
    if (!this.stock) return;
    this.thresholds = {
      minimumQuantity: this.stock.minimumQuantity ?? null,
      maximumQuantity: this.stock.maximumQuantity ?? null
    };
    this.editingThresholds = true;
  }

  cancelEditThresholds(): void {
    this.editingThresholds = false;
  }

  saveThresholds(): void {
    if (!this.stock) return;
    this.savingThresholds = true;
    this.stockService.updateThresholds(this.stock.id, this.thresholds).subscribe({
      next: (data) => {
        this.stock = data;
        this.editingThresholds = false;
        this.savingThresholds = false;
      },
      error: (err) => {
        alert(err?.error?.message || 'Erreur lors de la mise à jour des seuils');
        this.savingThresholds = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/stocks']);
  }
}
