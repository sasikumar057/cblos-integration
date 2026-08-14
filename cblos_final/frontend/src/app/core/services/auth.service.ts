import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, of, map } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface UserProfile {
  email: string;
  role: string;
  corporateCustomerId?: number | null;
  loanOfficerId?: number | null;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly base = `${environment.apiUrl}/api/auth`;

  readonly currentUser = signal<UserProfile | null>(null);

  login(credentials: { username: string; password: string }): Observable<UserProfile> {
    return this.http.post<UserProfile>(`${this.base}/login`, credentials).pipe(
      tap(user => this.currentUser.set(user))
    );
  }

  logout(redirectTo: unknown[] | null = ['/']): Observable<void> {
    return this.http.post<{ status: string }>(`${this.base}/logout`, {}).pipe(
      tap(() => {
        this.currentUser.set(null);
        if (redirectTo) this.router.navigate(redirectTo as string[]);
      }),
      map(() => void 0)
    );
  }

  me(): Observable<UserProfile | null> {
    return this.http.get<UserProfile>(`${this.base}/me`).pipe(
      tap(user => this.currentUser.set(user)),
      catchError(() => {
        this.currentUser.set(null);
        return of(null);
      })
    );
  }

  navigateAfterLogin(user: UserProfile): void {
    switch (user.role) {
      case 'ADMIN':
        this.router.navigate(['/admin/dashboard']);
        break;
      case 'CUSTOMER':
        this.router.navigate(['/customer/dashboard', user.corporateCustomerId]);
        break;
      case 'OFFICER':
        this.router.navigate(['/officer/dashboard/workdesk', user.loanOfficerId]);
        break;
      case 'MANAGER':
        this.router.navigate(['/manager/dashboard/workdesk', user.loanOfficerId]);
        break;
      default:
        this.router.navigate(['/login']);
    }
  }
}
