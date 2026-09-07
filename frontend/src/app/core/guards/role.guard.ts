import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const user = auth.currentUser();
  const allowedRoles: string[] = route.data['roles'] ?? [];

  if (!user) {
    router.navigate(['/login']);
    return false;
  }

  if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
    auth.navigateAfterLogin(user);
    return false;
  }

  const customerId = route.paramMap.get('customerId');
  if (customerId && user.role === 'CUSTOMER' && user.corporateCustomerId?.toString() !== customerId) {
    router.navigate(['/customer/dashboard', user.corporateCustomerId]);
    return false;
  }

  const officerId = route.paramMap.get('officerId');
  if (officerId && user.role === 'OFFICER' && user.loanOfficerId?.toString() !== officerId) {
    router.navigate(['/officer/dashboard/workdesk', user.loanOfficerId]);
    return false;
  }

  const managerId = route.paramMap.get('managerId');
  if (managerId && user.role === 'MANAGER' && user.loanOfficerId?.toString() !== managerId) {
    router.navigate(['/manager/dashboard/workdesk', user.loanOfficerId]);
    return false;
  }

  return true;
};
