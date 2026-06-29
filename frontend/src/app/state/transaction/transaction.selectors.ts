import { createFeatureSelector, createSelector } from '@ngrx/store';
import { TransactionState, transactionAdapter } from './transaction.reducer';

export const selectTransactionState = createFeatureSelector<TransactionState>('transaction');

const { selectAll, selectEntities } = transactionAdapter.getSelectors();

export const selectAllTransactions = createSelector(
  selectTransactionState,
  selectAll
);

export const selectTransactionEntities = createSelector(
  selectTransactionState,
  selectEntities
);

export const selectSelectedTransactionId = createSelector(
  selectTransactionState,
  (state) => state.selectedTransactionId
);

export const selectSelectedTransaction = createSelector(
  selectTransactionEntities,
  selectSelectedTransactionId,
  (entities, selectedId) => (selectedId !== null ? entities[selectedId] : null) || null
);

export const selectTransactionLoading = createSelector(
  selectTransactionState,
  (state) => state.loading
);

export const selectTransactionError = createSelector(
  selectTransactionState,
  (state) => state.error
);

export const selectTransactionPageInfo = createSelector(
  selectTransactionState,
  (state) => ({
    page: state.page,
    size: state.size,
    totalElements: state.totalElements,
    totalPages: state.totalPages,
    last: state.last
  })
);
