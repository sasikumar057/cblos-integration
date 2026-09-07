package com.cblos.controller;

import com.cblos.model.RepaymentSchedule;
import com.cblos.model.LoanAccount;
import com.cblos.repository.LoanAccountRepository;
import com.cblos.service.DisbursementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repayments")
public class RepaymentController {

    @Autowired
    private DisbursementService disbursementService;

    @Autowired
    private LoanAccountRepository loanAccountRepository;

    // 1. View the full installment plan for an account
    @GetMapping("/schedule/{accountId}")
    public ResponseEntity<List<RepaymentSchedule>> getRepaymentStatus(@PathVariable Integer accountId) {
        return ResponseEntity.ok(disbursementService.getRepaymentSchedule(accountId));
    }

 // 2. Simulate paying an installment (Securely tied to the account)
    @PutMapping("/account/{accountId}/pay/{installmentId}")
    public ResponseEntity<String> recordPayment(
            @PathVariable Integer accountId, 
            @PathVariable Integer installmentId) {
        
        disbursementService.updateInstallmentStatusSecurely(accountId, installmentId, "PAID");
        return ResponseEntity.ok("Payment successful for Account ID " + accountId + "!");
    }

    @PutMapping("/account/{accountId}/settle")
    public ResponseEntity<String> settleAccount(@PathVariable Integer accountId) {
        LoanAccount account = loanAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Loan account not found."));
        disbursementService.settleAccountSecurely(account);
        loanAccountRepository.save(account);
        return ResponseEntity.ok("Full settlement successful for Account ID " + accountId + "!");
    }
}
