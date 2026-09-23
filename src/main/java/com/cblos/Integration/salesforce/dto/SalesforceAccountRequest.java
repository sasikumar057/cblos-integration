package com.cblos.Integration.salesforce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SalesforceAccountRequest(
        @JsonProperty("Name") String name,

        @JsonProperty("Phone") String phone,

        @JsonProperty("Email__c") String email,

        @JsonProperty("TAXID__c") String taxId,

        @JsonProperty("Industry") String industry,

        @JsonProperty("Business_Address__c") String billingAddress) {
}