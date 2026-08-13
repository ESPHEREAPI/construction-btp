import { Routes } from '@angular/router';
import { MainLayoutComponent } from './main-layout.component';

export const MAIN_LAYOUT_ROUTES: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('../../features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'projects',
        loadChildren: () => import('../../features/projects/projects.routes').then(m => m.PROJECT_ROUTES)
      },
      {
        path: 'materials',
        loadChildren: () => import('../../features/materials/materials.routes').then(m => m.MATERIAL_ROUTES)
      }
    ]
  }
];
