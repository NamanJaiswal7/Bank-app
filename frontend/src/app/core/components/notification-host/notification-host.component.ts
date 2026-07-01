import { Component, inject } from '@angular/core';
import { NgFor, NgClass } from '@angular/common';
import { NotificationService } from '../../services/notification.service';

/**
 * Global toast host — renders whatever the {@link NotificationService} queue
 * currently contains.
 *
 * <p>Mount this component once, at the application root, and the entire app
 * gets consistent, dismissable toast notifications for HTTP errors, form
 * validation failures, and success confirmations. No per-page wiring
 * required.</p>
 */
@Component({
  selector: 'app-notification-host',
  standalone: true,
  imports: [NgFor, NgClass],
  template: `
    <div class="notification-host" role="region" aria-live="polite" aria-label="Notifications">
      <div
        *ngFor="let n of notifications()"
        class="notification-toast"
        [ngClass]="'severity-' + n.severity"
        role="alert"
      >
        <span class="notification-icon" aria-hidden="true">
          <i
            [ngClass]="{
              'bi-exclamation-octagon-fill': n.severity === 'error',
              'bi-exclamation-triangle-fill': n.severity === 'warning',
              'bi-check-circle-fill': n.severity === 'success',
              'bi-info-circle-fill': n.severity === 'info'
            }"
            class="bi"
          ></i>
        </span>
        <div class="notification-body">
          <p class="notification-message">{{ n.message }}</p>
          <p class="notification-code" *ngIf="n.errorCode">{{ n.errorCode }}</p>
        </div>
        <button
          type="button"
          class="notification-dismiss"
          (click)="service.dismiss(n.id)"
          aria-label="Dismiss notification"
        >
          <i class="bi bi-x-lg" aria-hidden="true"></i>
        </button>
      </div>
    </div>
  `,
  styles: [`
    .notification-host {
      position: fixed;
      top: 1.25rem;
      right: 1.25rem;
      z-index: 2000;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      max-width: min(24rem, calc(100vw - 2rem));
      pointer-events: none;
    }

    .notification-toast {
      display: grid;
      grid-template-columns: auto 1fr auto;
      align-items: start;
      gap: 0.75rem;
      padding: 0.85rem 1rem;
      border-radius: 0.75rem;
      background: rgba(18, 20, 32, 0.92);
      color: #fff;
      backdrop-filter: blur(12px);
      border: 1px solid rgba(255, 255, 255, 0.08);
      box-shadow: 0 12px 32px rgba(0, 0, 0, 0.35);
      pointer-events: auto;
      animation: slide-in 180ms ease-out;
    }

    .severity-error   { border-left: 4px solid #ef4444; }
    .severity-warning { border-left: 4px solid #f59e0b; }
    .severity-success { border-left: 4px solid #10b981; }
    .severity-info    { border-left: 4px solid #6366f1; }

    .notification-icon .bi {
      font-size: 1.2rem;
      line-height: 1;
    }
    .severity-error   .notification-icon .bi { color: #ef4444; }
    .severity-warning .notification-icon .bi { color: #f59e0b; }
    .severity-success .notification-icon .bi { color: #10b981; }
    .severity-info    .notification-icon .bi { color: #6366f1; }

    .notification-body { min-width: 0; }
    .notification-message {
      margin: 0;
      font-size: 0.9rem;
      line-height: 1.35;
      word-wrap: break-word;
    }
    .notification-code {
      margin: 0.25rem 0 0;
      font-family: ui-monospace, 'SFMono-Regular', Menlo, monospace;
      font-size: 0.7rem;
      opacity: 0.6;
      letter-spacing: 0.02em;
    }

    .notification-dismiss {
      background: transparent;
      border: 0;
      color: rgba(255, 255, 255, 0.6);
      cursor: pointer;
      padding: 0.15rem 0.35rem;
      border-radius: 0.35rem;
      transition: background 120ms ease, color 120ms ease;
    }
    .notification-dismiss:hover {
      background: rgba(255, 255, 255, 0.08);
      color: #fff;
    }

    @keyframes slide-in {
      from { transform: translateX(120%); opacity: 0; }
      to   { transform: translateX(0);    opacity: 1; }
    }
  `],
})
export class NotificationHostComponent {
  readonly service = inject(NotificationService);
  readonly notifications = this.service.notifications;
}
