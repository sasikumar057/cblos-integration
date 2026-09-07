export function statusLabel(status?: string): string {
  return (status ?? 'UNKNOWN')
    .replace(/_/g, ' ')
    .toLowerCase()
    .replace(/\b\w/g, char => char.toUpperCase());
}

export function statusBadgeClass(status?: string): string {
  switch ((status ?? '').toUpperCase()) {
    case 'ACTIVE':
    case 'APPROVED':
      return 'status-badge status-approved';
    case 'UNDER_REVIEW':
      return 'status-badge status-review';
    case 'PENDING_MANAGER_APPROVAL':
      return 'status-badge status-manager';
    case 'DOCUMENT_PENDING':
    case 'PENDING':
    case 'PENDING_VERIFICATION':
      return 'status-badge status-pending';
    case 'REJECTED':
    case 'REJECTED_INVALID_DOCUMENTS':
      return 'status-badge status-rejected';
    case 'WITHDRAWN':
      return 'status-badge status-withdrawn';
    default:
      return 'status-badge';
  }
}
