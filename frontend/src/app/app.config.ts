import { ApplicationConfig, provideZoneChangeDetection, isDevMode } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideStore } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { provideStoreDevtools } from '@ngrx/store-devtools';

import { routes } from './app.routes';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { userReducer } from './state/user/user.reducer';
import { UserEffects } from './state/user/user.effects';
import { accountReducer } from './state/account/account.reducer';
import { AccountEffects } from './state/account/account.effects';
import { transactionReducer } from './state/transaction/transaction.reducer';
import { TransactionEffects } from './state/transaction/transaction.effects';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([errorInterceptor])),
    provideStore({
      user: userReducer,
      account: accountReducer,
      transaction: transactionReducer
    }),
    provideEffects([
      UserEffects,
      AccountEffects,
      TransactionEffects
    ]),
    provideStoreDevtools({
      maxAge: 25,
      logOnly: !isDevMode(),
      autoPause: true,
      trace: false,
      traceLimit: 75
    })
  ]
};
