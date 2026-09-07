package com.cblos.Integration.salesforce.config;

import com.cblos.Integration.salesforce.dto.SalesforceContactResponse;
import com.cblos.Integration.salesforce.service.SalesforceContactSyncService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


@Component
@Profile("salesforce-contact-test")
public class SalesforceContactSyncTest
        implements CommandLineRunner {

    private final SalesforceContactSyncService contactSyncService;
    private final Integer contactId;

    public SalesforceContactSyncTest(
            SalesforceContactSyncService contactSyncService,
            @Value("${salesforce.test-contact-id}") Integer contactId) {

        this.contactSyncService = contactSyncService;
        this.contactId = contactId;
    }

    @Override
    public void run(String... args) {

        SalesforceContactResponse response = contactSyncService.syncContact(contactId);

        System.out.println(
                "Salesforce Contact synchronization successful.");

        System.out.println(
                "Salesforce Contact ID: " + response.id());

        System.out.println(
                "Salesforce Contact name: "
                        + response.firstName()
                        + " "
                        + response.lastName());
    }
}