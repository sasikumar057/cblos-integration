package com.cblos.Integration.salesforce.config;

import com.cblos.Integration.salesforce.dto.SalesforceAccountResponse;
import com.cblos.Integration.salesforce.service.SalesforceAccountSyncService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("salesforce-account-test")
public class SalesforceAccountSyncTest
        implements CommandLineRunner {

    private final SalesforceAccountSyncService accountSyncService;
    private final Integer customerId;

    public SalesforceAccountSyncTest(
            SalesforceAccountSyncService accountSyncService,
            @Value("${salesforce.test-customer-id}") Integer customerId) {

        this.accountSyncService = accountSyncService;
        this.customerId = customerId;
    }

    @Override
    public void run(String... args) {

        SalesforceAccountResponse response = accountSyncService.syncAccount(customerId);

        System.out.println(
                "Salesforce Account synchronization successful.");

        System.out.println(
                "Salesforce Account ID: " + response.id());

        System.out.println(
                "Salesforce Account Name: " + response.name());
    }
}