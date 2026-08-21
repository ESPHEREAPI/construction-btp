import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { StockService } from '../../../core/services/stock.service';
import { Stock } from '../../../core/models/stock.model';
import { AuthService } from '../../../core/services/auth.service';


@Component({
  selector: 'app-stock-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './stock-list.component.html',
  styleUrls: ['./stock-list.component.scss']
})
export class StockListComponent implements OnInit {
  stocks: Stock[] = [];
  loading = false;
  error = '';
  
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  
  filterProjectId: number | null = null;
  showLowStockOnly = false;

  constructor(private stockService: StockService, private authService: AuthService) {}

  get canUpdate(): boolean {
    return this.authService.hasPermission('STOCK_UPDATE');
  }

  ngOnInit(): void {
    this.loadStocks();
  }

  loadStocks(): void {
    this.loading = true;
    this.error = '';
    
    if (this.showLowStockOnly) {
      this.stockService.getLowStockAlerts(this.filterProjectId || undefined).subscribe({
        next: (data) => {
          this.stocks = data;
          this.totalElements = data.length;
          this.totalPages = 1;
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Erreur lors du chargement des stocks';
          this.loading = false;
          console.error(err);
        }
      });
    } else {
      this.stockService.getAll(
        this.filterProjectId || undefined,
        this.currentPage,
        this.pageSize
      ).subscribe({
        next: (response) => {
          this.stocks = response.content;
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Erreur lors du chargement des stocks';
          this.loading = false;
          console.error(err);
        }
      });
    }
  }

  applyFilters(): void {
    this.currentPage = 0;
    this.loadStocks();
  }

  clearFilters(): void {
    this.filterProjectId = null;
    this.showLowStockOnly = false;
    this.currentPage = 0;
    this.loadStocks();
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadStocks();
  }
}
