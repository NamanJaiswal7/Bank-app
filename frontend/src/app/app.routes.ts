import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/home-page/home-page.component').then(m => m.HomePageComponent)
  },
  {
    path: 'account/:id',
    loadComponent: () => import('./pages/account-overview-page/account-overview-page.component').then(m => m.AccountOverviewPageComponent)
  },
  {
    path: 'transaction/:id',
    loadComponent: () => import('./pages/transaction-detail-page/transaction-detail-page.component').then(m => m.TransactionDetailPageComponent)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
