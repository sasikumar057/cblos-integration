package com.cblos.controller;

import com.cblos.model.CorporateCustomer;
import com.cblos.model.LoanAccount;
import com.cblos.model.RepaymentSchedule;
import com.cblos.repository.LoanAccountRepository;
import com.cblos.repository.RepaymentScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/manager/portfolio")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ManagerPortfolioController {

    @Autowired
    private LoanAccountRepository accountRepository;

    @Autowired
    private RepaymentScheduleRepository scheduleRepository;

@GetMapping("/summary")
    public ResponseEntity<PortfolioSummaryResponse> getPortfolioSummary() {
        List<AccountRepaymentSummary> accountSummaries = new ArrayList<>();
        BigDecimal totalDisbursed = BigDecimal.ZERO;
        BigDecimal totalPaidBack = BigDecimal.ZERO;
        BigDecimal totalRemaining = BigDecimal.ZERO;
        LocalDate nearestDueDate = null;
        int activeAccountsCounter = 0;

        for (LoanAccount account : accountRepository.findAll()) {
            List<RepaymentSchedule> schedule = scheduleRepository.findByLoanAccount_Id(account.getId());
            
            // 🟢 FIX STEP A: Calculate the true historical disbursement by summing up 
            // all the underlying schedule principal components instead of reading a muted column cell value!
            BigDecimal trueHistoricalDisbursed = schedule.stream()
                    .map(inst -> valueOrZero(inst.getPrincipalComponent()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Fallback safety filter just in case an old test account has an empty amortization map
            if (trueHistoricalDisbursed.compareTo(BigDecimal.ZERO) == 0) {
                trueHistoricalDisbursed = valueOrZero(account.getPrincipalAmount());
            }

            BigDecimal paidBackAmount = BigDecimal.ZERO;
            BigDecimal remainingAmount = BigDecimal.ZERO;
            LocalDate nextDueDate = null;

            String accountStatus = account.getStatus() != null ? account.getStatus().toUpperCase().trim() : "ACTIVE";
            
            // Standardize checks against your text status flags
            boolean isPreClosedOrSettled = "PAID_PRE_CLOSURE".equals(accountStatus) 
                                        || "CLOSED".equals(accountStatus)
                                        || "SETTLED_CLOSED".equals(accountStatus);

            if (isPreClosedOrSettled) {
                // 🟢 Rule A: Closed or Settled accounts successfully recovered 100% of the true historical principal balance
                paidBackAmount = trueHistoricalDisbursed;
                remainingAmount = BigDecimal.ZERO;
                nextDueDate = null; 
            } else {
                // 🟢 Rule B: Running active loans step through individual installments
                for (RepaymentSchedule installment : schedule) {
                    BigDecimal installmentAmount = valueOrZero(installment.getInstallmentAmount());
                    if (isPaid(installment)) {
                        paidBackAmount = paidBackAmount.add(installmentAmount);
                    } else {
                        remainingAmount = remainingAmount.add(installmentAmount);
                        LocalDate dueDate = installment.getDueDate();
                        if (dueDate != null && (nextDueDate == null || dueDate.isBefore(nextDueDate))) {
                            nextDueDate = dueDate;
                        }
                    }
                }
                
                if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
                    activeAccountsCounter++;
                }
            }

            // 🟢 FIX STEP B: Aggregate global summary values with consistent historical balances
            totalDisbursed = totalDisbursed.add(trueHistoricalDisbursed);
            totalPaidBack = totalPaidBack.add(paidBackAmount);
            totalRemaining = totalRemaining.add(remainingAmount);
            
            if (nextDueDate != null && (nearestDueDate == null || nextDueDate.isBefore(nearestDueDate))) {
                nearestDueDate = nextDueDate;
            }

            CorporateCustomer customer = account.getCustomer();
            accountSummaries.add(new AccountRepaymentSummary(
                    account.getId(),
                    account.getAccountNumber(),
                    customer != null ? customer.getId() : null,
                    customer != null ? customer.getCompanyName() : "Unknown customer",
                    account.getLoanApplication() != null ? account.getLoanApplication().getApplicationId() : null,
                    account.getLoanApplication() != null ? account.getLoanApplication().getLoanType() : "Loan",
                    account.getStatus(),
                    trueHistoricalDisbursed, // 🟢 Now passing the accurate calculation value back to the UI grid
                    paidBackAmount,
                    remainingAmount,
                    nextDueDate
            ));
        }

        accountSummaries.sort(Comparator
                .comparing(AccountRepaymentSummary::nextDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(AccountRepaymentSummary::customerName, String.CASE_INSENSITIVE_ORDER));

        return ResponseEntity.ok(new PortfolioSummaryResponse(
                totalDisbursed,
                totalPaidBack,
                totalRemaining,
                nearestDueDate,
                activeAccountsCounter, 
                accountSummaries
        ));
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private boolean isPaid(RepaymentSchedule installment) {
        String status = installment.getStatus();
        return status != null && ("PAID".equalsIgnoreCase(status) || "PAID_PRE_CLOSURE".equalsIgnoreCase(status));
    }

    public record PortfolioSummaryResponse(
            BigDecimal totalDisbursed,
            BigDecimal totalPaidBack,
            BigDecimal totalRemaining,
            LocalDate nearestDueDate,
            int activeAccountCount,
            List<AccountRepaymentSummary> accounts
    ) {}

    public record AccountRepaymentSummary(
            Integer accountId,
            String accountNumber,
            Integer customerId,
            String customerName,
            Integer applicationId,
            String loanType,
            String accountStatus,
            BigDecimal disbursedAmount,
            BigDecimal paidBackAmount,
            BigDecimal remainingAmount,
            LocalDate nextDueDate
    ) {}
}