import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { Order, ORDER_STATUS_COLORS, ORDER_STATUS_LABELS } from '../../../core/models/order.model';
import { OrderService } from '../../../core/services/order.service';
import { AuthService } from '../../../core/services/auth.service';
import { AppCurrencyPipe } from '../../../shared/pipes/app-currency.pipe';


@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, AppCurrencyPipe],
  templateUrl: './order-detail.component.html',
  styleUrls: ['./order-detail.component.scss']
})
export class OrderDetailComponent implements OnInit {
  order: Order | null = null;
  loading = false;
  error = '';
  
  statusLabels = ORDER_STATUS_LABELS;
  statusColors = ORDER_STATUS_COLORS;

  constructor(
    private orderService: OrderService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  get canApprove(): boolean {
    return this.authService.hasPermission('ORDER_APPROVE');
  }

  get canUpdate(): boolean {
    return this.authService.hasPermission('ORDER_UPDATE');
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadOrder(+id);
    }
  }

  loadOrder(id: number): void {
    this.loading = true;
    this.orderService.getById(id).subscribe({
      next: (data) => {
        this.order = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement';
        this.loading = false;
        console.error(err);
      }
    });
  }

  approveOrder(): void {
    if (this.order && confirm('Approuver cette commande ?')) {
      this.orderService.approve(this.order.id).subscribe({
        next: () => this.loadOrder(this.order!.id),
        error: (err) => {
          alert('Erreur lors de l approbation');
          console.error(err);
        }
      });
    }
  }

  receiveOrder(): void {
    if (this.order && confirm('Marquer cette commande comme reçue ?')) {
      this.orderService.receive(this.order.id).subscribe({
        next: () => this.loadOrder(this.order!.id),
        error: (err) => {
          alert('Erreur lors de la réception');
          console.error(err);
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/orders']);
  }
}
