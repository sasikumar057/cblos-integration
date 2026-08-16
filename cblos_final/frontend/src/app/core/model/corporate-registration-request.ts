export interface PrimaryContactRequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  designation: string;
}

export interface CorporateRegistrationRequest {
  companyName: string;
  taxId: string;
  companyEmail: string;
  phoneNumber: string;
  businessAddress: string;
  industryType: string;
  password: string;
  primaryContact: PrimaryContactRequest;
}