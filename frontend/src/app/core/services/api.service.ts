import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  User,
  Account,
  Transaction,
  TransactionPageResponse,
  CreateAccountRequest,
  CreditRequest,
  DebitRequest,
  ExchangeRequest
} from '../models/bank.models';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private http = inject(HttpClient);
  private baseUrl = '/api';

  // --- Users ---
  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.baseUrl}/users`);
  }

  // --- Accounts ---
  getUserAccounts(userId: number): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.baseUrl}/users/${userId}/accounts`);
  }

  createAccount(userId: number, request: CreateAccountRequest): Observable<Account> {
    return this.http.post<Account>(`${this.baseUrl}/users/${userId}/accounts`, request);
  }

  getAccount(accountId: number): Observable<Account> {
    return this.http.get<Account>(`${this.baseUrl}/accounts/${accountId}`);
  }

  creditAccount(accountId: number, request: CreditRequest): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.baseUrl}/accounts/${accountId}/credit`, request);
  }

  debitAccount(accountId: number, request: DebitRequest): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.baseUrl}/accounts/${accountId}/debit`, request);
  }

  // --- Exchange ---
  exchange(request: ExchangeRequest): Observable<Transaction[]> {
    return this.http.post<Transaction[]>(`${this.baseUrl}/exchange`, request);
  }

  // --- Transactions ---
  getTransactions(accountId: number, page: number = 0, size: number = 10): Observable<TransactionPageResponse> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<TransactionPageResponse>(`${this.baseUrl}/accounts/${accountId}/transactions`, { params });
  }

  getTransaction(transactionId: number): Observable<Transaction> {
    return this.http.get<Transaction>(`${this.baseUrl}/transactions/${transactionId}`);
  }
}
