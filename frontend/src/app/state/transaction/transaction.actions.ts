import { createActionGroup, props, emptyProps } from '@ngrx/store';
import { Transaction, ApiError } from '../../core/models/bank.models';

export const TransactionActions = createActionGroup({
  source: 'Transaction API',
  events: {
    'Load Transactions': props<{ accountId: number; page: number; size: number }>(),
    'Load Transactions Success': props<{
      transactions: Transaction[];
      page: number;
      size: number;
      totalElements: number;
      totalPages: number;
      last: boolean;
    }>(),
    'Load Transactions Failure': props<{ error: ApiError }>(),

    'Load Transaction Detail': props<{ transactionId: number }>(),
    'Load Transaction Detail Success': props<{ transaction: Transaction }>(),
    'Load Transaction Detail Failure': props<{ error: ApiError }>(),

    'Clear Transactions': emptyProps(),
  }
});
