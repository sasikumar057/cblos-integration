import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CustomerService, CorporateCustomer } from '../../../core/services/customer.service';
import { LoanService, LoanApplication } from '../../../core/services/loan.service';
import { LoanProductService, LoanProduct } from '../../../core/services/loan-product.service';
import { LoanAccountService, LoanAccount } from '../../../core/services/loan-account.service';
import { CollateralService, Collateral } from '../../../core/services/collateral.service';
import { DocumentService, DocumentSummary } from '../../../core/services/document.service';
import { DecimalPipe } from '@angular/common';
import { catchError, finalize, forkJoin, map, of } from 'rxjs';
import { buildLoanTimeline, LoanTimeline } from '../../../core/utils/loan-timeline';
import { buildCreditIntelligence, CreditIntelligence } from '../../../core/utils/credit-intelligence';
import { statusBadgeClass, statusLabel } from '../../../core/utils/status-ui';

@Component({
  selector: 'app-customer-dashboard',
  standalone: true,
  imports: [RouterLink, DecimalPipe],
  templateUrl: './customer-dashboard.component.html',
  styleUrl: './customer-dashboard.component.css'
})
export class CustomerDashboardComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private readonly customerService = inject(CustomerService);
  private readonly loanService = inject(LoanService);
  private readonly productService = inject(LoanProductService);
  private readonly accountService = inject(LoanAccountService);
  private readonly collateralService = inject(CollateralService);
  private readonly documentService = inject(DocumentService);

  customerId = 0;
  customer = signal<CorporateCustomer | null>(null);
  products = signal<LoanProduct[]>([]);
  activeApplications = signal<LoanApplication[]>([]);
  approvedLoans = signal<LoanAccount[]>([]);
  isLoading = signal(false);
  withdrawingId = signal<number | null>(null);
  message = signal('');
  error = signal('');
  productSearch = signal('');
  applicationStatusFilter = signal('ALL');
  selectedApplication = signal<LoanApplication | null>(null);
  applicationReadiness = signal<Record<number, { collateral: Collateral[]; documents: DocumentSummary[] }>>({});
  lastRefreshed = signal('');
  pendingWithdrawApp = signal<LoanApplication | null>(null);
  readonly requiredDocumentTypes = ['COLLATERAL_PROOF', 'TAX_RETURN', 'BUSINESS_LICENSE'];

  filteredProducts = computed(() => {
    const term = this.productSearch().trim().toLowerCase();
    if (!term) return this.products();
    return this.products().filter(product =>
      [product.productName, product.description]
        .some(value => (value ?? '').toLowerCase().includes(term))
    );
  });

  applicationStatuses = computed(() =>
    Array.from(new Set(this.activeApplications()
      .map(app => app.status)
      .filter((status): status is string => !!status)))
  );

  filteredApplications = computed(() => {
    const status = this.applicationStatusFilter();
    if (status === 'ALL') return this.activeApplications();
    return this.activeApplications().filter(app => app.status === status);
  });

  dashboardStats = computed(() => {
    const apps = this.activeApplications();
    return {
      total: apps.length,
      underReview: apps.filter(app => app.status === 'UNDER_REVIEW').length,
      rejected: apps.filter(app => app.status === 'REJECTED').length,
      activeLoans: this.approvedLoans().length
    };
  });

  ngOnInit(): void {
    this.customerId = Number(this.route.snapshot.paramMap.get('customerId'));
    this.refresh();
  }

  refresh(): void {
    this.error.set('');
    this.message.set('');
    this.isLoading.set(true);
    forkJoin({
      customer: this.customerService.getById(this.customerId),
      products: this.productService.getCatalog(),
      applications: this.loanService.getByCustomer(this.customerId),
      loans: this.accountService.getByCustomer(this.customerId)
    }).pipe(finalize(() => this.isLoading.set(false))).subscribe({
      next: ({ customer, products, applications, loans }) => {
        this.customer.set(customer);
        this.products.set(products);
        this.activeApplications.set(applications);
        this.approvedLoans.set(loans);
        this.lastRefreshed.set(new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }));
        this.selectedApplication.update(current =>
          applications.find(app => app.applicationId === current?.applicationId) ?? applications[0] ?? null
        );
        this.loadApplicationReadiness(applications);
      },
      error: () => this.error.set('Dashboard data could not be refreshed.')
    });
  }

  apply(productName: string): void {
    this.router.navigate(['/customer/dashboard', this.customerId, 'apply'], {
      queryParams: { productType: productName }
    });
  }

  selectApplication(app: LoanApplication): void {
    this.selectedApplication.set(app);
  }

  timelineFor(app: LoanApplication): LoanTimeline {
    const readiness = this.readinessFor(app);
    return buildLoanTimeline(app, {
      collateralCount: readiness.collateral.length,
      documentCount: readiness.documents.length,
      requiredDocumentCount: this.uploadedRequiredDocumentCount(app),
      requiredDocumentsTotal: this.requiredDocumentTypes.length
    });
  }

  intelligenceFor(app: LoanApplication): CreditIntelligence {
    const readiness = this.readinessFor(app);
    return buildCreditIntelligence(app, readiness.collateral, readiness.documents);
  }

  readinessFor(app: LoanApplication): { collateral: Collateral[]; documents: DocumentSummary[] } {
    const id = app.applicationId ?? 0;
    return this.applicationReadiness()[id] ?? { collateral: [], documents: [] };
  }

  uploadedRequiredDocumentCount(app: LoanApplication): number {
    const uploadedTypes = new Set(this.readinessFor(app).documents.map(doc => (doc.documentType ?? '').toUpperCase()));
    return this.requiredDocumentTypes.filter(type => uploadedTypes.has(type)).length;
  }

  continueApplication(app: LoanApplication): void {
    const id = app.applicationId;
    if (!id) return;

    const readiness = this.readinessFor(app);
    if (readiness.collateral.length === 0) {
      this.router.navigate(['/customer/dashboard/application', id, 'collateral']);
      return;
    }

    if (this.uploadedRequiredDocumentCount(app) < this.requiredDocumentTypes.length) {
      this.router.navigate(['/customer/dashboard/application', id, 'document']);
      return;
    }

    this.selectApplication(app);
  }

  canContinue(app: LoanApplication): boolean {
    const status = (app.status ?? '').toUpperCase();
    return !['UNDER_REVIEW', 'PENDING_MANAGER_APPROVAL', 'APPROVED', 'REJECTED'].includes(status)
      || this.uploadedRequiredDocumentCount(app) < this.requiredDocumentTypes.length;
  }

  continueLabel(app: LoanApplication): string {
    if (this.readinessFor(app).collateral.length === 0) return 'Add Collateral';
    if (this.uploadedRequiredDocumentCount(app) < this.requiredDocumentTypes.length) return 'Upload Docs';
    return 'View Timeline';
  }

  withdrawLabel(app: LoanApplication): string {
    return (app.status ?? '').toUpperCase() === 'REJECTED' ? 'Clear Rejected' : 'Withdraw';
  }

  canWithdraw(app: LoanApplication): boolean {
    const status = (app.status ?? '').toUpperCase();
    return !!app.applicationId && !['APPROVED', 'WITHDRAWN'].includes(status);
  } 

  withdrawApplication(app: LoanApplication): void {
    const id = app.applicationId;
    if (!id || !this.canWithdraw(app)) return;
    this.pendingWithdrawApp.set(app);
  }

  cancelWithdraw(): void {
    this.pendingWithdrawApp.set(null);
  }

