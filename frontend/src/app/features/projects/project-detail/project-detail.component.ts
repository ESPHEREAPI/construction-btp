import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ProjectService } from '../../../core/services/project.service';
import { AuthService } from '../../../core/services/auth.service';
import { OrderService } from '../../../core/services/order.service';
import { UsageService } from '../../../core/services/usage.service';
import { StockService } from '../../../core/services/stock.service';
import { Project } from '../../../core/models/project.model';
import { Order, ORDER_STATUS_LABELS, ORDER_STATUS_COLORS } from '../../../core/models/order.model';
import { Usage } from '../../../core/models/usage.model';
import { ProjectActivity } from '../../../core/models/project-activity.model';

@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule],
  templateUrl: './project-detail.component.html',
  styleUrls: ['./project-detail.component.scss']
})
export class ProjectDetailComponent implements OnInit {
  project?: Project;
  loading = true;

  stocks: any[] = [];
  loadingStocks = true;
  orders: Order[] = [];
  loadingOrders = true;
  usages: Usage[] = [];
  loadingUsages = true;
  activities: ProjectActivity[] = [];
  loadingActivities = true;

  readonly orderStatusLabels = ORDER_STATUS_LABELS;
  readonly orderStatusColors = ORDER_STATUS_COLORS;

  constructor(
    private projectService: ProjectService,
    private authService: AuthService,
    private orderService: OrderService,
    private usageService: UsageService,
    private stockService: StockService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  /** Permission-based, not a hardcoded role list - see project-list.component.ts for why. */
  get canEdit(): boolean {
    return this.authService.hasPermission('PROJECT_UPDATE');
  }

  get canDelete(): boolean {
    return this.authService.hasPermission('PROJECT_DELETE');
  }

  get canManageProjects(): boolean {
    return this.canEdit || this.canDelete;
  }

  /** Pure, computed from already-fetched data - no HTTP call. */
  get conclusions(): { key: string; params: any }[] {
    const list: { key: string; params: any }[] = [];
    if (!this.project) {
      return list;
    }

    if (this.project.budget != null && this.project.spentAmount != null && this.project.budget > 0
        && this.project.spentAmount > this.project.budget) {
      const pct = Math.round((this.project.spentAmount / this.project.budget) * 100) - 100;
      list.push({ key: 'projects.conclusion.budgetExceeded', params: { pct } });
    }

    if (this.project.staleOrdersCount) {
      list.push({ key: 'projects.conclusion.staleOrders', params: { count: this.project.staleOrdersCount } });
    }

    const lowStockCount = this.stocks.filter((s: any) => s.lowStockAlert).length;
    if (lowStockCount > 0) {
      list.push({ key: 'projects.conclusion.lowStock', params: { count: lowStockCount } });
    }

    if (list.length === 0) {
      list.push({ key: 'projects.conclusion.onTrack', params: {} });
    }

    return list;
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        const id = +params['id'];
        this.loadProject(id);
        this.loadStocks(id);
        this.loadOrders(id);
        this.loadUsages(id);
        this.loadActivity(id);
      }
    });
  }

  loadProject(id: number): void {
    this.projectService.getById(id).subscribe({
      next: (data) => {
        this.project = data;
        this.loading = false;
      },
      error: () => this.router.navigate(['/projects'])
    });
  }

  loadStocks(projectId: number): void {
    this.loadingStocks = true;
    this.stockService.getByProject(projectId).subscribe({
      next: (data) => {
        this.stocks = data.content ?? data;
        this.loadingStocks = false;
      },
      error: () => (this.loadingStocks = false)
    });
  }

  loadOrders(projectId: number): void {
    this.loadingOrders = true;
    this.orderService.getAll(projectId, undefined, 0, 10).subscribe({
      next: (data) => {
        this.orders = data.content ?? data;
        this.loadingOrders = false;
      },
      error: () => (this.loadingOrders = false)
    });
  }

  loadUsages(projectId: number): void {
    this.loadingUsages = true;
    this.usageService.getAll(projectId, undefined, 0, 10).subscribe({
      next: (data) => {
        this.usages = data.content ?? data;
        this.loadingUsages = false;
      },
      error: () => (this.loadingUsages = false)
    });
  }

  loadActivity(projectId: number): void {
    this.loadingActivities = true;
    this.projectService.getActivity(projectId, 0, 10).subscribe({
      next: (data) => {
        this.activities = data.content ?? data;
        this.loadingActivities = false;
      },
      error: () => (this.loadingActivities = false)
    });
  }

  deleteProject(): void {
    if (confirm('Êtes-vous sûr ?') && this.project?.id) {
      this.projectService.delete(this.project.id).subscribe({
        next: () => this.router.navigate(['/projects'])
      });
    }
  }
}
