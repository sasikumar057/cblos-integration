import { Collateral } from '../services/collateral.service';
import { DocumentSummary } from '../services/document.service';
import { LoanApplication } from '../services/loan.service';

export type RiskBand = 'Low' | 'Moderate' | 'High';
export type CreditRecommendation = 'Approve' | 'Escalate' | 'Request More Docs' | 'Reject';

export interface CreditIntelligence {
  riskBand: RiskBand;
  recommendation: CreditRecommendation;
  exposureAmount: number;
  collateralValue: number;
  collateralCoverage: number | null;
  documentReadiness: string;
  missingDocuments: string[];
  tenureRisk: string;
  summary: string;
  signals: string[];
}

const expectedDocuments = [
  { type: 'COLLATERAL_PROOF', label: 'Collateral Proof' },
  { type: 'TAX_RETURN', label: 'Tax Return' },
  { type: 'BUSINESS_LICENSE', label: 'Business License' }
];

export function buildCreditIntelligence(
  app: LoanApplication,
  collateral: Collateral[] = [],
  documents: DocumentSummary[] = []
): CreditIntelligence {
  const exposureAmount = Number(app.loanAmount ?? 0);
  const collateralValue = collateral.reduce((total, item) => total + Number(item.estimatedValue ?? 0), 0);
  const collateralCoverage = exposureAmount > 0 ? Math.round((collateralValue / exposureAmount) * 100) : null;
  const tenureMonths = Number(app.requestedTenureMonths ?? 0);
  const missingDocuments = findMissingDocuments(documents);
  const uploadedRequiredCount = expectedDocuments.length - missingDocuments.length;
  const documentReadiness = `${uploadedRequiredCount}/${expectedDocuments.length} received`;
  const riskScore = calculateRiskScore(exposureAmount, collateralCoverage, tenureMonths, missingDocuments.length);
  const riskBand = riskScore <= 2 ? 'Low' : riskScore <= 4 ? 'Moderate' : 'High';
  const recommendation = recommendationFor(app.status, riskBand, missingDocuments.length, collateralCoverage);
  const tenureRisk = tenureMonths > 36 ? `Elevated, ${tenureMonths} months` : tenureMonths > 0 ? `Standard, ${tenureMonths} months` : 'Not specified';
  const signals = [
    collateralCoverage === null ? 'Collateral coverage not available' : `Collateral covers ${collateralCoverage}% of exposure`,
    missingDocuments.length === 0 ? 'Core documents received' : `${missingDocuments.length} core document(s) missing`,
    tenureRisk,
    exposureAmount >= 5_000_000 ? 'Large exposure review required' : 'Exposure within standard review band'
  ];

  return {
    riskBand,
    recommendation,
    exposureAmount,
    collateralValue,
    collateralCoverage,
    documentReadiness,
    missingDocuments,
    tenureRisk,
    summary: summaryFor(riskBand, recommendation),
    signals
  };
}

function calculateRiskScore(
  exposureAmount: number,
  collateralCoverage: number | null,
  tenureMonths: number,
  missingDocumentCount: number
): number {
  let score = 0;
  if (exposureAmount >= 10_000_000) score += 2;
  else if (exposureAmount >= 5_000_000) score += 1;

  if (collateralCoverage === null || collateralCoverage < 80) score += 2;
  else if (collateralCoverage < 120) score += 1;

  if (tenureMonths > 36) score += 1;
  if (missingDocumentCount > 1) score += 2;
  else if (missingDocumentCount === 1) score += 1;

  return score;
}

function findMissingDocuments(documents: DocumentSummary[]): string[] {
  const receivedTypes = new Set(documents.map(doc => (doc.documentType ?? '').trim().toUpperCase()));
  return expectedDocuments
    .filter(required => !receivedTypes.has(required.type))
    .map(required => required.label);
}

function recommendationFor(
  status: string | undefined,
  riskBand: RiskBand,
  missingDocumentCount: number,
  collateralCoverage: number | null
): CreditRecommendation {
  if ((status ?? '').toUpperCase() === 'REJECTED') return 'Reject';
  if (missingDocumentCount > 0) return 'Request More Docs';
  if (riskBand === 'High' || (collateralCoverage !== null && collateralCoverage < 80)) return 'Reject';
  if (riskBand === 'Moderate') return 'Escalate';
  return 'Approve';
}

function summaryFor(riskBand: RiskBand, recommendation: CreditRecommendation): string {
  if (recommendation === 'Request More Docs') {
    return 'Application needs document completion before a confident credit decision.';
  }
  if (recommendation === 'Reject') {
    return 'Risk indicators are above the preferred approval threshold.';
  }
  if (riskBand === 'Low') {
    return 'Application is strongly positioned for approval based on available signals.';
  }
  return 'Application is reviewable with manager oversight and documented rationale.';
}