confirmWithdraw(): void {
  const app = this.pendingWithdrawApp();
  const id = app?.applicationId;
  if (!app || !id || !this.canWithdraw(app)) return;

  this.error.set('');
  this.message.set('');
  this.withdrawingId.set(id);

  this.loanService.withdraw(id).pipe(
    finalize(() => {
      this.withdrawingId.set(null);
      this.pendingWithdrawApp.set(null); // Force close the modal panel safely
    })
  ).subscribe({
    next: () => {
      // 1. Drop it from active pipelines
      const remaining = this.activeApplications().filter(item => item.applicationId !== id);
      this.activeApplications.set(remaining);

      // 2. Clean out readiness states safely 
      const { [id]: removed, ...readiness } = this.applicationReadiness();
      this.applicationReadiness.set(readiness);

      // 3. Clear active selections cleanly to avoid reactivity loop crashes
      this.selectedApplication.set(null); 
      
      this.message.set(`Application #${id} successfully withdrawn.`);
    },
    error: (err) => this.error.set(err.error?.message ?? 'Application withdrawal failed.')
  });
}

  statusClass(status?: string): string {
    return statusBadgeClass(status);
  }

  statusText(status?: string): string {
    return statusLabel(status);
  }

  private loadApplicationReadiness(applications: LoanApplication[]): void {
    const appsWithIds = applications.filter(app => !!app.applicationId);
    if (appsWithIds.length === 0) {
      this.applicationReadiness.set({});
      return;
    }

    forkJoin(appsWithIds.map(app => {
      const id = app.applicationId!;
      return forkJoin({
        collateral: this.collateralService.getByApplication(id),
        documents: this.documentService.listByApplication(id)
      }).pipe(
        map(readiness => [id, readiness] as const),
        catchError(() => of([id, { collateral: [], documents: [] }] as const))
      );
    })).subscribe(entries => {
      this.applicationReadiness.set(Object.fromEntries(entries));
      this.loanService.getByCustomer(this.customerId).subscribe(refreshedApplications => {
        this.activeApplications.set(refreshedApplications);
        this.selectedApplication.update(current =>
          refreshedApplications.find(app => app.applicationId === current?.applicationId)
            ?? refreshedApplications[0]
            ?? null
        );
      });
    });
  }

  logout(): void {
    this.auth.logout().subscribe();
  }
}
