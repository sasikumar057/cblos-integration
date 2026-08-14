import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CorporateCustomer } from './customer.service';

export interface LoanOfficer {
  id?: number;
  name: string;
  employeeId?: string;
  role?: string;
  employeeEmail?: string;
  password?: string;
  activeApplicationCount?: number;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/admin`;

  getPendingCustomers(): Observable<CorporateCustomer[]> {
    return this.http.get<CorporateCustomer[]>(`${this.base}/customers/pending`);
  }

  getAllStaff(): Observable<LoanOfficer[]> {
    return this.http.get<LoanOfficer[]>(`${this.base}/staff/all`);
  }

  onboardStaff(staff: LoanOfficer, employeeEmail: string): Observable<LoanOfficer> {
    return this.http.post<LoanOfficer>(`${this.base}/staff/onboard`, staff, {
      params: { employeeEmail }
    });
  }

  reviewCustomer(customerId: number, approve: boolean, reason?: string): Observable<CorporateCustomer> {
    const params: Record<string, string> = { approve: approve.toString() };
    if (reason) params['reason'] = reason;
    return this.http.put<CorporateCustomer>(`${this.base}/customers/review/${customerId}`, null, { params });
  }
}
