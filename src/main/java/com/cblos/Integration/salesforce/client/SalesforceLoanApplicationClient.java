package com.cblos.Integration.salesforce.client;

import com.cblos.Integration.salesforce.dto.SalesforceLoanApplicationResponse;
import com.cblos.Integration.salesforce.dto.SalesforceLoanApplicationRequest;
import com.cblos.Integration.salesforce.dto.SalesforceTokenResponse;
import com.cblos.Integration.salesforce.service.SalesforceAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SalesforceLoanApplicationClient {

    private final RestClient restClient;
    private final SalesforceAuthService authService;
    private final String apiVersion;

    public SalesforceLoanApplicationClient(
            RestClient.Builder restClientBuilder,
            SalesforceAuthService authService,
            @Value("${salesforce.api-version}") String apiVersion) {

        this.restClient = restClientBuilder.build();
        this.authService = authService;
        this.apiVersion = apiVersion;
    }

    public SalesforceLoanApplicationResponse upsertLoanApplication(
            Integer LoanApplicationId,
            SalesforceLoanApplicationRequest LoanApplicationRequest) {

        if(LoanApplicationId == null){
            throw new IllegalArgumentException("CBLOS Loan Application Required.");
        }
        if(LoanApplicationRequest == null){
            throw new IllegalArgumentException("Salesforce Loan Application data is required.");
        }

        SalesforceTokenResponse tokenResponse = authService.getAccessToken();
        String ExternalIdValue = LoanApplicationId.toString();
        String LoanApplicationUrl  = tokenResponse.instanceUrl()
                + "/services/data/"
                + apiVersion
                + "/sobjects/Loan_Application__c/"
                + "CBLOS_Loan_Application_ID__c/"
                + ExternalIdValue;

        restClient.patch()
                .uri(LoanApplicationUrl)
                .header (HttpHeaders.AUTHORIZATION, 
                    "Bearer " + tokenResponse.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(LoanApplicationRequest)
                .retrieve()
                .toBodilessEntity();

        SalesforceLoanApplicationResponse LoanApplicationResponse = restClient.get()
                .uri(LoanApplicationUrl)
                .header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer "+tokenResponse.accessToken())
                .retrieve()
                .body(SalesforceLoanApplicationResponse.class);
                if(LoanApplicationResponse == null
                    || LoanApplicationResponse.id() == null
                    || LoanApplicationResponse.id().isBlank()){
                            throw new IllegalStateException("Salesforce Loan Application upsert completed," + "but no Loan Application Id was returned. ");
                    }
        return LoanApplicationResponse;
    }
}
