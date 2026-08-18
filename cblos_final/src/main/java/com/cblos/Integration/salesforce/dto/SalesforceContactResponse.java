package com.cblos.Integration.salesforce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SalesforceContactResponse(

        @JsonProperty("Id") String id,

        @JsonProperty("FirstName") String firstName,

        @JsonProperty("LastName") String lastName,

        @JsonProperty("CBLOS_Contact_ID__c") String cblosContactId,

        @JsonProperty("AccountId") String accountId

) {
}
