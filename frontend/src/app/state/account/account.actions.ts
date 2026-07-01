import { createActionGroup, props, emptyProps } from '@ngrx/store';
import { Account, Currency, ApiError } from '../../core/models/bank.models';

export const AccountActions = createActionGroup({
  source: 'Account API',
  events: {
    'Load Accounts': props<{ userId: number }>(),
    'Load Accounts Success': props<{ accounts: Account[] }>(),
    'Load Accounts Failure': props<{ error: ApiError }>(),

    'Create Account': props<{ userId: number; currency: Currency }>(),
    'Create Account Success': props<{ account: Account }>(),
    'Create Account Failure': props<{ error: ApiError }>(),

    'Load Account Detail': props<{ accountId: number }>(),
    'Load Account Detail Success': props<{ account: Account }>(),
    'Load Account Detail Failure': props<{ error: ApiError }>(),

    'Credit Account': props<{ accountId: number; amount: number }>(),
    'Credit Account Success': props<{ accountId: number; balanceAfter: number }>(),
    'Credit Account Failure': props<{ error: ApiError }>(),

    'Debit Account': props<{ accountId: number; amount: number }>(),
    'Debit Account Success': props<{ accountId: number; balanceAfter: number }>(),
    'Debit Account Failure': props<{ error: ApiError }>(),

    'Exchange Funds': props<{ sourceAccountId: number; targetAccountId: number; amount: number }>(),
    'Exchange Funds Success': props<{
      sourceAccountId: number;
      sourceBalanceAfter: number;
      targetAccountId: number;
      targetBalanceAfter: number;
    }>(),
    'Exchange Funds Failure': props<{ error: ApiError }>(),

    'Clear Error': emptyProps(),
  }
});
