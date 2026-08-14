import { LoanApplication } from '../services/loan.service';

export type TimelineState = 'completed' | 'current' | 'pending' | 'blocked';
export type SlaState = 'On Time' | 'Due Soon' | 'Overdue' | 'Closed';

export interface TimelineStep {
  label: string;
  state: TimelineState;
  detail: string;
  routeKey?: 'summary' | 'collateral' | 'documents' | 'review';
}

export interface LoanTimeline {
  progress: number;
  sla: SlaState;
  ageDays: number;
  nextAction: string;
  steps: TimelineStep[];
}

const finalStatuses = ['APPROVED', 'REJECTED'];

export function buildLoanTimeline(
  app: LoanApplication,
  options: { collateralCount?: number; documentCount?: number; requiredDocumentCount?: number; requiredDocumentsTotal?: number } = {}
): LoanTimeline {
  const status = (app.status ?? '').toUpperCase();
  const collateralCount = options.collateralCount ?? 0;
  const documentCount = options.documentCount ?? 0;
  const requiredDocumentsTotal = options.requiredDocumentsTotal ?? 3;
  const requiredDocumentCount = options.requiredDocumentCount ?? Math.min(documentCount, requiredDocumentsTotal);
  const hasOfficer = !!app.loanOfficer?.id;
  const hasCollateral = collateralCount > 0;
  const hasDocuments = requiredDocumentCount >= requiredDocumentsTotal || ['UNDER_REVIEW', 'PENDING_MANAGER_APPROVAL', 'APPROVED', 'REJECTED'].includes(status);
  const rejected = status === 'REJECTED';
  const officerReviewed = ['PENDING_MANAGER_APPROVAL', 'APPROVED', 'REJECTED'].includes(status);
  const managerReviewed = ['APPROVED'].includes(status);
  const disbursed = status === 'APPROVED';
  const scoreDetail = app.officerCreditScore ? `Score: ${app.officerCreditScore}` : 'Credit score reviewed';
  const officerDecisionDetail = rejected
    ? `${scoreDetail}. ${app.officerAssessmentNotes || 'Application rejected during officer credit assessment.'}`
    : officerReviewed
      ? 'Officer recommendation complete'
      : 'Officer review pending';

  const rawSteps = [
    { label: 'Submitted', done: !!app.applicationId, detail: app.submissionDate ? `Submitted ${app.submissionDate}` : 'Application captured', routeKey: 'summary' as const },
    { label: 'Collateral', done: hasCollateral, detail: collateralCount > 0 ? `${collateralCount} collateral record(s)` : 'Waiting for collateral', routeKey: 'collateral' as const },
    { label: 'Documents', done: hasDocuments, detail: `${requiredDocumentCount}/${requiredDocumentsTotal} required document(s) uploaded`, routeKey: 'documents' as const },
    { label: 'Assigned', done: hasOfficer || statusAfterSubmission(status), detail: app.loanOfficer?.name ? `Officer: ${app.loanOfficer.name}` : 'Officer assignment pending', routeKey: 'review' as const },
    { label: 'Officer Review', done: officerReviewed, detail: officerDecisionDetail, routeKey: 'review' as const },
    { label: 'Manager Approval', done: managerReviewed, detail: rejected ? 'Not sent to manager after credit rejection' : managerReviewed ? 'Manager decision complete' : 'Manager decision pending', routeKey: 'review' as const },
    { label: 'Disbursement', done: disbursed, detail: disbursed ? 'Loan account activated' : 'Awaiting approval', routeKey: 'review' as const }
  ];

  const completedCount = rawSteps.filter(step => step.done).length;
  const firstPendingIndex = rawSteps.findIndex(step => !step.done);
  const steps = rawSteps.map((step, index): TimelineStep => ({
    label: step.label,
    detail: step.detail,
    routeKey: step.routeKey,
    state: step.done ? 'completed' : rejected ? 'blocked' : index === firstPendingIndex ? 'current' : 'pending'
  }));

  return {
    progress: Math.round((completedCount / rawSteps.length) * 100),
    sla: calculateSla(app.submissionDate, status),
    ageDays: calculateAgeDays(app.submissionDate),
    nextAction: nextActionFor(steps, status),
    steps
  };
}

function statusAfterSubmission(status: string): boolean {
  return ['DOCUMENT_PENDING', 'UNDER_REVIEW', 'PENDING_MANAGER_APPROVAL', 'APPROVED', 'REJECTED'].includes(status);
}

function calculateAgeDays(submissionDate?: string): number {
  if (!submissionDate) return 0;
  const submitted = new Date(`${submissionDate}T00:00:00`);
  if (Number.isNaN(submitted.getTime())) return 0;
  const diff = Date.now() - submitted.getTime();
  return Math.max(0, Math.floor(diff / 86_400_000));
}

function calculateSla(submissionDate: string | undefined, status: string): SlaState {
  if (finalStatuses.includes(status)) return 'Closed';
  const ageDays = calculateAgeDays(submissionDate);
  if (ageDays > 5) return 'Overdue';
  if (ageDays >= 3) return 'Due Soon';
  return 'On Time';
}

function nextActionFor(steps: TimelineStep[], status: string): string {
  if (status === 'APPROVED') return 'Loan approved and ready for repayment servicing.';
  if (status === 'REJECTED') return 'Application closed after rejection.';
  return steps.find(step => step.state === 'current')?.detail ?? 'Workflow is up to date.';
}
