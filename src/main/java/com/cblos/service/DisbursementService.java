package com.cblos.service;

import com.cblos.model.Disbursement;
import com.cblos.model.LoanAccount;
import com.cblos.model.RepaymentSchedule;
import com.cblos.repository.DisbursementRepository;
import com.cblos.repository.RepaymentScheduleRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class DisbursementService {

    @Autowired
    private DisbursementRepository disbursementRepository;
    
    @Autowired
    private RepaymentScheduleRepository scheduleRepository;

    @Transactional 
    public Disbursement scheduleDisbursement(LoanAccount account) {
        if (account == null || account.getLoanApplication() == null) {
            throw new IllegalArgumentException("System Error: Cannot schedule disbursement for an account with missing application context.");
        }

        Integer dynamicTenureMonths = account.getLoanApplication().getRequestedTenureMonths();

        if (dynamicTenureMonths == null || dynamicTenureMonths <= 0) {
            dynamicTenureMonths = 12; 
            System.out.println("Warning: Tenure was null or invalid. Defaulting schedule partition to 12 months.");
        }

        Disbursement disbursement = new Disbursement();
        disbursement.setLoanAccount(account); 
        disbursement.setDisbursedAmount(account.getPrincipalAmount());
        disbursement.setDisbursementDate(LocalDate.now());
        disbursement.setReferenceNumber("TXN-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        
        Disbursement savedDisbursement = disbursementRepository.save(disbursement);
        
        generateRepaymentSchedule(account, dynamicTenureMonths);
        
        System.out.println("[Disbursement Engine] Automatically generated a trackable " 
                + dynamicTenureMonths + "-month dynamic amortization schedule for Account: " + account.getAccountNumber());
        
        return savedDisbursement;
    }
    
    private void generateRepaymentSchedule(LoanAccount account, int months) {
        BigDecimal totalPrincipal = account.getPrincipalAmount();
        BigDecimal principalPerMonth = totalPrincipal.divide(new BigDecimal(months), 2, RoundingMode.HALF_UP);
        
        double annualRate = account.getInterestRate();
        BigDecimal annualRatePercentage = BigDecimal.valueOf(annualRate).divide(BigDecimal.valueOf(100));
        BigDecimal totalAnnualInterest = totalPrincipal.multiply(annualRatePercentage);
        BigDecimal interestPerMonth = totalAnnualInterest.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

        BigDecimal totalInstallmentAmount = principalPerMonth.add(interestPerMonth);

        for (int i = 1; i <= months; i++) {
            RepaymentSchedule schedule = new RepaymentSchedule();
            schedule.setLoanAccount(account);
            schedule.setInstallmentNumber(i);
            schedule.setDueDate(LocalDate.now().plusMonths(i));

            schedule.setPrincipalComponent(principalPerMonth);
            schedule.setInterestComponent(interestPerMonth);
            schedule.setInstallmentAmount(totalInstallmentAmount); 
            schedule.setStatus("Pending");
            
            scheduleRepository.save(schedule);
        }
        System.out.println("Schedule Generated: " + months + " months initialized with interest component: ₹" + interestPerMonth);
    }

    //postmand tesing
    public String generateDisbursementReport() {

        List<Disbursement> allDisbursements = disbursementRepository.findAll();
        
        if (allDisbursements.isEmpty()) {
            return "--- Disbursement Compliance Report ---\nNo corporate disbursement records found in the core ledger.";
        }

        StringBuilder report = new StringBuilder();
        report.append("================================================================================\n");
        report.append("TRUSTEDGE COMMERCIAL BANK - DISBURSEMENT AUDIT REPORT\n");
        report.append("Generated On: ").append(LocalDate.now()).append("\n");
        report.append("================================================================================\n\n");

        for (Disbursement d : allDisbursements) {
            report.append(" TRANSACTION RECORD [REF ID: ").append(d.getReferenceNumber()).append("]\n")
                  .append("  • Disbursement Database Row ID : ").append(d.getId()).append("\n")
                  .append("  • Associated Account Asset ID  : ").append(d.getLoanAccount().getId()).append("\n")
                  .append("  • Core Checking Account Number : ").append(d.getLoanAccount().getAccountNumber()).append("\n")
                  .append("  • Funded Corporate Client Name : ").append(d.getLoanAccount().getCustomer().getCompanyName()).append("\n")
                  .append("  • Capital Released to Client   : ₹").append(d.getDisbursedAmount()).append("\n")
                  .append("  • Settlement Timestamp Value   : ").append(d.getDisbursementDate()).append("\n")
                  .append(" ───────────────────────────────────────────────────────────────────────────────\n");
        }
        
        report.append("\n================================================================================\n");
        report.append("END OF COMPLIANCE LEDGER AUDIT STRINGS\n");
        report.append("================================================================================\n");

        return report.toString();
    }
    

    public List<RepaymentSchedule> getRepaymentSchedule(Integer accountId) {
        return scheduleRepository.findByLoanAccount_Id(accountId);
    }

    public void updateInstallmentStatus(Integer installmentId, String status) {
        RepaymentSchedule schedule = scheduleRepository.findById(installmentId)
                .orElseThrow(() -> new RuntimeException("Installment not found"));
        schedule.setStatus(status);
        scheduleRepository.save(schedule);
    }
    
    public void updateInstallmentStatusSecurely(Integer accountId, Integer installmentId, String status) {
        RepaymentSchedule schedule = scheduleRepository.findById(installmentId)
                .orElseThrow(() -> new RuntimeException("Installment not found"));

        if (!schedule.getLoanAccount().getId().equals(accountId)) {
            throw new RuntimeException("Security Error: This installment does not belong to Account ID " + accountId);
        }

        schedule.setStatus(status);
        scheduleRepository.save(schedule);
    }

    @Transactional
    public void settleAccountSecurely(LoanAccount account) {
        if (account == null || account.getId() == null) {
            throw new IllegalArgumentException("Active loan account not found.");
        }

        List<RepaymentSchedule> schedules = scheduleRepository.findByLoanAccount_Id(account.getId());
        for (RepaymentSchedule schedule : schedules) {
            String currentStatus = schedule.getStatus();
            if ("PENDING".equalsIgnoreCase(currentStatus) || "UNPAID".equalsIgnoreCase(currentStatus)) {
                schedule.setStatus("PAID_PRE_CLOSURE");
                scheduleRepository.save(schedule);
            }
        }

        account.setPrincipalAmount(BigDecimal.ZERO);
        account.setStatus("SETTLED_CLOSED");
    }
}
