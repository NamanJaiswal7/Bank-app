import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { ApiService } from '../../core/services/api.service';
import { UserActions } from './user.actions';
import { toApiError } from '../../core/interceptors/error.interceptor';
import { catchError, map, mergeMap, of } from 'rxjs';

@Injectable()
export class UserEffects {
  private actions$ = inject(Actions);
  private apiService = inject(ApiService);

  loadUsers$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UserActions.loadUsers),
      mergeMap(() =>
        this.apiService.getUsers().pipe(
          map((users) => UserActions.loadUsersSuccess({ users })),
          catchError((err) => of(UserActions.loadUsersFailure({ error: toApiError(err) })))
        )
      )
    )
  );
}
