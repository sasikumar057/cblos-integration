package com.cblos.Integration.salesforce.client;

import com.cblos.Integration.salesforce.dto.SalesforceContactRequest;
import com.cblos.Integration.salesforce.dto.SalesforceContactResponse;
import com.cblos.Integration.salesforce.dto.SalesforceTokenResponse;
import com.cblos.Integration.salesforce.service.SalesforceAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SalesforceContactClient {

    private final RestClient restClient;
    private final SalesforceAuthService authService;
    private final String apiVersion;

    public SalesforceContactClient(
            RestClient.Builder restClientBuilder,
            SalesforceAuthService authService,
            @Value("${salesforce.api-version}") String apiVersion) {

        this.restClient = restClientBuilder.build();
        this.authService = authService;
        this.apiVersion = apiVersion;
    }

    public SalesforceContactResponse upsertContact(
            Integer costomerContactId,
            SalesforceContactRequest contactRequest) {

        if (costomerContactId == null) {
            throw new IllegalArgumentException(
                    "CBLOS Customer Contact ID is required.");
        }
        if (contactRequest == null) {
            throw new IllegalArgumentException(
                    "Salesforce Contact data is required.");

        }
        SalesforceTokenResponse tokenResponse = authService.getAccessToken();

        String externalIdValue = costomerContactId.toString();

        String contactUrl = tokenResponse.instanceUrl()
                + "/services/data/"
                + apiVersion
                + "/sobjects/Contact/"
                + "CBLOS_Contact_ID__c/"
                + externalIdValue;

        restClient.patch()
                .uri(contactUrl)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokenResponse.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(contactRequest)
                .retrieve()
                .toBodilessEntity();

        SalesforceContactResponse contactResponse = restClient.get()
                .uri(contactUrl)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokenResponse.accessToken())
                .retrieve()
                .body(SalesforceContactResponse.class);

        if (contactResponse == null || contactResponse.id() == null
                || contactResponse.id().isBlank()) {
            throw new IllegalStateException(
                    "Salesforce Contact upsert failed for CBLOS Customer Contact ID: "
                            + costomerContactId);
        }
        return contactResponse;
    }
}
