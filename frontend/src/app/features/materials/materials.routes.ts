import { Routes } from '@angular/router';

export const MATERIAL_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./material-list/material-list.component').then(m => m.MaterialListComponent)
  },
  {
    path: 'new',
    loadComponent: () => import('./material-form/material-form.component').then(m => m.MaterialFormComponent)
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./material-form/material-form.component').then(m => m.MaterialFormComponent)
  }
];
