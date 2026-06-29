import { EntityState, EntityAdapter, createEntityAdapter } from '@ngrx/entity';
import { createReducer, on } from '@ngrx/store';
import { Account } from '../../core/models/bank.models';
import { AccountActions } from './account.actions';

export interface AccountState extends EntityState<Account> {
  selectedAccountId: number | null;
  loading: boolean;
  actionLoading: boolean;
  error: string | null;
}

export const accountAdapter: EntityAdapter<Account> = createEntityAdapter<Account>();

export const initialAccountState: AccountState = accountAdapter.getInitialState({
  selectedAccountId: null,
  loading: false,
  actionLoading: false,
  error: null
});

export const accountReducer = createReducer(
  initialAccountState,

  // Load Accounts
  on(AccountActions.loadAccounts, (state) => ({
    ...state,
    loading: true,
    error: null
  })),
  on(AccountActions.loadAccountsSuccess, (state, { accounts }) => {
    return accountAdapter.setAll(accounts, {
      ...state,
      loading: false
    });
  }),
  on(AccountActions.loadAccountsFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // Create Account
  on(AccountActions.createAccount, (state) => ({
    ...state,
    actionLoading: true,
    error: null
  })),
  on(AccountActions.createAccountSuccess, (state, { account }) => {
    return accountAdapter.addOne(account, {
      ...state,
      actionLoading: false
    });
  }),
  on(AccountActions.createAccountFailure, (state, { error }) => ({
    ...state,
    actionLoading: false,
    error
  })),

  // Load Account Detail
  on(AccountActions.loadAccountDetail, (state) => ({
    ...state,
    loading: true,
    error: null
  })),
  on(AccountActions.loadAccountDetailSuccess, (state, { account }) => {
    return accountAdapter.upsertOne(account, {
      ...state,
      loading: false,
      selectedAccountId: account.id
    });
  }),
  on(AccountActions.loadAccountDetailFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // Credit Account
  on(AccountActions.creditAccount, (state) => ({
    ...state,
    actionLoading: true,
    error: null
  })),
  on(AccountActions.creditAccountSuccess, (state, { accountId, balanceAfter }) => {
    return accountAdapter.updateOne(
      { id: accountId, changes: { balance: balanceAfter } },
      { ...state, actionLoading: false }
    );
  }),
  on(AccountActions.creditAccountFailure, (state, { error }) => ({
    ...state,
    actionLoading: false,
    error
  })),

  // Debit Account
  on(AccountActions.debitAccount, (state) => ({
    ...state,
    actionLoading: true,
    error: null
  })),
  on(AccountActions.debitAccountSuccess, (state, { accountId, balanceAfter }) => {
    return accountAdapter.updateOne(
      { id: accountId, changes: { balance: balanceAfter } },
      { ...state, actionLoading: false }
    );
  }),
  on(AccountActions.debitAccountFailure, (state, { error }) => ({
    ...state,
    actionLoading: false,
    error
  })),

  // Exchange Funds
  on(AccountActions.exchangeFunds, (state) => ({
    ...state,
    actionLoading: true,
    error: null
  })),
  on(AccountActions.exchangeFundsSuccess, (state, { sourceAccountId, sourceBalanceAfter, targetAccountId, targetBalanceAfter }) => {
    return accountAdapter.updateMany(
      [
        { id: sourceAccountId, changes: { balance: sourceBalanceAfter } },
        { id: targetAccountId, changes: { balance: targetBalanceAfter } }
      ],
      { ...state, actionLoading: false }
    );
  }),
  on(AccountActions.exchangeFundsFailure, (state, { error }) => ({
    ...state,
    actionLoading: false,
    error
  })),

  // Clear Error
  on(AccountActions.clearError, (state) => ({
    ...state,
    error: null
  }))
);
