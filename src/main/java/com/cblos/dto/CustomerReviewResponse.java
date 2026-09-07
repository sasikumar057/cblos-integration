package com.cblos.dto;

public record CustomerReviewResponse(
        Integer id,
        String companyName,
        String taxId,
        String companyEmail,
        String phoneNumber,
        String businessAddress,
        String industryType,
        String status,
        String rejectionReason,
        PrimaryContactResponse primaryContact) {
}