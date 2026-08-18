package com.cblos.Integration.salesforce.service;

import com.cblos.Integration.salesforce.client.SalesforceContactClient;
import com.cblos.Integration.salesforce.dto.SalesforceContactRequest;
import com.cblos.Integration.salesforce.dto.SalesforceContactResponse;
import com.cblos.model.CorporateContact;
import com.cblos.model.CorporateCustomer;
import com.cblos.repository.CorporateContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalesforceContactSyncService {

        private final CorporateContactRepository contactRepository;
        private final SalesforceContactClient contactClient;

        public SalesforceContactSyncService(
                        CorporateContactRepository contactRepository,
                        SalesforceContactClient contactClient) {

                this.contactRepository = contactRepository;
                this.contactClient = contactClient;
        }

        @Transactional
        public SalesforceContactResponse syncContact(
                        Integer contactId) {

                CorporateContact contact = contactRepository.findById(contactId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Corporate Contact not found: "
                                                                + contactId));

                CorporateCustomer customer = contact.getCorporateCustomer();

                if (customer == null) {
                        throw new IllegalStateException(
                                        "Corporate Customer is missing for Contact ID: "
                                                        + contactId);
                }

                String salesforceAccountId = customer.getSalesforceAccountId();

                if (salesforceAccountId == null
                                || salesforceAccountId.isBlank()) {

                        throw new IllegalStateException(
                                        "Salesforce Account must be synchronized "
                                                        + "before synchronizing the Contact.");
                }

                if (contact.getLastName() == null
                                || contact.getLastName().isBlank()) {

                        throw new IllegalStateException(
                                        "Contact last name is required "
                                                        + "for Salesforce synchronization.");
                }

                try {
                        SalesforceContactRequest contactRequest = new SalesforceContactRequest(
                                        contact.getFirstName(),
                                        contact.getLastName(),
                                        salesforceAccountId);

                        SalesforceContactResponse contactResponse = contactClient.upsertContact(
                                        contact.getId(),
                                        contactRequest);

                        contact.setSalesforceContactId(
                                        contactResponse.id());

                        contactRepository.save(contact);

                        return contactResponse;

                } catch (Exception exception) {
                        throw new IllegalStateException(
                                        "Salesforce Contact synchronization failed "
                                                        + "for Contact ID: " + contactId,
                                        exception);
                }
        }
}
