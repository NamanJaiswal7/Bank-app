import { EntityState, EntityAdapter, createEntityAdapter } from '@ngrx/entity';
import { createReducer, on } from '@ngrx/store';
import { Transaction, ApiError } from '../../core/models/bank.models';
import { TransactionActions } from './transaction.actions';

export interface TransactionState extends EntityState<Transaction> {
  selectedTransactionId: number | null;
  loading: boolean;
  error: ApiError | null;
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export const transactionAdapter: EntityAdapter<Transaction> = createEntityAdapter<Transaction>({
  selectId: (transaction: Transaction) => transaction.id,
  sortComparer: (a, b) => b.timestamp.localeCompare(a.timestamp)
});

export const initialTransactionState: TransactionState = transactionAdapter.getInitialState({
  selectedTransactionId: null,
  loading: false,
  error: null,
  page: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0,
  last: false
});

export const transactionReducer = createReducer(
  initialTransactionState,

  on(TransactionActions.loadTransactions, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(TransactionActions.loadTransactionsSuccess, (state, { transactions, page, size, totalElements, totalPages, last }) => {
    const updatedState = {
      ...state,
      loading: false,
      page,
      size,
      totalElements,
      totalPages,
      last
    };
    if (page === 0) {
      return transactionAdapter.setAll(transactions, updatedState);
    } else {
      return transactionAdapter.addMany(transactions, updatedState);
    }
  }),

  on(TransactionActions.loadTransactionsFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  on(TransactionActions.loadTransactionDetail, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(TransactionActions.loadTransactionDetailSuccess, (state, { transaction }) => {
    return transactionAdapter.upsertOne(transaction, {
      ...state,
      loading: false,
      selectedTransactionId: transaction.id
    });
  }),

  on(TransactionActions.loadTransactionDetailFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  on(TransactionActions.clearTransactions, (state) => {
    return transactionAdapter.removeAll({
      ...state,
      selectedTransactionId: null,
      page: 0,
      totalElements: 0,
      totalPages: 0,
      last: false
    });
  })
);
