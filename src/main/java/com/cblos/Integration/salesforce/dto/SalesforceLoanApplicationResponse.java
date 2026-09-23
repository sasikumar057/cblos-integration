package com.cblos.Integration.salesforce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SalesforceLoanApplicationResponse(
                @JsonProperty("Id") String id) {
}
