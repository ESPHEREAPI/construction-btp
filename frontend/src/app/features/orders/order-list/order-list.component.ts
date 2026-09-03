import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { Order, ORDER_STATUS_COLORS, ORDER_STATUS_LABELS } from '../../../core/models/order.model';
import { OrderService } from '../../../core/services/order.service';
import { AuthService } from '../../../core/services/auth.service';
import { AppCurrencyPipe } from '../../../shared/pipes/app-currency.pipe';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, AppCurrencyPipe, TranslateModule],
  templateUrl: './order-list.component.html',
  styleUrls: ['./order-list.component.scss']
})
export class OrderListComponent implements OnInit {
  orders: Order[] = [];
  loading = false;
  error = '';
  errorDetails: any = null;
  
  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  
  // Filters
  filterProjectId: number | null = null;
  filterStatus: string = '';
  
  // Labels
  statusLabels = ORDER_STATUS_LABELS;
  statusColors = ORDER_STATUS_COLORS;

  constructor(private orderService: OrderService, private authService: AuthService) {}

  get canCreate(): boolean {
    return this.authService.hasPermission('ORDER_CREATE');
  }

  get canApprove(): boolean {
    return this.authService.hasPermission('ORDER_APPROVE');
  }

  get canUpdate(): boolean {
    return this.authService.hasPermission('ORDER_UPDATE');
  }

  get canDelete(): boolean {
    return this.authService.hasPermission('ORDER_DELETE');
  }

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.error = '';
    this.errorDetails = null;
    
    this.orderService.getAll(
      this.filterProjectId || undefined,
      this.filterStatus || undefined,
      this.currentPage,
      this.pageSize
    ).subscribe({
      next: (response) => {
        console.log('✅ Commandes chargées avec succès:', response);
        this.orders = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: (err) => {
        console.error('❌ Erreur capturée dans le composant:', err);
        this.error = err.message || 'Erreur lors du chargement des commandes';
        this.errorDetails = err;
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    this.currentPage = 0;
    this.loadOrders();
  }

  clearFilters(): void {
    this.filterProjectId = null;
    this.filterStatus = '';
    this.currentPage = 0;
    this.loadOrders();
  }

  onPageChange(page: number): void {
    if (page < 0 || page >= this.totalPages) return;
    this.currentPage = page;
    this.loadOrders();
  }

  deleteOrder(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette commande ?')) {
      this.orderService.delete(id).subscribe({
        next: () => {
          console.log('✅ Commande supprimée avec succès');
          this.loadOrders();
        },
        error: (err) => {
          console.error('❌ Erreur lors de la suppression:', err);
          alert(err.message || 'Erreur lors de la suppression');
        }
      });
    }
  }

  approveOrder(id: number): void {
    if (confirm('Approuver cette commande ?')) {
      this.orderService.approve(id).subscribe({
        next: () => {
          console.log('✅ Commande approuvée avec succès');
          this.loadOrders();
        },
        error: (err) => {
          console.error('❌ Erreur lors de l\'approbation:', err);
          alert(err.message || 'Erreur lors de l\'approbation');
        }
      });
    }
  }

  receiveOrder(id: number): void {
    if (confirm('Marquer cette commande comme reçue ?')) {
      this.orderService.receive(id).subscribe({
        next: () => {
          console.log('✅ Commande réceptionnée avec succès');
          this.loadOrders();
        },
        error: (err) => {
          console.error('❌ Erreur lors de la réception:', err);
          alert(err.message || 'Erreur lors de la réception');
        }
      });
    }
  }

  retryLoad(): void {
    console.log('🔄 Nouvelle tentative de chargement...');
    this.loadOrders();
  }

  clearError(): void {
    this.error = '';
    this.errorDetails = null;
  }
}