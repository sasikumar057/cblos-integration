package com.cblos.event;

import com.cblos.Integration.salesforce.service.SalesforceCustomerSyncService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CustomerApprovedEventListener {

    private final SalesforceCustomerSyncService customerSyncService;

    public CustomerApprovedEventListener(
            SalesforceCustomerSyncService customerSyncService) {

        this.customerSyncService = customerSyncService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCustomerApproved(
            CustomerApprovedEvent event) {

        try {
            System.out.println(
                    "Starting Salesforce synchronization for Customer ID: "
                            + event.customerId());

            customerSyncService.syncCustomer(
                    event.customerId());

            System.out.println(
                    "Salesforce synchronization completed for Customer ID: "
                            + event.customerId());

        } catch (Exception exception) {
            System.err.println(
                    "Salesforce synchronization failed for Customer ID: "
                            + event.customerId());

            System.err.println(
                    "Reason: " + exception.getMessage());
        }
    }
}