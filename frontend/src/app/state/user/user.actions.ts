import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { User, ApiError } from '../../core/models/bank.models';

export const UserActions = createActionGroup({
  source: 'User API',
  events: {
    'Load Users': emptyProps(),
    'Load Users Success': props<{ users: User[] }>(),
    'Load Users Failure': props<{ error: ApiError }>(),
    'Select User': props<{ userId: number }>(),
  }
});
