import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { AdminService, LoanOfficer } from '../../../core/services/admin.service';
import { CorporateCustomer } from '../../../core/services/customer.service';
import { statusBadgeClass, statusLabel } from '../../../core/utils/status-ui';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly adminService = inject(AdminService);
  private readonly fb = inject(FormBuilder);

  pendingCustomers = signal<CorporateCustomer[]>([]);
  staff = signal<LoanOfficer[]>([]);
  selectedCustomer = signal<CorporateCustomer | null>(null);
  reviewReason = signal('');
  message = signal('');
  error = signal('');
  staffError = signal('');
  isLoading = signal(false);
  isReviewing = signal(false);
  isOnboarding = signal(false);
  customerSearch = signal('');
  staffSearch = signal('');
  lastRefreshed = signal('');
  pendingReviewAction = signal<'APPROVE' | 'REJECT' | null>(null);

  filteredPendingCustomers = computed(() => {
    const term = this.customerSearch().trim().toLowerCase();
    if (!term) return this.pendingCustomers();
    return this.pendingCustomers().filter(customer =>
      [customer.companyName, customer.taxId, customer.companyEmail, customer.phoneNumber, customer.industryType]
        .some(value => (value ?? '').toLowerCase().includes(term))
    );
  });

  filteredStaff = computed(() => {
    const term = this.staffSearch().trim().toLowerCase();
    if (!term) return this.staff();
    return this.staff().filter(member =>
      [member.name, member.employeeId, member.employeeEmail, member.role]
        .some(value => (value ?? '').toLowerCase().includes(term))
    );
  });

  operationsHealth = computed(() => {
    if (this.pendingCustomers().length > 5) return 'High onboarding load';
    if (this.staff().length === 0) return 'Officer capacity missing';
    return 'Healthy';
  });

  recommendedAction = computed(() => {
    if (this.staff().length === 0) return 'Onboard at least one officer before approving new customers.';
    if (this.pendingCustomers().length > 0) return 'Review pending customer registrations and approve verified companies.';
    return 'No immediate admin action required.';
  });

  adminStats = computed(() => ({
    pending: this.pendingCustomers().length,
    staff: this.staff().length,
    officers: this.staff().filter(member => member.role === 'OFFICER').length,
    managers: this.staff().filter(member => member.role === 'MANAGER').length
  }));

  staffForm = this.fb.group({
    name: ['', Validators.required],
    employeeId: ['', Validators.required],
    role: ['OFFICER', Validators.required],
    password: ['', Validators.required],
    employeeEmail: ['', [Validators.required, Validators.email]]
  });

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading.set(true);
    forkJoin({
      customers: this.adminService.getPendingCustomers(),
      staff: this.adminService.getAllStaff()
    }).pipe(finalize(() => this.isLoading.set(false))).subscribe({
      next: ({ customers, staff }) => {
        this.pendingCustomers.set(customers);
        this.staff.set(staff);
        this.lastRefreshed.set(new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }));
      },
      error: () => this.error.set('Dashboard data could not be refreshed.')
    });
  }

  selectCustomer(c: CorporateCustomer): void {
    this.selectedCustomer.set(c);
    this.reviewReason.set('');
  }

  approveCustomer(): void {
    this.pendingReviewAction.set('APPROVE');
  }

  rejectCustomer(): void {
    this.pendingReviewAction.set('REJECT');
  }

  cancelReviewAction(): void {
    this.pendingReviewAction.set(null);
  }

  confirmReviewAction(): void {
    const c = this.selectedCustomer();
    if (!c?.id) return;
    const approve = this.pendingReviewAction() === 'APPROVE';
    this.isReviewing.set(true);
    this.adminService.reviewCustomer(c.id, approve, approve ? undefined : this.reviewReason()).pipe(finalize(() => this.isReviewing.set(false))).subscribe({
      next: () => { this.message.set(approve ? 'Customer approved.' : 'Customer rejected.'); this.pendingReviewAction.set(null); this.selectedCustomer.set(null); this.loadData(); },
      error: (err) => this.error.set(err.error?.message ?? 'Review failed.')
    });
  }

  statusClass(status?: string): string {
    return statusBadgeClass(status);
  }

  statusText(status?: string): string {
    return statusLabel(status);
  }

  onboardStaff(): void {
    if (this.staffForm.invalid) return;
    this.staffError.set('');
    this.isOnboarding.set(true);
    const { employeeEmail, ...staff } = this.staffForm.value;
    this.adminService.onboardStaff(staff as LoanOfficer, employeeEmail!).pipe(finalize(() => this.isOnboarding.set(false))).subscribe({
      next: () => { this.message.set('Staff onboarded.'); this.staffForm.reset({ role: 'OFFICER' }); this.loadData(); },
      error: (err) => this.staffError.set(err.error?.message ?? 'Staff onboarding failed.')
    });
  }

  closeStaffError(): void {
    this.staffError.set('');
  }

  logout(): void {
    this.auth.logout().subscribe();
  }
}
