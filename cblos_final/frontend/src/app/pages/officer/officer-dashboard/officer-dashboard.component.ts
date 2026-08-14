import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LoanService, LoanApplication } from '../../../core/services/loan.service';
import { CollateralService, Collateral } from '../../../core/services/collateral.service';
import { DocumentService, DocumentSummary } from '../../../core/services/document.service';
import { CreditService } from '../../../core/services/credit.service';
import { DecimalPipe } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { finalize, forkJoin } from 'rxjs';
import { buildLoanTimeline, LoanTimeline } from '../../../core/utils/loan-timeline';
import { buildCreditIntelligence, CreditIntelligence } from '../../../core/utils/credit-intelligence';
import { statusBadgeClass, statusLabel } from '../../../core/utils/status-ui';

@Component({
  selector: 'app-officer-dashboard',
  standalone: true,
  imports: [DecimalPipe],
  templateUrl: './officer-dashboard.component.html',
  styleUrl: './officer-dashboard.component.css'
})
export class OfficerDashboardComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly auth = inject(AuthService);
  private readonly loanService = inject(LoanService);
  private readonly collateralService = inject(CollateralService);
  private readonly documentService = inject(DocumentService);
  private readonly creditService = inject(CreditService);
  private readonly sanitizer = inject(DomSanitizer);

  officerId = 0;
  queue = signal<LoanApplication[]>([]);
  myAssignedLoans = signal<LoanApplication[]>([]); // Added for your assigned applications
  selectedApp = signal<LoanApplication | null>(null);
  collateral = signal<Collateral[]>([]);
  documents = signal<DocumentSummary[]>([]);
  riskScore = signal<number | null>(null);
  docUrl = signal<SafeResourceUrl | null>(null);
  selectedDocumentId = signal<number | null>(null);
  selectedDocumentName = signal('');
  requiredDocuments = [
    { type: 'COLLATERAL_PROOF', label: 'Collateral Proof', icon: 'bi-house-lock' },
    { type: 'TAX_RETURN', label: 'Tax Return', icon: 'bi-receipt' },
    { type: 'BUSINESS_LICENSE', label: 'Business License', icon: 'bi-patch-check' }
  ];
  private currentDocumentUrl = '';

  creditScore = 700;
  riskNotes = '';
  message = signal('');
  error = signal('');
  isLoading = signal(false);
  isDetailsLoading = signal(false);
  isEvaluating = signal(false);
  queueSearch = signal('');
  lastRefreshed = signal('');
  pendingEvaluation = signal<boolean | null>(null);

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
    sharedQueue: this.queue().length,
    myTasks: this.myAssignedLoans().length, // Tracks your own workload
    selected: this.selectedApp() ? 1 : 0,
    documentsReady: this.selectedApp() ? this.uploadedRequiredDocumentCount() : 0,
    highRisk: this.queue().filter(app => (app.loanAmount ?? 0) >= 10000000).length
  }));

  ngOnInit(): void {
    this.officerId = Number(this.route.snapshot.paramMap.get('officerId'));
    this.loadQueue();
    const selectedId = this.route.snapshot.queryParamMap.get('selectedAppId');
    if (selectedId) this.selectApp(+selectedId);
  }
  
  
  loadQueue(): void {
  // 🟢 1. Turn loading state ON
  this.isLoading.set(true);
  this.error.set(''); // Clear any previous error messages safely
  
  // 2. Load My Assigned Loans
  this.loanService.getByOfficer(this.officerId).pipe(
    // 🟢 3. This block is GUARANTEED to run when the HTTP call completes (success OR error)
    finalize(() => this.isLoading.set(false))
  ).subscribe({
    next: assignedApps => {
      this.myAssignedLoans.set(assignedApps);
      this.queue.set(assignedApps); // Keeps your search and computation pipelines functional
      this.lastRefreshed.set(new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }));
    },
    error: (err) => {
      console.error('Could not load assigned loans.', err);
      this.error.set('Failed to refresh your dashboard workspace queue.');
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
      documents: this.documentService.listByApplication(id),
      riskScore: this.creditService.getRiskScore(id)
    }).pipe(finalize(() => this.isDetailsLoading.set(false))).subscribe({
      next: ({ app, collateral, documents, riskScore }) => {
        this.selectedApp.set(app);
        this.collateral.set(collateral);
        this.documents.set(documents);
        this.riskScore.set(riskScore);
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

  evaluate(pass: boolean): void {
    this.pendingEvaluation.set(pass);
  }

  cancelEvaluation(): void {
    this.pendingEvaluation.set(null);
  }

  confirmEvaluation(): void {
    const pass = this.pendingEvaluation();
    const app = this.selectedApp();
    if (!app?.applicationId || pass === null) return;
    this.isEvaluating.set(true);
    this.loanService.officerReview(app.applicationId, this.creditScore, this.riskNotes, pass)
      .pipe(finalize(() => this.isEvaluating.set(false))).subscribe({
      next: () => { 
        this.message.set(pass ? 'Application escalated.' : 'Application rejected.'); 
        this.pendingEvaluation.set(null); 
        this.selectedApp.set(null); 
        this.loadQueue(); 
      },
      error: () => this.error.set('Evaluation failed.')
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
}