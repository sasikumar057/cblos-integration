package com.cblos.Integration.salesforce.service;

import com.cblos.Integration.salesforce.dto.SalesforceTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class SalesforceAuthService {

    private final RestClient restClient;
    private final String loginUrl;
    private final String clientId;
    private final String clientSecret;

    public SalesforceAuthService(
            RestClient.Builder restClientBuilder,
            @Value("${salesforce.login-url}") String loginUrl,
            @Value("${salesforce.client-id}") String clientId,
            @Value("${salesforce.client-secret}") String clientSecret) {

        this.restClient = restClientBuilder.build();
        this.loginUrl = loginUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public SalesforceTokenResponse getAccessToken() {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("grant_type", "client_credentials");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        SalesforceTokenResponse response = restClient.post()
                .uri(loginUrl + "/services/oauth2/token")
                .contentType(
                        MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(SalesforceTokenResponse.class);

        if (response == null
                || response.accessToken() == null
                || response.accessToken().isBlank()) {

            throw new IllegalStateException(
                    "Salesforce authentication returned no access token.");
        }

        return response;
    }
}