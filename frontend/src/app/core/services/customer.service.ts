import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CorporateRegistrationRequest } from '../model/corporate-registration-request';

export interface CorporateCustomer {
  id?: number;
  companyName: string;
  taxId: string;
  companyEmail: string;
  phoneNumber?: string;
  businessAddress?: string;
  industryType?: string;
  status?: string;
  tempRegistrationPassword?: string;
  registrationPasswordConfigured?: boolean;
}

export interface RegistrationStatus {
  found: boolean;
  id?: number; 
  companyName?: string;
  companyEmail?: string;
  status: string;
  message: string;
  rejectionReason?: string;
  canLogin: boolean;
}

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/customers`;

  onboard(request: CorporateRegistrationRequest): Observable<CorporateCustomer> {
    return this.http.post<CorporateCustomer>(`${this.base}/onboard`, request);
  }

  updateDetails(customerId: number, customer: Partial<CorporateCustomer>): Observable<CorporateCustomer> {
  return this.http.patch<CorporateCustomer>(`${this.base}/update/${customerId}`, customer);
  }
  getById(id: number): Observable<CorporateCustomer> {
    return this.http.get<CorporateCustomer>(`${this.base}/${id}`);
  }

  verify(id: number, action: 'APPROVE' | 'REJECT', reason?: string): Observable<CorporateCustomer> {
    const params: Record<string, string> = { action };
    if (reason) params['reason'] = reason;
    return this.http.put<CorporateCustomer>(`${this.base}/verify/${id}`, null, { params });
  }

  checkRegistrationStatus(email: string): Observable<RegistrationStatus> {
    return this.http.get<RegistrationStatus>(`${this.base}/registration-status`, { params: { email } });
  }

  resetPassword(email: string, newPassword: string): Observable<string> {
    return this.http.post(`${this.base}/forgot-password`, { email, newPassword }, { responseType: 'text' });
  }
}
