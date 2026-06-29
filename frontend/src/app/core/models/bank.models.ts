export type Currency = 'USD' | 'EUR' | 'GBP' | 'SEK' | 'VND';

export type TransactionType = 'CREDIT' | 'DEBIT' | 'EXCHANGE_OUT' | 'EXCHANGE_IN';

export interface User {
  id: number;
  username: string;
  email: string;
  createdAt: string;
}

export interface Account {
  id: number;
  userId: number;
  currency: Currency;
  balance: number;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface Transaction {
  id: number;
  accountId: number;
  type: TransactionType;
  amount: number;
  currency: Currency;
  balanceAfter: number;
  referenceId?: string;
  description?: string;
  timestamp: string;
}

export interface TransactionPageResponse {
  content: Transaction[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface CreateAccountRequest {
  currency: Currency;
}

export interface CreditRequest {
  amount: number;
}

export interface DebitRequest {
  amount: number;
}

export interface ExchangeRequest {
  sourceAccountId: number;
  targetAccountId: number;
  amount: number;
}

/**
 * Standard error envelope returned by the backend.
 * Mirrors `com.bank.adapter.in.web.dto.ErrorResponse`.
 */
export interface ApiError {
  timestamp?: string;
  status: number;
  errorCode: string;
  message: string;
  path?: string;
}
