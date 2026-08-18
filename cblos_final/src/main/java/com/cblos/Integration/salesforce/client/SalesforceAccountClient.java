package com.cblos.Integration.salesforce.client;

import com.cblos.Integration.salesforce.dto.SalesforceAccountRequest;
import com.cblos.Integration.salesforce.dto.SalesforceAccountResponse;
import com.cblos.Integration.salesforce.dto.SalesforceTokenResponse;
import com.cblos.Integration.salesforce.service.SalesforceAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SalesforceAccountClient {

    private final RestClient restClient;
    private final SalesforceAuthService authService;
    private final String apiVersion;

    public SalesforceAccountClient(
            RestClient.Builder restClientBuilder,
            SalesforceAuthService authService,
            @Value("${salesforce.api-version}") String apiVersion) {

        this.restClient = restClientBuilder.build();
        this.authService = authService;
        this.apiVersion = apiVersion;
    }

    public SalesforceAccountResponse upsertAccount(
            Integer customerId,
            SalesforceAccountRequest accountRequest) {

        if (customerId == null) {
            throw new IllegalArgumentException(
                    "CBLOS Customer ID is required.");
        }

        if (accountRequest == null) {
            throw new IllegalArgumentException(
                    "Salesforce Account data is required.");
        }

        SalesforceTokenResponse tokenResponse = authService.getAccessToken();

        String externalIdValue = customerId.toString();

        String accountUrl = tokenResponse.instanceUrl()
                + "/services/data/"
                + apiVersion
                + "/sobjects/Account/"
                + "CBLOS_Customer_ID__c/"
                + externalIdValue;

        /*
         * PATCH performs the upsert.
         *
         * If the External ID does not exist:
         * Salesforce creates an Account.
         *
         * If the External ID already exists:
         * Salesforce updates that Account.
         */
        restClient.patch()
                .uri(accountUrl)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokenResponse.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(accountRequest)
                .retrieve()
                .toBodilessEntity();

        /*
         * Retrieve the Account by the same External ID.
         * This gives us the Salesforce Account ID for
         * both create and update scenarios.
         */
        SalesforceAccountResponse accountResponse = restClient.get()
                .uri(accountUrl)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer "
                                + tokenResponse.accessToken())
                .retrieve()
                .body(SalesforceAccountResponse.class);

        if (accountResponse == null
                || accountResponse.id() == null
                || accountResponse.id().isBlank()) {

            throw new IllegalStateException(
                    "Salesforce Account upsert completed, "
                            + "but no Account ID was returned.");
        }

        return accountResponse;
    }
}