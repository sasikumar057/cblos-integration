package com.cblos.Integration.salesforce.service;

import com.cblos.repository.LoanApplicationRepository;
import org.springframework.stereotype.Service;
import com.cblos.Integration.salesforce.client.SalesforceLoanApplicationClient;
import com.cblos.Integration.salesforce.dto.SalesforceLoanApplicationResponse;
import com.cblos.Integration.salesforce.dto.SalesforceLoanApplicationRequest;
import com.cblos.model.LoanApplication;
import com.cblos.model.SalesforceSyncStatus;
import java.time.LocalDate;

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
        LoanApplication.setSalesforceSyncError(null);
        LoanApplication.setSalesforceLastSyncAt(java.time.LocalDate.now());
        loanApplicationRepository.save(LoanApplication);

        try {
            SalesforceLoanApplicationRequest LoanApplicationRequest = new SalesforceLoanApplicationRequest(
                    LoanApplication.getLoanAmount(),
                    LoanApplication.getRequestedTenureMonths(),
                    LoanApplication.getLoanProduct().getProductName(),
                    LoanApplication.getCustomer().getSalesforceAccountId(),
                    LoanApplication.getStatus(),
                    LoanApplication.getSubmissionDate());

            SalesforceLoanApplicationResponse LoanApplicationResponse = salesforceLoanApplicationClient
                    .upsertLoanApplication(LoanApplication.getApplicationId(), LoanApplicationRequest);

            LoanApplication.setsalesforceLoanApplicationId(LoanApplicationResponse.id());
            LoanApplication.setSalesforceSyncStatus(SalesforceSyncStatus.SUCCESS);
            LoanApplication.setSalesforceLastSyncAt(LocalDate.now());
            LoanApplication.setSalesforceSyncError(null);
            loanApplicationRepository.save(LoanApplication);
            return LoanApplicationResponse;

        } catch (Exception exception) {
            LoanApplication.setSalesforceSyncStatus(SalesforceSyncStatus.FAILED);
            LoanApplication.setSalesforceSyncError(exception.getMessage());
            LoanApplication.setSalesforceLastSyncAt(LocalDate.now());
            loanApplicationRepository.save(LoanApplication);
            throw new IllegalStateException("Salesforce Loan Application sync failed.", exception);
        }
    }

}
