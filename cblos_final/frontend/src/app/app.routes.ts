import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { HomeComponent } from './pages/home/home.component';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { ForgotPasswordComponent } from './pages/forgot-password/forgot-password.component';
import { RegistrationSuccessComponent } from './pages/registration-success/registration-success.component';
import { AdminDashboardComponent } from './pages/admin/admin-dashboard/admin-dashboard.component';
import { CustomerDashboardComponent } from './pages/customer/customer-dashboard/customer-dashboard.component';
import { LoanApplyComponent } from './pages/customer/loan-apply/loan-apply.component';
import { CollateralComponent } from './pages/customer/collateral/collateral.component';
import { DocumentUploadComponent } from './pages/customer/document-upload/document-upload.component';
import { ApplicationSuccessComponent } from './pages/customer/application-success/application-success.component';
import { LoanRepaymentComponent } from './pages/customer/loan-repayment/loan-repayment.component';
import { OfficerDashboardComponent } from './pages/officer/officer-dashboard/officer-dashboard.component';
import { ManagerDashboardComponent } from './pages/manager/manager-dashboard/manager-dashboard.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'corporate-login', component: LoginComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'register/success', component: RegistrationSuccessComponent },
  {
    path: 'admin/dashboard',
    component: AdminDashboardComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'customer/dashboard/:customerId',
    component: CustomerDashboardComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['CUSTOMER'] }
  },
  {
    path: 'customer/dashboard/:customerId/apply',
    component: LoanApplyComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['CUSTOMER'] }
  },
  {
    path: 'customer/dashboard/application/:applicationId/collateral',
    component: CollateralComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['CUSTOMER'] }
  },
  {
    path: 'customer/dashboard/application/:applicationId/document',
    component: DocumentUploadComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['CUSTOMER'] }
  },
  {
    path: 'customer/dashboard/:customerId/application-complete',
    component: ApplicationSuccessComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['CUSTOMER'] }
  },
  {
    path: 'customer/dashboard/loan/repay/:accountId',
    component: LoanRepaymentComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['CUSTOMER'] }
  },
  {
    path: 'officer/dashboard/workdesk/:officerId',
    component: OfficerDashboardComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['OFFICER'] }
  },
  {
    path: 'manager/dashboard/workdesk/:managerId',
    component: ManagerDashboardComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['MANAGER'] }
  },
  { path: '**', redirectTo: '/login' }
];
