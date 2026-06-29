import { createFeatureSelector, createSelector } from '@ngrx/store';
import { AccountState, accountAdapter } from './account.reducer';

export const selectAccountState = createFeatureSelector<AccountState>('account');

const { selectAll, selectEntities } = accountAdapter.getSelectors();

export const selectAllAccounts = createSelector(
  selectAccountState,
  selectAll
);

export const selectAccountEntities = createSelector(
  selectAccountState,
  selectEntities
);

export const selectSelectedAccountId = createSelector(
  selectAccountState,
  (state) => state.selectedAccountId
);

export const selectSelectedAccount = createSelector(
  selectAccountEntities,
  selectSelectedAccountId,
  (entities, selectedId) => (selectedId !== null ? entities[selectedId] : null) || null
);

export const selectAccountLoading = createSelector(
  selectAccountState,
  (state) => state.loading
);

export const selectAccountActionLoading = createSelector(
  selectAccountState,
  (state) => state.actionLoading
);

export const selectAccountError = createSelector(
  selectAccountState,
  (state) => state.error
);
