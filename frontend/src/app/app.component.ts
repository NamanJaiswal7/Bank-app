import { Component, OnInit, inject } from '@angular/core';
import { RouterOutlet, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AsyncPipe, NgFor } from '@angular/common';
import { Store } from '@ngrx/store';
import { UserActions } from './state/user/user.actions';
import { AccountActions } from './state/account/account.actions';
import { selectAllUsers, selectSelectedUserId } from './state/user/user.selectors';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, AsyncPipe, NgFor],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  private store = inject(Store);
  private router = inject(Router);

  users$ = this.store.select(selectAllUsers);
  selectedUserId$ = this.store.select(selectSelectedUserId);

  ngOnInit() {
    // Load initial users
    this.store.dispatch(UserActions.loadUsers());
    
    // Automatically load accounts when selected user changes
    this.selectedUserId$.subscribe(userId => {
      if (userId !== null) {
        this.store.dispatch(AccountActions.loadAccounts({ userId }));
      }
    });
  }

  onUserChange(event: Event) {
    const selectElement = event.target as HTMLSelectElement;
    const userId = Number(selectElement.value);
    if (!isNaN(userId)) {
      this.store.dispatch(UserActions.selectUser({ userId }));
      // Return to home page upon user change
      this.router.navigate(['/']);
    }
  }
}
