import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LoanService, LoanApplication } from '../../../core/services/loan.service';
import { CollateralService, Collateral } from '../../../core/services/collateral.service';
import { DocumentService, DocumentSummary } from '../../../core/services/document.service';
import { DatePipe, DecimalPipe } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { finalize, forkJoin } from 'rxjs';
import { buildLoanTimeline, LoanTimeline } from '../../../core/utils/loan-timeline';
import { buildCreditIntelligence, CreditIntelligence } from '../../../core/utils/credit-intelligence';
import { statusBadgeClass, statusLabel } from '../../../core/utils/status-ui';
import { AccountRepaymentSummary, ManagerPortfolioService, ManagerPortfolioSummary } from '../../../core/services/manager-portfolio.service';

@Component({
  selector: 'app-manager-dashboard',
  standalone: true,
  imports: [DatePipe, DecimalPipe],
  templateUrl: './manager-dashboard.component.html',
  styleUrl: './manager-dashboard.component.css'
})
export class ManagerDashboardComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly auth = inject(AuthService);
  private readonly loanService = inject(LoanService);
  private readonly collateralService = inject(CollateralService);
  private readonly documentService = inject(DocumentService);
  private readonly portfolioService = inject(ManagerPortfolioService);
  private readonly sanitizer = inject(DomSanitizer);

  managerId = 0;
  queue = signal<LoanApplication[]>([]);
  selectedApp = signal<LoanApplication | null>(null);
  collateral = signal<Collateral[]>([]);
  documents = signal<DocumentSummary[]>([]);
  docUrl = signal<SafeResourceUrl | null>(null);
  selectedDocumentId = signal<number | null>(null);
  selectedDocumentName = signal('');
  requiredDocuments = [
    { type: 'COLLATERAL_PROOF', label: 'Collateral Proof', icon: 'bi-house-lock' },
    { type: 'TAX_RETURN', label: 'Tax Return', icon: 'bi-receipt' },
    { type: 'BUSINESS_LICENSE', label: 'Business License', icon: 'bi-patch-check' }
  ];
  private currentDocumentUrl = '';
  managerNotes = '';
  message = signal('');
  error = signal('');
  isLoading = signal(false);
  isDetailsLoading = signal(false);
  isPortfolioLoading = signal(false);
  isAuthorizing = signal(false);
  queueSearch = signal('');
  portfolioSearch = signal('');
  lastRefreshed = signal('');
  portfolio = signal<ManagerPortfolioSummary>({
    totalDisbursed: 0,
    totalPaidBack: 0,
    totalRemaining: 0,
    activeAccountCount: 0,
    accounts: []
  });
  pendingAuthorization = signal<boolean | null>(null);
  private portfolioRefreshTimer: ReturnType<typeof setInterval> | null = null;

  filteredQueue = computed(() => {
    const term = this.queueSearch().trim().toLowerCase();
    if (!term) return this.queue();
    return this.queue().filter(app =>
      [
        app.applicationId?.toString(),
        app.loanType,
        app.status,
        app.customer?.companyName,
        app.loanAmount?.toString()
      ].some(value => (value ?? '').toLowerCase().includes(term))
    );
  });

  queueStats = computed(() => ({
    approvals: this.queue().length,
    selected: this.selectedApp() ? 1 : 0,
    documentsReady: this.selectedApp() ? this.uploadedRequiredDocumentCount() : 0,
    highExposure: this.queue().filter(app => (app.loanAmount ?? 0) >= 10000000).length
  }));

