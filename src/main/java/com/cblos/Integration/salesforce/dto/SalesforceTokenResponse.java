package com.cblos.Integration.salesforce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SalesforceTokenResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("instance_url") String instanceUrl,
    @JsonProperty("token_type") String tokenType,
    @JsonProperty("issued_at") String issuedAt,
    String scope
){
    
}