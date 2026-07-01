import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { ApiService } from '../../core/services/api.service';
import { AccountActions } from './account.actions';
import { toApiError } from '../../core/interceptors/error.interceptor';
import { catchError, map, mergeMap, of } from 'rxjs';

@Injectable()
export class AccountEffects {
  private actions$ = inject(Actions);
  private apiService = inject(ApiService);

  loadAccounts$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AccountActions.loadAccounts),
      mergeMap(({ userId }) =>
        this.apiService.getUserAccounts(userId).pipe(
          map((accounts) => AccountActions.loadAccountsSuccess({ accounts })),
          catchError((err) => of(AccountActions.loadAccountsFailure({ error: toApiError(err) })))
        )
      )
    )
  );

  createAccount$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AccountActions.createAccount),
      mergeMap(({ userId, currency }) =>
        this.apiService.createAccount(userId, { currency }).pipe(
          map((account) => AccountActions.createAccountSuccess({ account })),
          catchError((err) => of(AccountActions.createAccountFailure({ error: toApiError(err) })))
        )
      )
    )
  );

  loadAccountDetail$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AccountActions.loadAccountDetail),
      mergeMap(({ accountId }) =>
        this.apiService.getAccount(accountId).pipe(
          map((account) => AccountActions.loadAccountDetailSuccess({ account })),
          catchError((err) => of(AccountActions.loadAccountDetailFailure({ error: toApiError(err) })))
        )
      )
    )
  );

  creditAccount$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AccountActions.creditAccount),
      mergeMap(({ accountId, amount }) =>
        this.apiService.creditAccount(accountId, { amount }).pipe(
          map((tx) => AccountActions.creditAccountSuccess({ accountId: tx.accountId, balanceAfter: tx.balanceAfter })),
          catchError((err) => of(AccountActions.creditAccountFailure({ error: toApiError(err) })))
        )
      )
    )
  );

  debitAccount$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AccountActions.debitAccount),
      mergeMap(({ accountId, amount }) =>
        this.apiService.debitAccount(accountId, { amount }).pipe(
          map((tx) => AccountActions.debitAccountSuccess({ accountId: tx.accountId, balanceAfter: tx.balanceAfter })),
          catchError((err) => of(AccountActions.debitAccountFailure({ error: toApiError(err) })))
        )
      )
    )
  );

  exchangeFunds$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AccountActions.exchangeFunds),
      mergeMap(({ sourceAccountId, targetAccountId, amount }) =>
        this.apiService.exchange({ sourceAccountId, targetAccountId, amount }).pipe(
          map((transactions) => {
            // Find the source and target transaction based on ID matching or just order
            // Backend sends source first, target second, but let's map them safely
            const sourceTx = transactions.find(t => t.accountId === sourceAccountId) || transactions[0];
            const targetTx = transactions.find(t => t.accountId === targetAccountId) || transactions[1];
            return AccountActions.exchangeFundsSuccess({
              sourceAccountId: sourceTx.accountId,
              sourceBalanceAfter: sourceTx.balanceAfter,
              targetAccountId: targetTx.accountId,
              targetBalanceAfter: targetTx.balanceAfter
            });
          }),
          catchError((err) => of(AccountActions.exchangeFundsFailure({ error: toApiError(err) })))
        )
      )
    )
  );
}
