import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { ApiService } from '../../core/services/api.service';
import { TransactionActions } from './transaction.actions';
import { AccountActions } from '../account/account.actions';
import { toApiError } from '../../core/interceptors/error.interceptor';
import { catchError, map, mergeMap, of } from 'rxjs';

@Injectable()
export class TransactionEffects {
  private actions$ = inject(Actions);
  private apiService = inject(ApiService);

  loadTransactions$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TransactionActions.loadTransactions),
      mergeMap(({ accountId, page, size }) =>
        this.apiService.getTransactions(accountId, page, size).pipe(
          map((res) =>
            TransactionActions.loadTransactionsSuccess({
              transactions: res.content,
              page: res.page,
              size: res.size,
              totalElements: res.totalElements,
              totalPages: res.totalPages,
              last: res.last
            })
          ),
          catchError((err) => of(TransactionActions.loadTransactionsFailure({ error: toApiError(err) })))
        )
      )
    )
  );

  loadTransactionDetail$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TransactionActions.loadTransactionDetail),
      mergeMap(({ transactionId }) =>
        this.apiService.getTransaction(transactionId).pipe(
          map((transaction) => TransactionActions.loadTransactionDetailSuccess({ transaction })),
          catchError((err) => of(TransactionActions.loadTransactionDetailFailure({ error: toApiError(err) })))
        )
      )
    )
  );

  reloadTransactionsAfterCredit$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AccountActions.creditAccountSuccess),
      map(({ accountId }) => TransactionActions.loadTransactions({ accountId, page: 0, size: 10 }))
    )
  );

  reloadTransactionsAfterDebit$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AccountActions.debitAccountSuccess),
      map(({ accountId }) => TransactionActions.loadTransactions({ accountId, page: 0, size: 10 }))
    )
  );

  reloadTransactionsAfterExchange$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AccountActions.exchangeFundsSuccess),
      map(({ sourceAccountId }) => TransactionActions.loadTransactions({ accountId: sourceAccountId, page: 0, size: 10 }))
    )
  );
}
