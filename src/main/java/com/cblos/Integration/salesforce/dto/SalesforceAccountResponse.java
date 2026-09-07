package com.cblos.Integration.salesforce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SalesforceAccountResponse(

        @JsonProperty("Id") String id,

        @JsonProperty("Name") String name,

        @JsonProperty("Phone") String phone,

        @JsonProperty("CBLOS_Customer_ID__c") String cblosCustomerId

) {
}