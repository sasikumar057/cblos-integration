package com.cblos.Integration.salesforce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SalesforceContactRequest(
 
    @JsonProperty("FirstName") String firstName,

    @JsonProperty("LastName") String LastName,

    @JsonProperty("AccountId") String accountId){
}
