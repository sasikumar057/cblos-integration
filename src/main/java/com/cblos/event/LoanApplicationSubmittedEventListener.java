package com.cblos.event;

import org.springframework.stereotype.Component;
import com.cblos.Integration.salesforce.service.SalesforceLoanApplicationSyncService;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LoanApplicationSubmittedEventListener {

    private final SalesforceLoanApplicationSyncService loanSyncService;

    public LoanApplicationSubmittedEventListener(
            SalesforceLoanApplicationSyncService loanSyncService) {
        System.out.println("LISTENER CREATED");
        this.loanSyncService = loanSyncService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleLoanApplicationSubmitted(LoanApplicationSubmittedEvent event) {
        System.out.println("SYNC STARTED");
        try {
            System.out.println("Starting Salesforce synchronization for LocanApplication Id: " + event.applicationId());

            loanSyncService.syncLoanApplication(event.applicationId());

            System.out.println("Salesforce synchronization completed for LoanApplication Id:" + event.applicationId());
        } catch (Exception e) {
            System.err.println("Salesforce synchronization faild for Customer ID:" + event.applicationId());
            e.printStackTrace();
        }
    }
}