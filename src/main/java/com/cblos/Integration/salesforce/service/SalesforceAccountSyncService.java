package com.cblos.Integration.salesforce.service;

import com.cblos.Integration.salesforce.client.SalesforceAccountClient;
import com.cblos.Integration.salesforce.dto.SalesforceAccountRequest;
import com.cblos.Integration.salesforce.dto.SalesforceAccountResponse;
import com.cblos.model.CorporateCustomer;
import com.cblos.model.SalesforceSyncStatus;
import com.cblos.repository.CorporateCustomerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SalesforceAccountSyncService {

    private final CorporateCustomerRepository customerRepository;
    private final SalesforceAccountClient accountClient;

    public SalesforceAccountSyncService(
            CorporateCustomerRepository customerRepository,
            SalesforceAccountClient accountClient) {

        this.customerRepository = customerRepository;
        this.accountClient = accountClient;
    }

    public SalesforceAccountResponse syncAccount(
            Integer customerId) {

        CorporateCustomer customer =
                customerRepository.findById(customerId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Corporate Customer not found: "
                                                + customerId
                                )
                        );

        customer.setSalesforceSyncStatus(
                SalesforceSyncStatus.IN_PROGRESS
        );

        customer.setSalesforceSyncError(null);
        customer.setSalesforceLastSyncAt(
                LocalDateTime.now()
        );

        customerRepository.save(customer);

        try {
            SalesforceAccountRequest accountRequest =
                    new SalesforceAccountRequest(
                            customer.getCompanyName(),
                            customer.getPhoneNumber()
                    );

            SalesforceAccountResponse accountResponse =
                    accountClient.upsertAccount(
                            customer.getId(),
                            accountRequest
                    );

            customer.setSalesforceAccountId(
                    accountResponse.id()
            );

            customer.setSalesforceSyncStatus(
                    SalesforceSyncStatus.SUCCESS
            );

            customer.setSalesforceSyncError(null);

            customer.setSalesforceLastSyncAt(
                    LocalDateTime.now()
            );

            customerRepository.save(customer);

            return accountResponse;

        } catch (Exception exception) {
            customer.setSalesforceSyncStatus(
                    SalesforceSyncStatus.FAILED
            );

            customer.setSalesforceSyncError(
                    createSafeErrorMessage(exception)
            );

            customer.setSalesforceLastSyncAt(
                    LocalDateTime.now()
            );

            customerRepository.save(customer);

            throw new IllegalStateException(
                    "Salesforce Account synchronization failed.",
                    exception
            );
        }
    }

    private String createSafeErrorMessage(
            Exception exception) {

        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            return "Salesforce Account synchronization failed.";
        }

        /*
         * The database column is limited to 2000 characters.
         * We store only a short message, not tokens or full payloads.
         */
        return message.length() > 1900
                ? message.substring(0, 1900)
                : message;
    }
}