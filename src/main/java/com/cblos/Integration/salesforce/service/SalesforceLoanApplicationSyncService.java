package com.cblos.Integration.salesforce.service;

import com.cblos.repository.LoanApplicationRepository;
import org.springframework.stereotype.Service;
import com.cblos.Integration.salesforce.client.SalesforceLoanApplicationClient;
import com.cblos.Integration.salesforce.dto.SalesforceLoanApplicationResponse;
import com.cblos.Integration.salesforce.dto.SalesforceLoanApplicationRequest;
import com.cblos.model.LoanApplication;
import com.cblos.model.SalesforceSyncStatus;

@Service
public class SalesforceLoanApplicationSyncService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final SalesforceLoanApplicationClient salesforceLoanApplicationClient;

    public SalesforceLoanApplicationSyncService(
            LoanApplicationRepository loanApplicationRepository,
            SalesforceLoanApplicationClient salesforceLoanApplicationClient) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.salesforceLoanApplicationClient = salesforceLoanApplicationClient;
    }

    public SalesforceLoanApplicationResponse syncLoanApplication(Integer LoanApplicationId) {

        LoanApplication LoanApplication = loanApplicationRepository.findById(LoanApplicationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Loan Application not found: " + LoanApplicationId));

        LoanApplication.setSalesforceSyncStatus(SalesforceSyncStatus.IN_PROGRESS);
        loanApplicationRepository.save(LoanApplication);

        try {
            SalesforceLoanApplicationRequest LoanApplicationRequest = new SalesforceLoanApplicationRequest(
                    LoanApplication.getLoanAmount(),
                    LoanApplication.getRequestedTenureMonths(),
                    LoanApplication.getLoanProduct().getProductName(),
                    LoanApplication.getCustomer().getSalesforceAccountId());

            SalesforceLoanApplicationResponse LoanApplicationResponse = salesforceLoanApplicationClient
                    .upsertLoanApplication(LoanApplication.getApplicationId(), LoanApplicationRequest);

            LoanApplication.setsalesforceLoanApplicationId(LoanApplicationResponse.id());
            LoanApplication.setSalesforceSyncStatus(SalesforceSyncStatus.SUCCESS);
            loanApplicationRepository.save(LoanApplication);
            return LoanApplicationResponse;

        } catch (Exception exception) {
            LoanApplication.setSalesforceSyncStatus(SalesforceSyncStatus.FAILED);
            loanApplicationRepository.save(LoanApplication);
            throw new IllegalStateException("Salesforce Loan Application sync failed.", exception);
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
