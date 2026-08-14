import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface RepaymentSchedule {
  id?: number;
  installmentNumber?: number;
  dueDate?: string;
  installmentAmount?: number;
  principalComponent?: number;
  interestComponent?: number;
  status?: string;
}

@Injectable({ providedIn: 'root' })
export class RepaymentService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/repayments`;

  getSchedule(accountId: number): Observable<RepaymentSchedule[]> {
    return this.http.get<RepaymentSchedule[]>(`${this.base}/schedule/${accountId}`);
  }

  payInstallment(accountId: number, installmentId: number): Observable<string> {
    return this.http.put(`${this.base}/account/${accountId}/pay/${installmentId}`, null, { responseType: 'text' });
  }

  settleAccount(accountId: number): Observable<string> {
    return this.http.put(`${this.base}/account/${accountId}/settle`, null, { responseType: 'text' });
  }
}
