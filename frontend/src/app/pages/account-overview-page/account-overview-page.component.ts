import { Component, OnInit, OnDestroy, AfterViewInit, ViewChild, ElementRef, inject } from '@angular/core';
import { AsyncPipe, NgFor, NgIf, DecimalPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { FormsModule } from '@angular/forms';
import { Subscription, combineLatest } from 'rxjs';
import { filter, take } from 'rxjs/operators';
import { Chart, registerables } from 'chart.js';

import { AccountActions } from '../../state/account/account.actions';
import { TransactionActions } from '../../state/transaction/transaction.actions';
import {
  selectSelectedAccount,
  selectAccountLoading,
  selectAccountActionLoading,
  selectAccountError,
  selectAllAccounts
} from '../../state/account/account.selectors';
import {
  selectAllTransactions,
  selectTransactionLoading,
  selectTransactionPageInfo
} from '../../state/transaction/transaction.selectors';
import { Currency } from '../../core/models/bank.models';

Chart.register(...registerables);

@Component({
  selector: 'app-account-overview-page',
  standalone: true,
  imports: [AsyncPipe, NgFor, NgIf, DecimalPipe, DatePipe, FormsModule, RouterLink],
  templateUrl: './account-overview-page.component.html',
  styleUrl: './account-overview-page.component.scss'
})
export class AccountOverviewPageComponent implements OnInit, OnDestroy, AfterViewInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private store = inject(Store);

  @ViewChild('chartCanvas') chartCanvas!: ElementRef<HTMLCanvasElement>;

  private sentinelElement: ElementRef<HTMLDivElement> | null = null;
  private observer: IntersectionObserver | null = null;

  @ViewChild('infiniteScrollSentinel', { static: false }) set sentinel(content: ElementRef<HTMLDivElement>) {
    if (content) {
      this.sentinelElement = content;
      this.setupIntersectionObserver();
    }
  }

  account$ = this.store.select(selectSelectedAccount);
  accountLoading$ = this.store.select(selectAccountLoading);
  actionLoading$ = this.store.select(selectAccountActionLoading);
  error$ = this.store.select(selectAccountError);

  allAccounts$ = this.store.select(selectAllAccounts);
  transactions$ = this.store.select(selectAllTransactions);
  transactionsLoading$ = this.store.select(selectTransactionLoading);
  pageInfo$ = this.store.select(selectTransactionPageInfo);

  accountId!: number;
  private subs = new Subscription();
  private chart: Chart | null = null;

  // Modal display states
  activeModal: 'credit' | 'debit' | 'exchange' | null = null;

  // Form inputs
  transactionAmount: number | null = null;
  exchangeTargetAccountId: number | null = null;

  ngOnInit() {
    this.subs.add(
      this.route.params.subscribe(params => {
        this.accountId = Number(params['id']);
        if (!isNaN(this.accountId)) {
          this.store.dispatch(AccountActions.loadAccountDetail({ accountId: this.accountId }));
          this.store.dispatch(TransactionActions.loadTransactions({ accountId: this.accountId, page: 0, size: 20 }));
        }
      })
    );
  }

  ngAfterViewInit() {
    // Reconstruct and update the chart whenever transactions change
    this.subs.add(
      combineLatest([this.transactions$, this.account$])
        .pipe(filter(([txs, acc]) => txs.length > 0 && acc !== null))
        .subscribe(([txs, acc]) => {
          this.updateChart(txs.slice().reverse(), acc!.currency); // Reverse to chronological order (oldest to newest)
        })
    );
  }

  private setupIntersectionObserver() {
    if (this.observer) {
      this.observer.disconnect();
    }

    if (!this.sentinelElement) return;

    this.observer = new IntersectionObserver((entries) => {
      const entry = entries[0];
      if (entry.isIntersecting) {
        this.onSentinelIntersect();
      }
    }, {
      root: null, // viewport
      rootMargin: '100px', // start loading before reaching bottom
      threshold: 0.1
    });

    this.observer.observe(this.sentinelElement.nativeElement);
  }

  private onSentinelIntersect() {
    combineLatest([this.pageInfo$, this.transactionsLoading$])
      .pipe(take(1))
      .subscribe(([page, loading]) => {
        if (!loading && page && !page.last) {
          this.loadMoreTransactions(page.page, page.last);
        }
      });
  }

  ngOnDestroy() {
    this.subs.unsubscribe();
    if (this.chart) {
      this.chart.destroy();
    }
    if (this.observer) {
      this.observer.disconnect();
    }
    this.store.dispatch(AccountActions.clearError());
  }

  loadMoreTransactions(currentPage: number, lastPage: boolean) {
    if (!lastPage) {
      this.store.dispatch(
        TransactionActions.loadTransactions({
          accountId: this.accountId,
          page: currentPage + 1,
          size: 20
        })
      );
    }
  }

  openModal(type: 'credit' | 'debit' | 'exchange') {
    this.activeModal = type;
    this.transactionAmount = null;
    this.exchangeTargetAccountId = null;
    this.store.dispatch(AccountActions.clearError());
  }

  closeModal() {
    this.activeModal = null;
    this.store.dispatch(AccountActions.clearError());
  }

  onSubmitTransaction() {
    if (!this.transactionAmount || this.transactionAmount <= 0) return;

    if (this.activeModal === 'credit') {
      this.store.dispatch(
        AccountActions.creditAccount({
          accountId: this.accountId,
          amount: this.transactionAmount
        })
      );
    } else if (this.activeModal === 'debit') {
      this.store.dispatch(
        AccountActions.debitAccount({
          accountId: this.accountId,
          amount: this.transactionAmount
        })
      );
    } else if (this.activeModal === 'exchange') {
      if (!this.exchangeTargetAccountId) return;
      this.store.dispatch(
        AccountActions.exchangeFunds({
          sourceAccountId: this.accountId,
          targetAccountId: this.exchangeTargetAccountId,
          amount: this.transactionAmount
        })
      );
    }

    // Close the modal once the dispatched action completes successfully.
    // `combineLatest` gives us a single emission stream so we don't accumulate
    // nested subscriptions every time the user submits.
    this.subs.add(
      combineLatest([this.actionLoading$, this.error$])
        .pipe(
          filter(([loading, _err]) => !loading),
          take(1)
        )
        .subscribe(([_loading, err]) => {
          if (!err && this.activeModal) {
            this.closeModal();
          }
        })
    );
  }

  onTransactionClick(transactionId: number) {
    this.router.navigate(['/transaction', transactionId]);
  }

  updateChart(data: any[], currency: string) {
    if (!this.chartCanvas) return;

    const ctx = this.chartCanvas.nativeElement.getContext('2d');
    if (!ctx) return;

    if (this.chart) {
      this.chart.destroy();
    }

    // Reconstruct dates and balance sequence
    const labels = data.map(tx => {
      const date = new Date(tx.timestamp);
      return date.toLocaleDateString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    });
    const balances = data.map(tx => tx.balanceAfter);

    this.chart = new Chart(ctx, {
      type: 'line',
      data: {
        labels,
        datasets: [{
          label: `Balance (${currency})`,
          data: balances,
          borderColor: '#6366f1',
          backgroundColor: 'rgba(99, 102, 241, 0.1)',
          fill: true,
          tension: 0.3,
          borderWidth: 2,
          pointBackgroundColor: '#a855f7',
          pointBorderColor: '#fff',
          pointHoverRadius: 6
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false }
        },
        scales: {
          x: {
            grid: { color: 'rgba(255, 255, 255, 0.05)' },
            ticks: { color: '#94a3b8', font: { family: 'Plus Jakarta Sans', size: 10 } }
          },
          y: {
            grid: { color: 'rgba(255, 255, 255, 0.05)' },
            ticks: { color: '#94a3b8', font: { family: 'Space Grotesk', size: 10 } }
          }
        }
      }
    });
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
