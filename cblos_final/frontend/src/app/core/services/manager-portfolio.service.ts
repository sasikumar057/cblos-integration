import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AccountRepaymentSummary {
  accountId?: number;
  accountNumber?: string;
  customerId?: number;
  customerName?: string;
  applicationId?: number;
  loanType?: string;
  accountStatus?: string;
  disbursedAmount?: number;
  paidBackAmount?: number;
  remainingAmount?: number;
  nextDueDate?: string;
}

export interface ManagerPortfolioSummary {
  totalDisbursed?: number;
  totalPaidBack?: number;
  totalRemaining?: number;
  nearestDueDate?: string;
  activeAccountCount?: number;
  accounts?: AccountRepaymentSummary[];


  
}

@Injectable({ providedIn: 'root' })
export class ManagerPortfolioService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/manager/portfolio`;

  getSummary(): Observable<ManagerPortfolioSummary> {
    return this.http.get<ManagerPortfolioSummary>(`${this.base}/summary`);
  }
}

