import { Component, inject } from '@angular/core';
import { AsyncPipe, NgFor, NgIf, DecimalPipe } from '@angular/common';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { FormsModule } from '@angular/forms';
import { AccountActions } from '../../state/account/account.actions';
import {
  selectAllAccounts,
  selectAccountLoading,
  selectAccountError,
  selectAccountActionLoading
} from '../../state/account/account.selectors';
import { selectSelectedUser, selectSelectedUserId } from '../../state/user/user.selectors';
import { Currency } from '../../core/models/bank.models';

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [AsyncPipe, NgFor, NgIf, DecimalPipe, FormsModule],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.scss'
})
export class HomePageComponent {
  private store = inject(Store);
  private router = inject(Router);

  selectedUser$ = this.store.select(selectSelectedUser);
  selectedUserId$ = this.store.select(selectSelectedUserId);
  accounts$ = this.store.select(selectAllAccounts);
  loading$ = this.store.select(selectAccountLoading);
  actionLoading$ = this.store.select(selectAccountActionLoading);
  error$ = this.store.select(selectAccountError);

  newAccountCurrency: Currency = 'USD';
  currencies: Currency[] = ['USD', 'EUR', 'SEK', 'GBP', 'VND'];

  onAccountClick(accountId: number) {
    this.router.navigate(['/account', accountId]);
  }

  onCreateAccount(userId: number) {
    this.store.dispatch(AccountActions.createAccount({ userId, currency: this.newAccountCurrency }));
  }

  getCurrencySymbol(currency: string): string {
    switch (currency) {
      case 'EUR': return '€';
      case 'GBP': return '£';
      case 'SEK': return 'kr';
      case 'VND': return '₫';
      default: return '$';
    }
  }

  getCurrencyName(currency: string): string {
    switch (currency) {
      case 'EUR': return 'Euro';
      case 'GBP': return 'British Pound';
      case 'SEK': return 'Swedish Krona';
      case 'VND': return 'Vietnamese Dong';
      default: return 'US Dollar';
    }
  }
}
