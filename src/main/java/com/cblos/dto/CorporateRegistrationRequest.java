package com.cblos.dto;

public record CorporateRegistrationRequest(
    String companyName,
    String taxId,
    String companyEmail,
    String phoneNumber,
    String businessAddress,
    String industryType,
    String password,
    PrimaryContactRequest primaryContact
) {
    
}
