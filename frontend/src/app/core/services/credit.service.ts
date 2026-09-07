import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CreditService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/credit`;

  getRiskScore(applicationId: number): Observable<number> {
    return this.http.get<number>(`${this.base}/risk-score/${applicationId}`);
  }
}
