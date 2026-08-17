package com.cblos.Integration.salesforce.config;

import com.cblos.Integration.salesforce.dto.SalesforceTokenResponse;
import com.cblos.Integration.salesforce.service.SalesforceAuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("salesforce-test")
public class SalesforceConnectionTest
        implements CommandLineRunner {

    private final SalesforceAuthService authService;

    public SalesforceConnectionTest(
            SalesforceAuthService authService) {

        this.authService = authService;
    }

    @Override
    public void run(String... args) {

        SalesforceTokenResponse response = authService.getAccessToken();

        System.out.println(
                "Salesforce authentication successful.");

        System.out.println(
                "Salesforce instance: "
                        + response.instanceUrl());

        /*
         * Never print response.accessToken().
         */
    }
}