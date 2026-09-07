import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface LoanAccount {
  id?: number;
  accountNumber?: string;
  principalAmount?: number;
  interestRate?: number;
  status?: string;
  openingDate?: string;
  customer?: { id?: number; companyName?: string };
  loanApplication?: { applicationId?: number; loanType?: string };
}

@Injectable({ providedIn: 'root' })
export class LoanAccountService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/accounts`;

  getByCustomer(customerId: number): Observable<LoanAccount[]> {
    return this.http.get<LoanAccount[]>(`${this.base}/customer/${customerId}`);
  }

  getById(accountId: number): Observable<LoanAccount> {
    return this.http.get<LoanAccount>(`${this.base}/${accountId}`);
  }
}
