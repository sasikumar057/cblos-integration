import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface LoanApplication {
  applicationId?: number;
  loanType?: string;
  loanAmount?: number;
  status?: string;
  submissionDate?: string;
  requestedTenureMonths?: number;
  officerCreditScore?: number;
  officerAssessmentNotes?: string;
  customer?: { id?: number; companyName?: string };
  loanOfficer?: { id?: number; name?: string };
}

@Injectable({ providedIn: 'root' })
export class LoanService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/loans`;

  getAll(): Observable<LoanApplication[]> {
    return this.http.get<LoanApplication[]>(`${this.base}/all`);
  }

  getByCustomer(customerId: number): Observable<LoanApplication[]> {
    return this.http.get<LoanApplication[]>(`${this.base}/customer/${customerId}`);
  }

  getById(id: number): Observable<LoanApplication> {
    return this.http.get<LoanApplication>(`${this.base}/${id}`);
  }
 
  getByOfficer(officerId: number): Observable<LoanApplication[]> {
  return this.http.get<LoanApplication[]>(`${this.base}/officer/${officerId}`);
  }

  getByManager(managerId: number): Observable<LoanApplication[]> {
  return this.http.get<LoanApplication[]>(`${this.base}/manager/${managerId}`);
}

  submit(customerId: number, application: Partial<LoanApplication>): Observable<LoanApplication> {
    return this.http.post<LoanApplication>(`${this.base}/submit/${customerId}`, application);
  }

  officerReview(id: number, score: number, notes: string, pass: boolean): Observable<LoanApplication> {
    return this.http.put<LoanApplication>(`${this.base}/officer-review/${id}`, null, {
      params: { score: score.toString(), notes, pass: pass.toString() }
    });
  }

  managerApproval(id: number, approve: boolean): Observable<LoanApplication> {
    return this.http.put<LoanApplication>(`${this.base}/manager-approval/${id}`, null, {
      params: { approve: approve.toString() }
    });
  }

  withdraw(id: number): Observable<LoanApplication> {
    return this.http.put<LoanApplication>(`${this.base}/withdraw/${id}`, null);
  }
}
