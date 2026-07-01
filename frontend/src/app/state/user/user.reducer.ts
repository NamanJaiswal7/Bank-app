import { createReducer, on } from '@ngrx/store';
import { User, ApiError } from '../../core/models/bank.models';
import { UserActions } from './user.actions';

export interface UserState {
  users: User[];
  selectedUserId: number | null;
  loading: boolean;
  error: ApiError | null;
}

export const initialUserState: UserState = {
  users: [],
  selectedUserId: null,
  loading: false,
  error: null
};

export const userReducer = createReducer(
  initialUserState,
  on(UserActions.loadUsers, (state) => ({
    ...state,
    loading: true,
    error: null
  })),
  on(UserActions.loadUsersSuccess, (state, { users }) => ({
    ...state,
    users,
    loading: false,
    selectedUserId: state.selectedUserId === null && users.length > 0 ? users[0].id : state.selectedUserId
  })),
  on(UserActions.loadUsersFailure, (state, { error }) => ({
    ...state,
    error,
    loading: false
  })),
  on(UserActions.selectUser, (state, { userId }) => ({
    ...state,
    selectedUserId: userId
  }))
);
