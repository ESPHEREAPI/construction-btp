import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../../core/services/auth.service';
import { ActiveModulesService } from '../../../../core/services/active-modules.service';
import { CurrencyService } from '../../../../core/services/currency.service';
import { LicenseModule } from '../../../../core/models/license.model';

interface MenuItem {
  path: string;
  icon: string;
  label: string;
  /** Omitted = visible to everyone authenticated. */
  roles?: string[];
  /** Company-scoped module: hidden for the Super Admin, who belongs to no company. */
  companyScoped?: boolean;
  /** Gated by the company's licensed modules on top of role/companyScoped filtering. */
  module?: LicenseModule;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit {
  @Input() collapsed = false;

  /** Employé (ROLE_READ_ONLY) is scoped to Stock only - the other operational modules are for the remaining roles. */
  private readonly NON_EMPLOYEE_ROLES = [
    'ROLE_SUPER_ADMIN', 'ROLE_COMPANY_ADMIN', 'ROLE_ADMIN',
    'ROLE_PROJECT_MANAGER', 'ROLE_SITE_MANAGER', 'ROLE_INVENTORY_MANAGER'
  ];

  private allMenuItems: MenuItem[] = [
    { path: '/dashboard', icon: 'bi-grid', label: 'nav.dashboard' },
    { path: '/my-license', icon: 'bi-key', label: 'nav.myLicense', companyScoped: true },
    { path: '/projects', icon: 'bi-building', label: 'nav.projects', roles: this.NON_EMPLOYEE_ROLES, companyScoped: true, module: 'PROJECTS' },
    { path: '/materials', icon: 'bi-box', label: 'nav.materials', roles: this.NON_EMPLOYEE_ROLES, companyScoped: true, module: 'MATERIALS' },
    { path: '/orders', icon: 'bi-cart', label: 'nav.orders', roles: this.NON_EMPLOYEE_ROLES, companyScoped: true, module: 'ORDERS' },
    { path: '/usages', icon: 'bi-graph-up', label: 'nav.usage', roles: this.NON_EMPLOYEE_ROLES, companyScoped: true, module: 'USAGE' },
    { path: '/stocks', icon: 'bi-stack', label: 'nav.stock', companyScoped: true, module: 'STOCK' },
    { path: '/admin/users', icon: 'bi-gear', label: 'nav.admin', roles: ['ROLE_COMPANY_ADMIN', 'ROLE_ADMIN'], companyScoped: true, module: 'ADMIN' },
    { path: '/admin/roles', icon: 'bi-shield', label: 'nav.roles', roles: ['ROLE_COMPANY_ADMIN', 'ROLE_ADMIN'], companyScoped: true, module: 'ADMIN' },
    { path: '/settings', icon: 'bi-gear', label: 'nav.settings', roles: ['ROLE_COMPANY_ADMIN', 'ROLE_ADMIN'], companyScoped: true },
  ];

  private superAdminMenuItems: MenuItem[] = [
    { path: '/admin/companies', icon: 'bi-buildings', label: 'nav.companies', roles: ['ROLE_SUPER_ADMIN'] },
    { path: '/admin/licenses', icon: 'bi-award', label: 'nav.licenses', roles: ['ROLE_SUPER_ADMIN'] },
  ];

  private activeModules: LicenseModule[] = [];

  constructor(
    private authService: AuthService,
    private activeModulesService: ActiveModulesService,
    private currencyService: CurrencyService
  ) {}

  ngOnInit(): void {
    if (!this.authService.isSuperAdmin()) {
      // Stay subscribed so any later refresh() call (e.g. after activating a
      // license key) updates the menu immediately, without requiring a re-login.
      this.activeModulesService.modules$.subscribe(modules => {
        this.activeModules = modules;
      });
      this.activeModulesService.refresh().subscribe();
      this.currencyService.refresh().subscribe();
    }
  }

  get menuItems(): MenuItem[] {
    const isSuperAdmin = this.authService.isSuperAdmin();
    return this.allMenuItems.filter(item => {
      if (item.companyScoped && isSuperAdmin) {
        return false;
      }
      if (item.module && !this.activeModules.includes(item.module)) {
        return false;
      }
      return !item.roles || this.authService.hasAnyRole(item.roles);
    });
  }

  get superAdminItems(): MenuItem[] {
    return this.superAdminMenuItems.filter(item => this.authService.hasAnyRole(item.roles!));
  }
}
