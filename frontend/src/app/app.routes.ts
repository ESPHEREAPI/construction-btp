import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  // Routes WITHOUT layout (Auth pages)
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component')
      .then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component')
      .then(m => m.RegisterComponent)
  },
  {
    path: 'change-password',
    canActivate: [AuthGuard],
    loadComponent: () => import('./features/auth/change-password/change-password.component')
      .then(m => m.ChangePasswordComponent)
  },

  // Routes WITH layout (Protected pages)
  {
    path: '',
    loadComponent: () => import('./layouts/main-layout/main-layout.component')
      .then(m => m.MainLayoutComponent),
    canActivate: [AuthGuard],
    children: [
      // Redirect root to dashboard
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },

      // Dashboard
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component')
          .then(m => m.DashboardComponent)
      },

      // My License (company self-service)
      {
        path: 'my-license',
        loadComponent: () => import('./features/my-license/my-license.component')
          .then(m => m.MyLicenseComponent)
      },

      // Projects
      {
        path: 'projects',
        loadComponent: () => import('./features/projects/project-list/project-list.component')
          .then(m => m.ProjectListComponent)
      },
      {
        path: 'projects/new',
        loadComponent: () => import('./features/projects/project-form/project-form.component')
          .then(m => m.ProjectFormComponent)
      },
      {
        path: 'projects/:id',
        loadComponent: () => import('./features/projects/project-detail/project-detail.component')
          .then(m => m.ProjectDetailComponent)
      },
      {
        path: 'projects/:id/edit',
        loadComponent: () => import('./features/projects/project-form/project-form.component')
          .then(m => m.ProjectFormComponent)
      },

      // Materials
      {
        path: 'materials',
        loadComponent: () => import('./features/materials/material-list/material-list.component')
          .then(m => m.MaterialListComponent)
      },
      {
        path: 'materials/new',
        loadComponent: () => import('./features/materials/material-form/material-form.component')
          .then(m => m.MaterialFormComponent)
      },
      {
        path: 'materials/:id',
        loadComponent: () => import('./features/materials/material-list/material-list.component')
          .then(m => m.MaterialListComponent)
      },
      {
        path: 'materials/:id/edit',
        loadComponent: () => import('./features/materials/material-form/material-form.component')
          .then(m => m.MaterialFormComponent)
      },

      // Orders
      {
        path: 'orders',
        loadComponent: () => import('./features/orders/order-list/order-list.component')
          .then(m => m.OrderListComponent)
      },
      {
        path: 'orders/new',
        loadComponent: () => import('./features/orders/order-form/order-form.component')
          .then(m => m.OrderFormComponent)
      },
      {
        path: 'orders/:id',
        loadComponent: () => import('./features/orders/order-detail/order-detail.component')
          .then(m => m.OrderDetailComponent)
      },
      {
        path: 'orders/:id/edit',
        loadComponent: () => import('./features/orders/order-form/order-form.component')
          .then(m => m.OrderFormComponent)
      },

      // Usage
      {
        path: 'usages',
        loadComponent: () => import('./features/usage/usage-list/usage-list.component')
          .then(m => m.UsageListComponent)
      },
      {
        path: 'usages/new',
        loadComponent: () => import('./features/usage/usage-form/usage-form.component')
          .then(m => m.UsageFormComponent)
      },
      {
        path: 'usages/:id/edit',
        loadComponent: () => import('./features/usage/usage-form/usage-form.component')
          .then(m => m.UsageFormComponent)
      },

      // Stock
      {
        path: 'stocks',
        loadComponent: () => import('./features/stock/stock-list/stock-list.component')
          .then(m => m.StockListComponent)
      },
      {
        path: 'stocks/movement',
        loadComponent: () => import('./features/stock/stock-movement/stock-movement.component')
          .then(m => m.StockMovementComponent)
      },
      {
        path: 'stocks/:id',
        loadComponent: () => import('./features/stock/stock-list/stock-list.component')
          .then(m => m.StockListComponent)
      },
      {
        path: 'stocks/:id/movements',
        loadComponent: () => import('./features/stock/stock-movement/stock-movement.component')
          .then(m => m.StockMovementComponent)
      },

      // Admin
      {
        path: 'admin/users',
        loadComponent: () => import('./features/admin/user-list/user-list.component')
          .then(m => m.UserListComponent)
      },
      {
        path: 'admin/users/new',
        loadComponent: () => import('./features/admin/user-form/user-form.component')
          .then(m => m.UserFormComponent)
      },
      {
        path: 'admin/users/:id',
        loadComponent: () => import('./features/admin/user-list/user-list.component')
          .then(m => m.UserListComponent)
      },
      {
        path: 'admin/users/:id/edit',
        loadComponent: () => import('./features/admin/user-form/user-form.component')
          .then(m => m.UserFormComponent)
      },
      {
        path: 'admin/roles',
        loadComponent: () => import('./features/admin/role-list/role-list.component')
          .then(m => m.RoleListComponent)
      },

      // Super Admin
      {
        path: 'admin/companies',
        canActivate: [roleGuard],
        data: { roles: ['ROLE_SUPER_ADMIN'] },
        loadComponent: () => import('./features/super-admin/company-list/company-list.component')
          .then(m => m.CompanyListComponent)
      },
      {
        path: 'admin/licenses',
        canActivate: [roleGuard],
        data: { roles: ['ROLE_SUPER_ADMIN'] },
        loadComponent: () => import('./features/super-admin/license-config/license-config.component')
          .then(m => m.LicenseConfigComponent)
      }
    ]
  },

  // 404 - Not Found
  {
    path: '**',
    redirectTo: '/login'
  }
];