portfolioStats = computed(() => {
  const portfolio = this.portfolio();
  const accounts = portfolio.accounts ?? [];

  let aggregateDisbursed = 0;
  let aggregatePaidBack = 0;
  let aggregateRemaining = 0;
  let activeCount = 0;

  accounts.forEach(account => {
    const status = (account.accountStatus ?? '').toUpperCase();
    
    // Convert to explicit numbers safely
    const disbursed = Number(account.disbursedAmount ?? 0);
    const paid = Number(account.paidBackAmount ?? 0);
    const remaining = Number(account.remainingAmount ?? 0);

    // RULE 1: If account is fully settled/closed early
    if (status === 'PRE_CLOSED' || status === 'CLOSED' || status === 'SETTLED' || status === 'SETTLED_CLOSED') {
      aggregateDisbursed += disbursed;
      // Use the actual historical paid back amount instead of forcing a fallback value
      aggregatePaidBack += paid > 0 ? paid : disbursed; 
      aggregateRemaining += 0; 
    } 
    // RULE 2: Standard active monthly tracking loan flow
    else {
      aggregateDisbursed += disbursed;
      aggregatePaidBack += paid;
      
      const realRemaining = remaining > 0 ? remaining : Math.max(0, disbursed - paid);
      aggregateRemaining += realRemaining;
      
      if (realRemaining > 0) {
        activeCount++;
      }
    }
  });

  // Calculate safe coverage percentages
  const coverage = aggregateDisbursed > 0 
    ? Math.round((aggregatePaidBack / aggregateDisbursed) * 100) 
    : 0;
    
  const outstanding = aggregateDisbursed > 0 
    ? Math.round((aggregateRemaining / aggregateDisbursed) * 100) 
    : 0;

  return {
    totalDisbursed: aggregateDisbursed,
    totalPaidBack: aggregatePaidBack,
    totalRemaining: aggregateRemaining,
    nearestDueDate: portfolio.nearestDueDate,
    activeAccountCount: activeCount, 
    repaymentCoverage: coverage,     
    outstandingRatio: outstanding
  };
});

  filteredPortfolioAccounts = computed(() => {
    const term = this.portfolioSearch().trim().toLowerCase();
    const accounts = this.portfolio().accounts ?? [];
    if (!term) return accounts;
    return accounts.filter(account =>
      [
        account.accountNumber,
        account.customerName,
        account.loanType,
        account.accountStatus,
        account.applicationId?.toString(),
        account.disbursedAmount?.toString(),
        account.remainingAmount?.toString()
      ].some(value => (value ?? '').toLowerCase().includes(term))
    );
  });

  ngOnInit(): void {
    this.managerId = Number(this.route.snapshot.paramMap.get('managerId'));
    this.loadQueue();
    this.portfolioRefreshTimer = setInterval(() => this.loadPortfolio(true), 15000);
    const selectedId = this.route.snapshot.queryParamMap.get('selectedAppId');
    if (selectedId) this.selectApp(+selectedId);
  }

  ngOnDestroy(): void {
    if (this.portfolioRefreshTimer) clearInterval(this.portfolioRefreshTimer);
  }

  loadQueue(): void {
    this.isLoading.set(true);
    forkJoin({
      apps: this.loanService.getByManager(this.managerId),
      portfolio: this.portfolioService.getSummary()
    }).pipe(finalize(() => this.isLoading.set(false))).subscribe({
      next: ({ apps, portfolio }) => {
        this.queue.set(apps.filter(a => a.status === 'PENDING_MANAGER_APPROVAL'));
        this.portfolio.set({
          ...portfolio,
          accounts: portfolio.accounts ?? []
        });
        this.lastRefreshed.set(new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }));
      },
      error: () => this.error.set('Manager data could not be refreshed.')
    });
  }

  loadPortfolio(silent = false): void {
    if (!silent) this.isPortfolioLoading.set(true);
    this.portfolioService.getSummary()
      .pipe(finalize(() => {
        if (!silent) this.isPortfolioLoading.set(false);
      }))
      .subscribe({
        next: portfolio => {
          this.portfolio.set({
            ...portfolio,
            accounts: portfolio.accounts ?? []
          });
        },
        error: () => {
          if (!silent) this.error.set('Repayment monitor could not be refreshed.');
        }
      });
  }

  selectApp(id: number): void {
    this.docUrl.set(null);
    this.selectedDocumentId.set(null);
    this.selectedDocumentName.set('');
    this.isDetailsLoading.set(true);
    forkJoin({
      app: this.loanService.getById(id),
      collateral: this.collateralService.getByApplication(id),
      documents: this.documentService.listByApplication(id)
    }).pipe(finalize(() => this.isDetailsLoading.set(false))).subscribe({
      next: ({ app, collateral, documents }) => {
        this.selectedApp.set(app);
        this.collateral.set(collateral);
        this.documents.set(documents);
        const firstRequiredDocument = this.requiredDocuments
          .map(required => documents.find(doc => this.normalizedDocumentType(doc) === required.type))
          .find((doc): doc is DocumentSummary => !!doc);
        if (firstRequiredDocument) this.viewDocument(firstRequiredDocument);
      },
      error: () => this.error.set('Application details could not be loaded.')
    });
  }

  documentFor(type: string): DocumentSummary | undefined {
    return this.documents().find(doc => this.normalizedDocumentType(doc) === type);
  }

  uploadedRequiredDocumentCount(): number {
    return this.requiredDocuments.filter(required => this.documentFor(required.type)).length;
  }

  viewDocument(doc: DocumentSummary): void {
    this.error.set('');
    this.selectedDocumentId.set(doc.documentId);
    this.selectedDocumentName.set(doc.fileName);
    this.documentService.download(doc.documentId).subscribe({
      next: blob => {
        if (this.currentDocumentUrl) URL.revokeObjectURL(this.currentDocumentUrl);
        const previewBlob = blob.type ? blob : new Blob([blob], { type: doc.fileType ?? 'application/octet-stream' });
        this.currentDocumentUrl = URL.createObjectURL(previewBlob);
        this.docUrl.set(this.sanitizer.bypassSecurityTrustResourceUrl(this.currentDocumentUrl));
      },
      error: () => this.error.set('Document preview failed.')
    });
  }

  authorize(approve: boolean): void {
    this.pendingAuthorization.set(approve);
  }

  cancelAuthorization(): void {
    this.pendingAuthorization.set(null);
  }

  confirmAuthorization(): void {
    const approve = this.pendingAuthorization();
    const app = this.selectedApp();
    if (!app?.applicationId || approve === null) return;
    this.isAuthorizing.set(true);
    this.loanService.managerApproval(app.applicationId, approve)
      .pipe(finalize(() => this.isAuthorizing.set(false))).subscribe({
      next: () => {
        this.message.set(approve ? 'Loan approved and disbursed.' : 'Loan rejected.');
        this.pendingAuthorization.set(null);
        this.selectedApp.set(null);
        this.loadQueue();
      },
      error: () => this.error.set('Authorization failed.')
    });
  }

  selectedTimeline(): LoanTimeline | null {
    const app = this.selectedApp();
    if (!app) return null;
    return buildLoanTimeline(app, {
      collateralCount: this.collateral().length,
      documentCount: this.documents().length
    });
  }

  selectedIntelligence(): CreditIntelligence | null {
    const app = this.selectedApp();
    if (!app) return null;
    return buildCreditIntelligence(app, this.collateral(), this.documents());
  }

  private normalizedDocumentType(doc: DocumentSummary): string {
    return (doc.documentType ?? '').trim().toUpperCase();
  }

  logout(): void {
    this.auth.logout().subscribe();
  }

  statusClass(status?: string): string {
    return statusBadgeClass(status);
  }

  statusText(status?: string): string {
    return statusLabel(status);
  }

  dueClass(account: AccountRepaymentSummary): string {
    if (!account.nextDueDate || (account.remainingAmount ?? 0) <= 0) return 'status-withdrawn';
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const due = new Date(account.nextDueDate);
    due.setHours(0, 0, 0, 0);
    const diffDays = Math.ceil((due.getTime() - today.getTime()) / 86400000);
    if (diffDays < 0) return 'status-rejected';
    if (diffDays <= 7) return 'status-pending';
    return 'status-review';
  }

  dueText(account: AccountRepaymentSummary): string {
    if (!account.nextDueDate || (account.remainingAmount ?? 0) <= 0) return 'Cleared';
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const due = new Date(account.nextDueDate);
    due.setHours(0, 0, 0, 0);
    const diffDays = Math.ceil((due.getTime() - today.getTime()) / 86400000);
    if (diffDays < 0) return `${Math.abs(diffDays)} day(s) overdue`;
    if (diffDays === 0) return 'Due today';
    return `Due in ${diffDays} day(s)`;
  }
}
