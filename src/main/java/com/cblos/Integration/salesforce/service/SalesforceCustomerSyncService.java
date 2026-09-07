package com.cblos.Integration.salesforce.service;

import com.cblos.model.CorporateContact;
import com.cblos.model.CorporateCustomer;
import com.cblos.model.SalesforceSyncStatus;
import com.cblos.repository.CorporateContactRepository;
import com.cblos.repository.CorporateCustomerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SalesforceCustomerSyncService {

    private final CorporateCustomerRepository customerRepository;
    private final CorporateContactRepository contactRepository;
    private final SalesforceAccountSyncService accountSyncService;
    private final SalesforceContactSyncService contactSyncService;

    public SalesforceCustomerSyncService(
            CorporateCustomerRepository customerRepository,
            CorporateContactRepository contactRepository,
            SalesforceAccountSyncService accountSyncService,
            SalesforceContactSyncService contactSyncService) {

        this.customerRepository = customerRepository;
        this.contactRepository = contactRepository;
        this.accountSyncService = accountSyncService;
        this.contactSyncService = contactSyncService;
    }

    public void syncCustomer(Integer customerId) {

        CorporateCustomer customer =
                customerRepository.findById(customerId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Corporate Customer not found: "
                                                + customerId
                                )
                        );

        /*
         * Mark the overall Salesforce synchronization
         * as currently running.
         */
        customer.setSalesforceSyncStatus(
                SalesforceSyncStatus.IN_PROGRESS
        );

        customer.setSalesforceSyncError(null);

        customer.setSalesforceLastSyncAt(
                LocalDateTime.now()
        );

        customerRepository.save(customer);

        try {
            /*
             * The Salesforce Account must be synchronized
             * before the Contact because Contact requires
             * the Salesforce AccountId.
             */
            accountSyncService.syncAccount(customerId);

            /*
             * Find the primary Contact connected to
             * the Corporate Customer.
             */
            CorporateContact primaryContact =
                    contactRepository
                            .findByCorporateCustomerIdAndPrimaryTrue(
                                    customerId
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Primary Contact not found "
                                                    + "for Customer ID: "
                                                    + customerId
                                    )
                            );

            /*
             * Contact synchronization reads the Salesforce
             * Account ID saved during Account synchronization.
             */
            contactSyncService.syncContact(
                    primaryContact.getId()
            );

            /*
             * Reload the Customer because Account sync may
             * have changed its Salesforce tracking fields.
             */
            CorporateCustomer syncedCustomer =
                    customerRepository.findById(customerId)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Customer disappeared during "
                                                    + "Salesforce synchronization."
                                    )
                            );

            syncedCustomer.setSalesforceSyncStatus(
                    SalesforceSyncStatus.SUCCESS
            );

            syncedCustomer.setSalesforceSyncError(null);

            syncedCustomer.setSalesforceLastSyncAt(
                    LocalDateTime.now()
            );

            customerRepository.save(syncedCustomer);

        } catch (Exception exception) {

            CorporateCustomer failedCustomer =
                    customerRepository.findById(customerId)
                            .orElse(customer);

            failedCustomer.setSalesforceSyncStatus(
                    SalesforceSyncStatus.FAILED
            );

            failedCustomer.setSalesforceSyncError(
                    createSafeErrorMessage(exception)
            );

            failedCustomer.setSalesforceLastSyncAt(
                    LocalDateTime.now()
            );

            customerRepository.save(failedCustomer);

            throw new IllegalStateException(
                    "Complete Salesforce Customer "
                            + "synchronization failed for Customer ID: "
                            + customerId,
                    exception
            );
        }
    }

    private String createSafeErrorMessage(
            Exception exception) {

        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            return "Salesforce Customer synchronization "
                    + "failed with an unknown error.";
        }

        return message.length() > 1900
                ? message.substring(0, 1900)
                : message;
    }
}