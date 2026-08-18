package com.cblos.Integration.salesforce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SalesforceAccountRequest(
        @JsonProperty("Name") String name,

        @JsonProperty("Phone") String phone) {
}