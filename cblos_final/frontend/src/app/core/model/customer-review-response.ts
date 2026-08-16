export interface PrimaryContactResponse {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string | null;
  designation: string | null;
}

export interface CustomerReviewResponse {
  id: number;
  companyName: string;
  taxId: string;
  companyEmail: string;
  phoneNumber: string | null;
  businessAddress: string | null;
  industryType: string | null;
  status: string;
  rejectionReason: string | null;
  primaryContact: PrimaryContactResponse | null;
}