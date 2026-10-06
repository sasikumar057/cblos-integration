package com.cblos.Integration.salesforce.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SalesforceLoanApplicationRequest(
        @JsonProperty("Loan_Amount__c") BigDecimal loanAmount,
        @JsonProperty("Requested_Tenure_Months__c") Integer loanTerm,
        @JsonProperty("Loan_Product_Name__c") String loanProductName,
        @JsonProperty("Customer_Account__c") String customerAccountId,
        @JsonProperty("Application_Status__c") String status,
        @JsonProperty("Application_Date__c") LocalDate date
) {
}
