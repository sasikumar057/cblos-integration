import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Collateral {
  id?: number;
  collateralType?: string;
  estimatedValue?: number;
  assetReferenceNumber?: string;
  description?: string;
  verificationStatus?: string;
}

@Injectable({ providedIn: 'root' })
export class CollateralService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/collateral`;

  add(applicationId: number, collateral: Collateral): Observable<Collateral> {
    return this.http.post<Collateral>(`${this.base}/add/${applicationId}`, collateral);
  }

  getByApplication(applicationId: number): Observable<Collateral[]> {
    return this.http.get<Collateral[]>(`${this.base}/loan/${applicationId}`);
  }
}
