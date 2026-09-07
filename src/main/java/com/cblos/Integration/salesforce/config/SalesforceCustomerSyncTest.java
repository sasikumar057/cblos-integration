package com.cblos.Integration.salesforce.config;

import com.cblos.Integration.salesforce.service.SalesforceCustomerSyncService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("salesforce-customer-test")
public class SalesforceCustomerSyncTest
        implements CommandLineRunner {

    private final SalesforceCustomerSyncService customerSyncService;
    private final Integer customerId;

    public SalesforceCustomerSyncTest(
            SalesforceCustomerSyncService customerSyncService,
            @Value("${salesforce.test-customer-id}")
            Integer customerId) {

        this.customerSyncService = customerSyncService;
        this.customerId = customerId;
    }

    @Override
    public void run(String... args) {

        customerSyncService.syncCustomer(customerId);

        System.out.println(
                "Complete Salesforce Customer synchronization successful."
        );

        System.out.println(
                "Salesforce Account and primary Contact synchronized "
                        + "for CBLOS Customer ID: " + customerId
        );
    }
}