import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface LoanProduct {
  id?: number;
  productName: string;
  description?: string;
  minLoanAmount?: number;
  maxLoanAmount?: number;
  defaultInterestRate?: number;
  minTenureMonths?: number;
  maxTenureMonths?: number;
}

@Injectable({ providedIn: 'root' })
export class LoanProductService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/products`;

  getCatalog(): Observable<LoanProduct[]> {
    return this.http.get<LoanProduct[]>(`${this.base}/catalog`);
  }
}
