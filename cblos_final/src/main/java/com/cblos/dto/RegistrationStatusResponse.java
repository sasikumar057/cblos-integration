package com.cblos.dto;

public record RegistrationStatusResponse(
boolean found,
    Integer id,
    String status,
    String companyEmail,
    String companyName,      
    String taxId,           
    String phoneNumber,     
    String industryType,    
    String businessAddress, 
    String rejectionReason,
    String message,
    boolean canLogin
) {
}
