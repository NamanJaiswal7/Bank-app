import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { ApiError } from '../models/bank.models';

/**
 * Global HTTP error interceptor.
 *
 * Normalises every failing response into an {@link ApiError}-shaped object
 * so that NgRx effects (and component error handlers) can rely on a stable
 * envelope — including the machine-readable {@code errorCode}, the HTTP
 * {@code status}, and a user-facing {@code message}.
 *
 * The thrown value is still an {@link Error} (so existing `err.message`
 * usages keep working), but carries the typed envelope as extra properties.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const apiError: ApiError = {
        status: error.status,
        errorCode: 'UNKNOWN_ERROR',
        message: 'An unknown error occurred'
      };

      if (error.error instanceof ErrorEvent) {
        // Client-side / network failure (CORS, offline, DNS, etc.)
        apiError.errorCode = 'NETWORK_ERROR';
        apiError.message = `Network error: ${error.error.message}`;
      } else if (error.error && typeof error.error === 'object') {
        // Server returned the structured ErrorResponse envelope.
        apiError.errorCode = error.error.errorCode ?? `HTTP_${error.status}`;
        apiError.message = error.error.message ?? error.message;
        apiError.timestamp = error.error.timestamp;
        apiError.path = error.error.path;
      } else {
        apiError.errorCode = `HTTP_${error.status}`;
        apiError.message = `Server error (${error.status}): ${error.statusText || 'Unknown'}`;
      }

      // Keep `Error` semantics so existing `err.message` consumers still work,
      // but expose the full envelope on the thrown object.
      const enriched: Error & ApiError = Object.assign(
        new Error(apiError.message),
        apiError
      );

      console.error('[API error]', apiError.errorCode, apiError.message, error);
      return throwError(() => enriched);
    })
  );
};
