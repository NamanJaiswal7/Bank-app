import { HttpInterceptorFn, HttpErrorResponse, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, catchError, throwError, timer, retry } from 'rxjs';
import { ApiError } from '../models/bank.models';
import { NotificationService } from '../services/notification.service';

/**
 * Extends the standard {@link Error} with the fields of {@link ApiError} so
 * that consumers can rely on either shape. {@code catchError} handlers
 * receive an instance of this type.
 */
export type EnrichedHttpError = Error & ApiError;

/**
 * Type-guard for narrowing an unknown error to the enriched envelope produced
 * by the interceptor. Useful in NgRx effects and component-level handlers.
 */
export function isApiError(err: unknown): err is EnrichedHttpError {
  return !!err
    && typeof err === 'object'
    && 'errorCode' in err
    && 'status' in err
    && 'message' in err;
}

/**
 * Convert any thrown value into a canonical {@link ApiError}. Guarantees that
 * downstream code (reducers, notification service) always sees the same
 * shape, even for programming errors thrown outside the HTTP path.
 */
export function toApiError(err: unknown): ApiError {
  if (isApiError(err)) {
    return {
      status: err.status,
      errorCode: err.errorCode,
      message: err.message,
      timestamp: err.timestamp,
      path: err.path,
    };
  }
  const message = err instanceof Error ? err.message : String(err ?? 'Unknown error');
  return { status: 0, errorCode: 'UNKNOWN_ERROR', message };
}

/**
 * HTTP methods that are safe to retry automatically — they must be idempotent
 * so a duplicate delivery cannot double-charge an account, etc. POSTs to
 * {@code /credit}, {@code /debit}, {@code /exchange} are intentionally
 * excluded; the backend already retries optimistic-locking conflicts via
 * {@code @Retryable} on the service beans.
 */
const RETRIABLE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS']);

/**
 * Server statuses that indicate a transient failure worth retrying.
 * {@code 0} covers network / CORS errors where the response never arrived.
 */
const RETRIABLE_STATUSES = new Set([0, 502, 503, 504]);

/**
 * Number of automatic retries in addition to the initial attempt.
 */
const MAX_RETRIES = 2;

/**
 * Global HTTP error interceptor.
 *
 * Responsibilities:
 * <ol>
 *   <li>Transparently retry idempotent requests that fail with a transient
 *       server / network error, using exponential back-off with jitter.</li>
 *   <li>Normalise every failing response into an {@link ApiError}-shaped
 *       object so NgRx effects (and component error handlers) can rely on a
 *       stable envelope — including the machine-readable {@code errorCode},
 *       the HTTP {@code status}, and a user-facing {@code message}.</li>
 *   <li>Surface every failure through the {@link NotificationService} so the
 *       user always sees a toast, without per-component wiring.</li>
 * </ol>
 *
 * <p>The thrown value is still an {@link Error} (so existing {@code err.message}
 * usages keep working) but carries the typed envelope as extra properties.</p>
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const notifications = inject(NotificationService);

  return next(req).pipe(
    retry({
      count: MAX_RETRIES,
      delay: (error, retryCount) => shouldRetry(req, error, retryCount),
    }),
    catchError((error: HttpErrorResponse) => {
      const apiError = normalise(error);
      const enriched: EnrichedHttpError = Object.assign(new Error(apiError.message), apiError);

      console.error('[API error]', apiError.errorCode, apiError.status, apiError.message, error);

      notifications.showError(apiError.message, apiError.errorCode);

      return throwError(() => enriched);
    })
  );
};

/**
 * Returns a delay Observable if the request should be retried, or re-throws
 * the error to short-circuit the {@code retry} operator.
 */
function shouldRetry(
  req: HttpRequest<unknown>,
  error: unknown,
  retryCount: number
): Observable<number> {
  const httpError = error as HttpErrorResponse;
  const method = req.method.toUpperCase();

  if (!RETRIABLE_METHODS.has(method) || !RETRIABLE_STATUSES.has(httpError?.status ?? -1)) {
    // Non-retriable: propagate immediately.
    return throwError(() => error);
  }

  // Exponential back-off with jitter: ~250ms, ~750ms (± 25%).
  const base = 250 * Math.pow(3, retryCount - 1);
  const jitter = base * (0.75 + Math.random() * 0.5);
  return timer(jitter);
}

/**
 * Convert an {@link HttpErrorResponse} into a stable {@link ApiError}.
 */
function normalise(error: HttpErrorResponse): ApiError {
  const apiError: ApiError = {
    status: error.status,
    errorCode: 'UNKNOWN_ERROR',
    message: 'An unknown error occurred',
  };

  if (error.error instanceof ErrorEvent) {
    // Client-side / network failure (CORS, offline, DNS, etc.)
    apiError.errorCode = 'NETWORK_ERROR';
    apiError.message = `Network error: ${error.error.message}`;
  } else if (error.status === 0) {
    apiError.errorCode = 'NETWORK_ERROR';
    apiError.message = 'Unable to reach the server. Check your connection and try again.';
  } else if (error.error && typeof error.error === 'object') {
    // Server returned the structured ErrorResponse envelope.
    apiError.errorCode = error.error.errorCode ?? `HTTP_${error.status}`;
    apiError.message = error.error.message ?? error.message ?? `Request failed with ${error.status}`;
    apiError.timestamp = error.error.timestamp;
    apiError.path = error.error.path;
  } else {
    apiError.errorCode = `HTTP_${error.status}`;
    apiError.message = `Server error (${error.status}): ${error.statusText || 'Unknown'}`;
  }

  return apiError;
}
