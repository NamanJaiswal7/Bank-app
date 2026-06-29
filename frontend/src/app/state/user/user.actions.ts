import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { User } from '../../core/models/bank.models';

export const UserActions = createActionGroup({
  source: 'User API',
  events: {
    'Load Users': emptyProps(),
    'Load Users Success': props<{ users: User[] }>(),
    'Load Users Failure': props<{ error: string }>(),
    'Select User': props<{ userId: number }>(),
  }
});
