import { Injectable, signal, computed } from '@angular/core';

/**
 * Severity levels for user-visible notifications.
 * Determines styling (color, icon) and, by convention, whether the message
 * is auto-dismissed or requires manual action.
 */
export type NotificationSeverity = 'error' | 'warning' | 'success' | 'info';

/**
 * A single notification in the queue.
 *
 * <p>{@link errorCode} is optional and mirrors the backend {@code ErrorResponse.errorCode}
 * so callers can attach machine-readable context (for e.g. i18n keys or analytics).</p>
 */
export interface Notification {
  id: number;
  severity: NotificationSeverity;
  message: string;
  errorCode?: string;
  /** When true, no timer is scheduled; the caller (or the user) must dismiss it. */
  sticky?: boolean;
  createdAt: number;
}

/**
 * Reusable, framework-agnostic notification queue.
 *
 * <p>Any part of the app (HTTP interceptor, components, effects, guards) can push
 * a notification; the {@code NotificationHostComponent} renders whatever the
 * queue currently contains — no per-component toast wiring required.</p>
 *
 * <p>Signals-based so consumers benefit from Angular's fine-grained reactivity;
 * a {@link computed} view of the queue lets templates render without change
 * detection ceremony.</p>
 */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private static readonly DEFAULT_TTL_MS: Record<NotificationSeverity, number> = {
    success: 4000,
    info: 5000,
    warning: 7000,
    error: 8000,
  };

  private readonly _notifications = signal<Notification[]>([]);
  /** Read-only view of the current queue for templates. */
  readonly notifications = computed(() => this._notifications());

  private nextId = 1;
  private readonly timers = new Map<number, ReturnType<typeof setTimeout>>();

  /**
   * Enqueue a notification. Returns the assigned id so the caller can dismiss
   * it programmatically before the auto-dismiss timer fires.
   */
  show(
    severity: NotificationSeverity,
    message: string,
    options: { errorCode?: string; sticky?: boolean; ttlMs?: number } = {}
  ): number {
    const id = this.nextId++;
    const notification: Notification = {
      id,
      severity,
      message,
      errorCode: options.errorCode,
      sticky: options.sticky,
      createdAt: Date.now(),
    };
    this._notifications.update((list) => [...list, notification]);

    if (!options.sticky) {
      const ttl = options.ttlMs ?? NotificationService.DEFAULT_TTL_MS[severity];
      const handle = setTimeout(() => this.dismiss(id), ttl);
      this.timers.set(id, handle);
    }
    return id;
  }

  /** Convenience for HTTP failures — always uses the {@code error} severity. */
  showError(message: string, errorCode?: string): number {
    return this.show('error', message, { errorCode });
  }

  showSuccess(message: string): number {
    return this.show('success', message);
  }

  showWarning(message: string): number {
    return this.show('warning', message);
  }

  showInfo(message: string): number {
    return this.show('info', message);
  }

  /** Remove a single notification by id. Idempotent — no-op if already gone. */
  dismiss(id: number): void {
    const timer = this.timers.get(id);
    if (timer) {
      clearTimeout(timer);
      this.timers.delete(id);
    }
    this._notifications.update((list) => list.filter((n) => n.id !== id));
  }

  /** Clear the entire queue (e.g. on route change or logout). */
  clearAll(): void {
    for (const handle of this.timers.values()) {
      clearTimeout(handle);
    }
    this.timers.clear();
    this._notifications.set([]);
  }
}
